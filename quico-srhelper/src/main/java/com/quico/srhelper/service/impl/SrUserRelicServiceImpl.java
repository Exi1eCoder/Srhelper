package com.quico.srhelper.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quico.common.constant.HttpStatus;
import com.quico.common.exception.BusinessException;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;
import com.quico.srhelper.constants.RelicRuleConstants;
import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.domain.SrRelicMainAffix;
import com.quico.srhelper.domain.SrRelicSubAffix;
import com.quico.srhelper.domain.SrUserRelic;
import com.quico.srhelper.domain.SrUserRelicSubAffix;
import com.quico.srhelper.domain.dto.SrUserRelicSaveDTO;
import com.quico.srhelper.domain.dto.SrUserRelicSubAffixDTO;
import com.quico.srhelper.mapper.SrRelicMainAffixMapper;
import com.quico.srhelper.mapper.SrRelicMapper;
import com.quico.srhelper.mapper.SrRelicSubAffixMapper;
import com.quico.srhelper.mapper.SrUserRelicMapper;
import com.quico.srhelper.mapper.SrUserRelicSubAffixMapper;
import com.quico.srhelper.service.ISrUserRelicService;

/**
 * 用户遗器Service业务层处理
 * 
 * @author quico
 * @date 2026-11-25
 */
@Service
public class SrUserRelicServiceImpl implements ISrUserRelicService
{
    private static final int VALUE_SCALE = 10;

    @Autowired
    private SrUserRelicMapper srUserRelicMapper;

    @Autowired
    private SrUserRelicSubAffixMapper srUserRelicSubAffixMapper;

    @Autowired
    private SrRelicMapper srRelicMapper;

    @Autowired
    private SrRelicMainAffixMapper srRelicMainAffixMapper;

    @Autowired
    private SrRelicSubAffixMapper srRelicSubAffixMapper;

    @Override
    public List<SrUserRelic> selectSrUserRelicList(SrUserRelic srUserRelic)
    {
        if (srUserRelic == null)
        {
            srUserRelic = new SrUserRelic();
        }
        srUserRelic.setUserId(SecurityUtils.getUserId());
        return srUserRelicMapper.selectSrUserRelicList(srUserRelic);
    }

    @Override
    public SrUserRelic selectSrUserRelicDetail(Long id)
    {
        SrUserRelic relic = srUserRelicMapper.selectSrUserRelicById(id);
        if (relic == null)
        {
            throw new BusinessException(HttpStatus.NOT_FOUND, "遗器不存在");
        }
        checkOwner(relic);
        relic.setSubAffixes(srUserRelicSubAffixMapper.selectByUserRelicId(id));
        return relic;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SrUserRelic saveSrUserRelic(SrUserRelicSaveDTO dto)
    {
        if (dto == null)
        {
            throw badRequest("保存参数不能为空");
        }
        validateRuleVersion(dto.getRuleVersion());

        SrRelic relicDef = srRelicMapper.selectSrRelicByRelicId(dto.getRelicId());
        if (relicDef == null)
        {
            throw badRequest("遗器定义不存在");
        }
        if (!Long.valueOf(RelicRuleConstants.RARITY_FIVE_STAR).equals(relicDef.getRarity()))
        {
            throw badRequest("仅支持五星遗器（ONLY_FIVE_STAR_SUPPORTED）");
        }

        int level = dto.getLevel() == null ? 0 : dto.getLevel();
        if (level < 0 || level > RelicRuleConstants.MAX_LEVEL)
        {
            throw badRequest("等级必须在 0 ~ " + RelicRuleConstants.MAX_LEVEL + " 之间");
        }

        Integer initialSubCount = dto.getInitialSubCount();
        if (initialSubCount == null
                || (initialSubCount.intValue() != RelicRuleConstants.SUB_COUNT_THREE
                    && initialSubCount.intValue() != RelicRuleConstants.SUB_COUNT_FOUR))
        {
            throw badRequest("初始词条数只能为 3 或 4");
        }

        List<SrUserRelicSubAffixDTO> subDtos = dto.getSubAffixes() == null
                ? Collections.emptyList() : dto.getSubAffixes();
        int expectedSubCount = RelicRuleConstants.getExpectedSubCount(initialSubCount, level);
        if (subDtos.size() != expectedSubCount)
        {
            throw badRequest("副词条数量应为 " + expectedSubCount + " 条");
        }

        if (StringUtils.isEmpty(relicDef.getMainAffixId()) || StringUtils.isEmpty(relicDef.getSubAffixId()))
        {
            throw badRequest("遗器的词条组配置缺失");
        }
        List<SrRelicMainAffix> mainPool = srRelicMainAffixMapper
                .selectByGroupIds(Collections.singletonList(relicDef.getMainAffixId()));
        List<SrRelicSubAffix> subPool = srRelicSubAffixMapper.selectByGroupId(relicDef.getSubAffixId());

        String mainProperty = dto.getMainProperty();
        if (StringUtils.isEmpty(mainProperty))
        {
            throw badRequest("主属性不能为空");
        }
        SrRelicMainAffix mainDef = findMainAffix(mainPool, mainProperty);
        if (mainDef == null)
        {
            throw badRequest("主属性不在可选池中：" + mainProperty);
        }
        if (RelicRuleConstants.SLOT_HEAD.equals(relicDef.getSlot())
                && !RelicRuleConstants.MAIN_PROPERTY_HEAD.equals(mainProperty))
        {
            throw badRequest("HEAD 主属性必须为 " + RelicRuleConstants.MAIN_PROPERTY_HEAD);
        }
        if (RelicRuleConstants.SLOT_HAND.equals(relicDef.getSlot())
                && !RelicRuleConstants.MAIN_PROPERTY_HAND.equals(mainProperty))
        {
            throw badRequest("HAND 主属性必须为 " + RelicRuleConstants.MAIN_PROPERTY_HAND);
        }

        int upgradeLimit = RelicRuleConstants.getUpgradeLimit(initialSubCount);
        Set<String> usedProperties = new HashSet<>();
        int totalUpgrades = 0;
        List<SrUserRelicSubAffix> subEntities = new ArrayList<>(subDtos.size());
        for (int i = 0; i < subDtos.size(); i++)
        {
            SrUserRelicSubAffixDTO subDto = subDtos.get(i);
            if (subDto == null || StringUtils.isEmpty(subDto.getProperty()))
            {
                throw badRequest("副词条属性不能为空");
            }
            String property = subDto.getProperty();
            if (property.equals(mainProperty))
            {
                throw badRequest("副词条不能与主属性重复：" + property);
            }
            if (!usedProperties.add(property))
            {
                throw badRequest("副词条属性重复：" + property);
            }
            SrRelicSubAffix subDef = findSubAffix(subPool, property);
            if (subDef == null)
            {
                throw badRequest("副词条不在可选池中：" + property);
            }
            int tierCount = RelicRuleConstants.getTierCount(subDef.getStepNum());
            if (subDto.getBaseTier() == null
                    || subDto.getBaseTier() < 1 || subDto.getBaseTier() > tierCount)
            {
                throw badRequest("初始档位不合法：" + property);
            }

            List<Integer> upgradeTiers = parseUpgradeTiers(subDto.getUpgradeTiers(), property);
            totalUpgrades += upgradeTiers.size();

            BigDecimal baseValue = scale(subDef.getBase()
                    .add(subDef.getStep().multiply(BigDecimal.valueOf(subDto.getBaseTier() - 1))));
            BigDecimal upgradeValue = BigDecimal.ZERO;
            for (Integer tier : upgradeTiers)
            {
                if (tier < 1 || tier > tierCount)
                {
                    throw badRequest("强化档位不合法：" + property);
                }
                BigDecimal increment = subDef.getBase()
                        .add(subDef.getStep().multiply(BigDecimal.valueOf(tier - 1)));
                upgradeValue = upgradeValue.add(increment);
            }
            upgradeValue = scale(upgradeValue);
            subEntities.add(SrUserRelicSubAffix.builder()
                    .property(property)
                    .baseTier(subDto.getBaseTier())
                    .baseValue(baseValue)
                    .upgradeTiers(joinTiers(upgradeTiers))
                    .upgradeValue(upgradeValue)
                    .totalValue(scale(baseValue.add(upgradeValue)))
                    .sortOrder((long) i)
                    .build());
        }
        if (totalUpgrades > upgradeLimit)
        {
            throw badRequest("强化次数超过上限 " + upgradeLimit + " 次");
        }

        Long userId = SecurityUtils.getUserId();
        SrUserRelic entity = new SrUserRelic();
        SrUserRelic existing = null;
        String origin;
        if (dto.getId() == null)
        {
            entity.setUserId(userId);
            entity.setCreateBy(String.valueOf(userId));
            entity.setCreateTime(DateUtils.getNowDate());
            entity.setSortOrder(0L);
            origin = StringUtils.isEmpty(dto.getOrigin())
                    ? RelicRuleConstants.ORIGIN_SIMULATED : dto.getOrigin();
        }
        else
        {
            existing = srUserRelicMapper.selectSrUserRelicById(dto.getId());
            if (existing == null)
            {
                throw new BusinessException(HttpStatus.NOT_FOUND, "遗器不存在");
            }
            checkOwner(existing);
            entity.setId(existing.getId());
            entity.setUserId(existing.getUserId());
            entity.setUpdateBy(String.valueOf(userId));
            entity.setUpdateTime(DateUtils.getNowDate());
            entity.setSortOrder(existing.getSortOrder() == null ? 0L : existing.getSortOrder());
            origin = resolveOrigin(existing, dto);
        }
        if (!RelicRuleConstants.ORIGINS.contains(origin))
        {
            throw badRequest("来源标记不合法：" + origin);
        }

        Boolean locked = dto.getLocked();
        if (locked == null)
        {
            locked = existing == null ? Boolean.FALSE
                    : Boolean.TRUE.equals(existing.getLocked());
        }

        entity.setRelicId(dto.getRelicId());
        entity.setSlot(relicDef.getSlot());
        entity.setLevel(level);
        entity.setInitialSubCount(initialSubCount);
        entity.setSubUpgradeUsed(totalUpgrades);
        entity.setMainProperty(mainProperty);
        entity.setMainValue(scale(mainDef.getBase()
                .add(mainDef.getStep().multiply(BigDecimal.valueOf(level)))));
        entity.setEquippedBy(dto.getEquippedBy());
        entity.setLocked(locked);
        entity.setOrigin(origin);

        releaseEquippedConflict(entity.getUserId(), entity.getEquippedBy(), entity.getSlot(), entity.getId());
        try
        {
            persistSrUserRelic(entity);
        }
        catch (DuplicateKeyException e)
        {
            // 并发下由唯一键兜底：重新释放同部位占用后重试一次
            releaseEquippedConflict(entity.getUserId(), entity.getEquippedBy(), entity.getSlot(), entity.getId());
            persistSrUserRelic(entity);
        }
        for (SrUserRelicSubAffix sub : subEntities)
        {
            sub.setUserRelicId(entity.getId());
            sub.setCreateBy(String.valueOf(userId));
            sub.setCreateTime(DateUtils.getNowDate());
        }
        if (!subEntities.isEmpty())
        {
            srUserRelicSubAffixMapper.batchInsert(subEntities);
        }
        return selectSrUserRelicDetail(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSrUserRelicByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        List<SrUserRelic> relics = srUserRelicMapper.selectSrUserRelicByIds(ids);
        if (relics.size() != ids.length)
        {
            throw new BusinessException(HttpStatus.NOT_FOUND, "部分遗器不存在");
        }
        for (SrUserRelic relic : relics)
        {
            checkOwner(relic);
            if (Boolean.TRUE.equals(relic.getLocked()))
            {
                throw badRequest("已锁定的遗器不可删除");
            }
        }
        srUserRelicSubAffixMapper.deleteByUserRelicIds(ids);
        return srUserRelicMapper.deleteSrUserRelicByIds(ids);
    }

    @Override
    public int updateSrUserRelicLock(Long id, Boolean locked)
    {
        requireOwnedRelic(id);
        Long userId = SecurityUtils.getUserId();
        return srUserRelicMapper.updateSrUserRelicLock(id, Boolean.TRUE.equals(locked),
                String.valueOf(userId), DateUtils.getNowDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSrUserRelicEquip(Long id, Long equippedBy)
    {
        SrUserRelic relic = requireOwnedRelic(id);
        Long userId = SecurityUtils.getUserId();
        String updateBy = String.valueOf(userId);
        if (equippedBy == null)
        {
            return srUserRelicMapper.updateSrUserRelicEquip(id, null, updateBy, DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(relic.getSlot()))
        {
            throw badRequest("遗器部位缺失，无法装备");
        }
        releaseEquippedConflict(userId, equippedBy, relic.getSlot(), id);
        try
        {
            return srUserRelicMapper.updateSrUserRelicEquip(id, equippedBy, updateBy, DateUtils.getNowDate());
        }
        catch (DuplicateKeyException e)
        {
            // 并发下由唯一键兜底：重新释放同部位占用后重试一次
            releaseEquippedConflict(userId, equippedBy, relic.getSlot(), id);
            try
            {
                return srUserRelicMapper.updateSrUserRelicEquip(id, equippedBy, updateBy, DateUtils.getNowDate());
            }
            catch (DuplicateKeyException ex)
            {
                throw badRequest("装备冲突，请重试");
            }
        }
    }

    /**
     * 释放同角色同部位已装备的遗器（置为未装备）
     */
    private void releaseEquippedConflict(Long userId, Long equippedBy, String slot, Long excludeId)
    {
        if (equippedBy == null || StringUtils.isEmpty(slot))
        {
            return;
        }
        List<SrUserRelic> conflicts = srUserRelicMapper
                .selectEquippedRelicBySlot(userId, equippedBy, slot, excludeId);
        String updateBy = String.valueOf(userId);
        for (SrUserRelic conflict : conflicts)
        {
            srUserRelicMapper.updateSrUserRelicEquip(conflict.getId(), null, updateBy, DateUtils.getNowDate());
        }
    }

    /**
     * 写入遗器主表与副词条
     */
    private void persistSrUserRelic(SrUserRelic entity)
    {
        if (entity.getId() == null)
        {
            srUserRelicMapper.insertSrUserRelic(entity);
        }
        else
        {
            srUserRelicMapper.updateSrUserRelic(entity);
            srUserRelicSubAffixMapper.deleteByUserRelicId(entity.getId());
        }
    }

    private SrUserRelic requireOwnedRelic(Long id)
    {
        SrUserRelic relic = srUserRelicMapper.selectSrUserRelicById(id);
        if (relic == null)
        {
            throw new BusinessException(HttpStatus.NOT_FOUND, "遗器不存在");
        }
        checkOwner(relic);
        return relic;
    }

    private void checkOwner(SrUserRelic relic)
    {
        Long userId = SecurityUtils.getUserId();
        if (!userId.equals(relic.getUserId()) && !SecurityUtils.isAdmin(userId))
        {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权操作该遗器");
        }
    }

    private void validateRuleVersion(String ruleVersion)
    {
        if (StringUtils.isEmpty(ruleVersion) || !RelicRuleConstants.RULE_VERSION.equals(ruleVersion))
        {
            throw badRequest("规则数据已更新，请刷新后重试（RULE_VERSION_MISMATCH）");
        }
    }

    private String resolveOrigin(SrUserRelic existing, SrUserRelicSaveDTO dto)
    {
        String requested = dto.getOrigin();
        if (RelicRuleConstants.ORIGIN_SIMULATED.equals(requested))
        {
            return RelicRuleConstants.ORIGIN_SIMULATED;
        }
        if (RelicRuleConstants.ORIGIN_IMPORTED_MODIFIED.equals(requested))
        {
            return RelicRuleConstants.ORIGIN_IMPORTED_MODIFIED;
        }
        if (RelicRuleConstants.ORIGIN_IMPORTED.equals(existing.getOrigin()))
        {
            List<SrUserRelicSubAffix> oldSubs = srUserRelicSubAffixMapper
                    .selectByUserRelicId(existing.getId());
            return sameAllocation(oldSubs, dto.getSubAffixes())
                    ? RelicRuleConstants.ORIGIN_IMPORTED
                    : RelicRuleConstants.ORIGIN_IMPORTED_MODIFIED;
        }
        if (StringUtils.isEmpty(requested))
        {
            return StringUtils.isEmpty(existing.getOrigin())
                    ? RelicRuleConstants.ORIGIN_SIMULATED : existing.getOrigin();
        }
        return requested;
    }

    private boolean sameAllocation(List<SrUserRelicSubAffix> oldSubs, List<SrUserRelicSubAffixDTO> newSubs)
    {
        Map<String, String> oldMap = new HashMap<>();
        if (oldSubs != null)
        {
            for (SrUserRelicSubAffix sub : oldSubs)
            {
                oldMap.put(sub.getProperty(), sub.getBaseTier() + "|" + normalizeTiers(sub.getUpgradeTiers()));
            }
        }
        Map<String, String> newMap = new HashMap<>();
        if (newSubs != null)
        {
            for (SrUserRelicSubAffixDTO sub : newSubs)
            {
                newMap.put(sub.getProperty(), sub.getBaseTier() + "|" + normalizeTiers(sub.getUpgradeTiers()));
            }
        }
        return oldMap.equals(newMap);
    }

    private String normalizeTiers(String tiers)
    {
        return StringUtils.isEmpty(tiers) ? "" : tiers.replace(" ", "");
    }

    private List<Integer> parseUpgradeTiers(String upgradeTiers, String property)
    {
        List<Integer> tiers = new ArrayList<>();
        if (StringUtils.isEmpty(upgradeTiers))
        {
            return tiers;
        }
        for (String part : upgradeTiers.split(","))
        {
            String value = part.trim();
            if (value.isEmpty())
            {
                continue;
            }
            try
            {
                tiers.add(Integer.parseInt(value));
            }
            catch (NumberFormatException e)
            {
                throw badRequest("强化档位格式不合法：" + property);
            }
        }
        return tiers;
    }

    private String joinTiers(List<Integer> tiers)
    {
        return tiers.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private SrRelicMainAffix findMainAffix(List<SrRelicMainAffix> pool, String property)
    {
        if (pool == null)
        {
            return null;
        }
        for (SrRelicMainAffix affix : pool)
        {
            if (property.equals(affix.getProperty()))
            {
                return affix;
            }
        }
        return null;
    }

    private SrRelicSubAffix findSubAffix(List<SrRelicSubAffix> pool, String property)
    {
        if (pool == null)
        {
            return null;
        }
        for (SrRelicSubAffix affix : pool)
        {
            if (property.equals(affix.getProperty()))
            {
                return affix;
            }
        }
        return null;
    }

    private BigDecimal scale(BigDecimal value)
    {
        return value.setScale(VALUE_SCALE, RoundingMode.HALF_UP);
    }

    private BusinessException badRequest(String message)
    {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }
}
