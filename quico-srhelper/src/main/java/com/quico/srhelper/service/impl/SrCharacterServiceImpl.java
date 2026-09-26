package com.quico.srhelper.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.quico.common.core.domain.entity.SysDictData;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.DictUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;
import com.quico.srhelper.config.GachaRecordImageCacheManager;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.domain.SrCharacterBonusAbilityMaterial;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.SrCharacterSkillMaterial;
import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.dto.MaterialItemDTO;
import com.quico.srhelper.domain.dto.SrCharacterAscensionDTO;
import com.quico.srhelper.domain.dto.SrCharacterBonusDTO;
import com.quico.srhelper.domain.dto.SrCharacterSaveDTO;
import com.quico.srhelper.domain.dto.SrCharacterSkillDTO;
import com.quico.srhelper.domain.dto.SrCharacterStatBonusDTO;
import com.quico.srhelper.domain.enums.AscensionSkillTypeEnum;
import com.quico.srhelper.domain.vo.SrCharacterDetailVO;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterBonusAbilityMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrCharacterSkillMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterStatBonusMaterialMapper;
import com.quico.srhelper.service.helper.MaterialBindSyncService;
import com.quico.srhelper.service.helper.AscensionMaterialGenerator;
import com.quico.srhelper.service.helper.MaterialBindExpander;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.service.ISrCharacterService;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 角色Service业务层处理
 * 
 * @author quico
 * @date 2026-05-12
 */
@Service
@Slf4j
public class SrCharacterServiceImpl implements ISrCharacterService 
{
    @Autowired
    private SrCharacterMapper srCharacterMapper;

    @Autowired
    private SrCharacterAscensionMaterialMapper ascensionMapper;

    @Autowired
    private SrCharacterSkillMaterialMapper skillMapper;

    @Autowired
    private SrCharacterBonusAbilityMaterialMapper bonusAbilityMapper;

    @Autowired
    private SrCharacterStatBonusMaterialMapper statBonusMapper;

    @Autowired
    private SrCharacterMaterialBindMapper materialBindMapper;

    @Autowired
    private MaterialBindSyncService materialBindSyncService;

    @Autowired
    private AscensionMaterialGenerator ascensionMaterialGenerator;

    @Autowired
    private MaterialBindExpander materialBindExpander;

    @Autowired
    private ISrCharacterExpUpgradeService expUpgradeService;

    @Autowired
    private GachaRecordImageCacheManager imageCacheManager;

    /**
     * 查询角色
     * 
     * @param id 角色主键
     * @return 角色
     */
    @Override
    public SrCharacter selectSrCharacterById(Long id)
    {
        return srCharacterMapper.selectSrCharacterById(id);
    }

    /**
     * 查询角色列表
     * 
     * @param srCharacter 角色
     * @return 角色集合
     */
    @Override
    public List<SrCharacter> selectSrCharacterList(SrCharacter srCharacter)
    {
        return srCharacterMapper.selectSrCharacterList(srCharacter);
    }

    /**
     * 新增角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    @Override
    public int insertSrCharacter(SrCharacter srCharacter)
    {
        srCharacter.setCreateTime(DateUtils.getNowDate());
        srCharacter.setUpdateTime(DateUtils.getNowDate());
        return srCharacterMapper.insertSrCharacter(srCharacter);
    }

    /**
     * 修改角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    @Override
    public int updateSrCharacter(SrCharacter srCharacter)
    {
        srCharacter.setUpdateTime(DateUtils.getNowDate());
        int result = srCharacterMapper.updateSrCharacter(srCharacter);
        
        // 应用层同步：更新角色名称到材料绑定表
        materialBindSyncService.syncCharacterName(srCharacter.getId(), srCharacter.getCharacterName());
        
        return result;
    }

    /**
     * 删除角色
     * 
     * @param id 角色主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterById(Long id)
    {
        return srCharacterMapper.deleteSrCharacterById(id);
    }

    /**
     * 批量删除角色
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteSrCharacterByIds(Long[] ids)
    {
        return srCharacterMapper.deleteSrCharacterByIds(ids);
    }

    /**
     * 统一保存/更新角色（包含所有关联数据）
     * 采用增量更新策略：只更新变化的数据，不删除未传来的数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAll(SrCharacterSaveDTO dto) {
        Long characterId;
        Date now = DateUtils.getNowDate();
        boolean isNew = dto.getCharacter().getId() == null;

        // 1. 保存角色基本信息
        String currentUser = SecurityUtils.getUserId().toString();
        if (isNew) {
            dto.getCharacter().setCreateBy(currentUser);
            dto.getCharacter().setCreateTime(now);
            dto.getCharacter().setUpdateBy(currentUser);
            dto.getCharacter().setUpdateTime(now);
            // 防御性：仅当未预设时才使用当前时间戳；批量场景由调用方预设唯一值避免重复
            if (dto.getCharacter().getSortOrder() == null) {
                dto.getCharacter().setSortOrder(System.currentTimeMillis());
            }
            srCharacterMapper.insertSrCharacter(dto.getCharacter());
            characterId = dto.getCharacter().getId();
        } else {
            characterId = dto.getCharacter().getId();
            dto.getCharacter().setUpdateBy(currentUser);
            log.info("updateBy:{}", currentUser);
            dto.getCharacter().setUpdateTime(now);
            srCharacterMapper.updateSrCharacter(dto.getCharacter());
            // 应用层同步：更新角色名称到材料绑定表
            materialBindSyncService.syncCharacterName(dto.getCharacter().getId(), dto.getCharacter().getCharacterName());
        }

        // 刷新卡池项目图片缓存
        imageCacheManager.refresh();

        // simple 模式：仅更新基本信息，不操作材料表
        if ("simple".equals(dto.getChangeType()) || dto.getMaterialBinds() == null) {
            return characterId;
        }
        // 2. 保存材料绑定 + 3. 生成晋升材料（仅当有材料绑定数据时）
        if (dto.getMaterialBinds() != null && !dto.getMaterialBinds().isEmpty()) {
            saveMaterialBinds(characterId, dto.getMaterialBinds(), now);
            ascensionMaterialGenerator.generateAndSave(characterId, dto.getCharacter(), dto.getMaterialBinds());
        }
        return characterId;
    }

    /**
     * 保存材料绑定
     * 前端传回4个基础材料，其中TRA和CAL类型需要查询高阶素材并扩展为3条记录
     * 最终存储8条数据：TRA(3条) + CAL(3条) + SS(1条) + EOW(1条)
     */
    private void saveMaterialBinds(Long characterId, List<SrCharacterMaterialBind> materialBinds, Date now) {
        // 如果前端发送null，表示不更新材料绑定，保留现有数据
        if (materialBinds == null) {
            return;
        }
        // 发送空列表表示要清空材料绑定
        if (materialBinds.isEmpty()) {
            materialBindMapper.deleteByCharacterId(characterId);
            return;
        }

        // 删除旧数据
        materialBindMapper.deleteByCharacterId(characterId);

        // 扩展材料绑定（TRA和CAL需要扩展为高阶素材）
        List<SrCharacterMaterialBind> expandedBinds = expandMaterialBinds(materialBinds);

        // 批量插入
        String currentUser = SecurityUtils.getUserId().toString();
        int sortOrder = 0;
        for (SrCharacterMaterialBind bind : expandedBinds) {
            bind.setCharacterId(characterId);
            bind.setSortOrder(sortOrder++);
            bind.setCreateBy(currentUser);
            bind.setUpdateBy(currentUser);
            bind.setCreateTime(now);
            bind.setUpdateTime(now);
            materialBindMapper.insertSrCharacterMaterialBind(bind);
        }
    }

    /**
     * 扩展材料绑定（委托给 MaterialBindExpander）
     * TRA和CAL类型的材料通过seriesId扩展为同系列全部稀有度素材
     */
    private List<SrCharacterMaterialBind> expandMaterialBinds(List<SrCharacterMaterialBind> materialBinds) {
        List<SrCharacterMaterialBind> expanded = new ArrayList<>();

        for (SrCharacterMaterialBind bind : materialBinds) {
            if (materialBindExpander.isExpandable(bind.getItemAscensionType()) && bind.getSeriesId() != null) {
                List<SrItem> seriesItems = materialBindExpander.expandBySeriesId(bind.getSeriesId());
                for (SrItem item : seriesItems) {
                    expanded.add(SrCharacterMaterialBind.builder()
                            .characterId(bind.getCharacterId())
                            .characterName(bind.getCharacterName())
                            .itemAscensionType(bind.getItemAscensionType())
                            .itemId(item.getId())
                            .seriesId(bind.getSeriesId())
                            .itemName(item.getItemName())
                            .itemImage(item.getImage())
                            .rarityLevel(item.getStarLevel().intValue())
                            .sortOrder(0)
                            .build());
                }
            } else {
                expanded.add(bind);
            }
        }
        return expanded;
    }

    /**
     * 增量保存技能材料
     * 前端发送null表示不更新（保留现有数据），发送空列表或非空列表才处理
     * 优化：天赋(talent)、战技(skill)、终结技(ultimate)材料相同，编辑一个自动同步到其他两个
     */
    private void saveSkillsIncremental(Long characterId, List<SrCharacterSkillDTO> skills, Date now) {
        // 如果前端发送null，表示不更新技能材料，保留现有数据
        if (skills == null) {
            return;
        }
        // 发送空列表表示要清空技能材料
        if (skills.isEmpty()) {
            skillMapper.deleteByCharacterId(characterId);
            return;
        }
        
        // 删除旧数据（只删除需要同步的技能类型）
        // 先保存普攻数据，然后删除所有技能材料，最后重新插入
        List<SrCharacterSkillMaterial> basicAtkData = skillMapper.selectByCharacterIdAndType(characterId, "basic_atk");
        
        // 删除所有技能材料
        skillMapper.deleteByCharacterId(characterId);
        
        int sortOrder = 0;
        
        for (SrCharacterSkillDTO skill : skills) {
            String originalSkillType = skill.getSkillType();
            
            // 确定需要同步的技能类型
            List<String> skillTypesToSave = getSkillTypesToSync(originalSkillType);
            
            for (String skillType : skillTypesToSave) {
                int slot = 1;
                if (skill.getMaterials() != null && !skill.getMaterials().isEmpty()) {
                    for (MaterialItemDTO material : skill.getMaterials()) {
                        SrCharacterSkillMaterial entity = SrCharacterSkillMaterial.builder()
                                .characterId(characterId)
                                .skillType(skillType)
                                .targetLevel(skill.getTargetLevel())
                                .materialSlot(slot++)
                                .itemId(material.getItemId())
                                .quantity(material.getQuantity())
                                .sortOrder(sortOrder++)
                                .build();
                        entity.setCreateTime(now);
                        entity.setUpdateTime(now);
                        skillMapper.insert(entity);
                    }
                }
            }
        }
        
        // 重新插入普攻数据（如果存在）
        for (SrCharacterSkillMaterial basicAtk : basicAtkData) {
            basicAtk.setId(null); // 重置ID，作为新记录插入
            basicAtk.setCreateTime(now);
            basicAtk.setUpdateTime(now);
            skillMapper.insert(basicAtk);
        }
    }
    
    /**
     * 获取需要同步的技能类型列表
     * 在星穹铁道中：天赋、战技、终结技升级材料相同，普攻材料不同
     */
    private List<String> getSkillTypesToSync(String skillType) {
        // 天赋、战技、终结技互相同步
        if ("talent".equals(skillType) || "skill".equals(skillType) || "ultimate".equals(skillType)) {
            return List.of("talent", "skill", "ultimate");
        }
        // 普攻单独处理，不同步
        return List.of(skillType);
    }

    /**
     * 增量保存额外能力材料
     * 前端发送null表示不更新（保留现有数据），发送空列表或非空列表才处理
     */
    private void saveBonusAbilitiesIncremental(Long characterId, List<SrCharacterBonusDTO> bonusAbilities, Date now) {
        // 如果前端发送null，表示不更新额外能力材料，保留现有数据
        if (bonusAbilities == null) {
            return;
        }
        // 发送空列表表示要清空额外能力材料
        if (bonusAbilities.isEmpty()) {
            bonusAbilityMapper.deleteByCharacterId(characterId);
            return;
        }
        // 非空列表：删除后重新插入
        bonusAbilityMapper.deleteByCharacterId(characterId);
        int sortOrder = 0;
        for (SrCharacterBonusDTO bonus : bonusAbilities) {
            int slot = 1;
            if (bonus.getMaterials() != null && !bonus.getMaterials().isEmpty()) {
                for (MaterialItemDTO material : bonus.getMaterials()) {
                    SrCharacterBonusAbilityMaterial entity = SrCharacterBonusAbilityMaterial.builder()
                            .characterId(characterId)
                            .abilityIndex(bonus.getAbilityIndex())
                            .materialSlot(slot++)
                            .itemId(material.getItemId())
                            .quantity(material.getQuantity())
                            .sortOrder(sortOrder++)
                            .build();
                    entity.setCreateTime(now);
                    entity.setUpdateTime(now);
                    bonusAbilityMapper.insert(entity);
                }
            }
        }
    }

    /**
     * 增量保存额外属性材料
     * 前端发送null表示不更新（保留现有数据），发送空列表或非空列表才处理
     */
    private void saveStatBonusesIncremental(Long characterId, List<SrCharacterStatBonusDTO> statBonuses, Date now) {
        // 如果前端发送null，表示不更新额外属性材料，保留现有数据
        if (statBonuses == null) {
            return;
        }
        // 发送空列表表示要清空额外属性材料
        if (statBonuses.isEmpty()) {
            statBonusMapper.deleteByCharacterId(characterId);
            return;
        }
        // 非空列表：删除后重新插入
        statBonusMapper.deleteByCharacterId(characterId);
        int sortOrder = 0;
        for (SrCharacterStatBonusDTO statBonus : statBonuses) {
            int slot = 1;
            if (statBonus.getMaterials() != null && !statBonus.getMaterials().isEmpty()) {
                for (MaterialItemDTO material : statBonus.getMaterials()) {
                    SrCharacterStatBonusMaterial entity = SrCharacterStatBonusMaterial.builder()
                            .characterId(characterId)
                            .statIndex(statBonus.getStatIndex())
                            .materialSlot(slot++)
                            .itemId(material.getItemId())
                            .quantity(material.getQuantity())
                            .sortOrder(sortOrder++)
                            .build();
                    entity.setCreateTime(now);
                    entity.setUpdateTime(now);
                    statBonusMapper.insert(entity);
                }
            }
        }
    }

    /**
     * 保存晋升材料 - 先清空再批量插入
     * 前端发送null表示不更新（保留现有数据），发送空列表或非空列表才处理
     * 优化：战技(skill)、天赋(talent)、终结技(ultimate)材料相同，插入一个自动同步到其他两个
     * 所有类型统一以小写形式存入数据库
     * 使用批量插入优化性能（150+条数据从150次INSERT优化为1次批量INSERT）
     */
    private void saveAscensionsIncremental(Long characterId, List<SrCharacterAscensionDTO> ascensions, Date now) {
        // 如果前端发送null，表示不更新晋升材料，保留现有数据
        if (ascensions == null) {
            return;
        }
        // 发送空列表表示要清空晋升材料
        if (ascensions.isEmpty()) {
            ascensionMapper.deleteByCharacterId(characterId);
            return;
        }
        // 收集所有要插入的实体
        List<SrCharacterAscensionMaterial> entities = new ArrayList<>();
        int sortOrder = 0;

        // 统计各类型数量
        java.util.Map<String, Integer> typeCount = new java.util.HashMap<>();

        for (int i = 0; i < ascensions.size(); i++) {
            SrCharacterAscensionDTO ascension = ascensions.get(i);
            String originalType = ascension.getAscensionType();

            // 跳过 ascensionType 为 null 的数据
            if (originalType == null) {
                continue;
            }

            // 判断是否需要同步
            List<String> typesToSave = getAscensionTypesToSync(originalType);
            
            // 为每个需要同步的类型创建实体
            for (String ascensionType : typesToSave) {
                SrCharacterAscensionMaterial entity = SrCharacterAscensionMaterial.builder()
                        .characterId(characterId)
                        .ascensionType(ascensionType)
                        .breakLevel(ascension.getAscensionLevel())
                        .materialSlot(ascension.getMaterialSlot() != null ? ascension.getMaterialSlot() : 1)
                        .itemId(ascension.getItemId())
                        .quantity(ascension.getQuantity())
                        .rarityLevel(ascension.getRarityLevel() != null ? ascension.getRarityLevel() : 2)
                        .sortOrder(sortOrder++)
                        .build();
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                entities.add(entity);
                
                // 统计类型
                typeCount.merge(ascensionType, 1, Integer::sum);
            }
        }

        // 先删除再批量插入（性能优化：150次INSERT → 1次批量INSERT）
        ascensionMapper.deleteByCharacterId(characterId);
        if (!entities.isEmpty()) {
            ascensionMapper.insertBatch(entities);
        }
    }
    
    /**
     * 获取需要同步的晋升类型列表
     * 在星穹铁道中：战技(skill)、天赋(talent)、终结技(ultimate)升级材料相同
     * 忆灵技能同理
     */
    private List<String> getAscensionTypesToSync(String ascensionType) {
        if (ascensionType == null) {
            return List.of();
        }
        
        // 转换为小写进行比较
        AscensionSkillTypeEnum typeEnum = AscensionSkillTypeEnum.fromCode(ascensionType);
        
        // 如果枚举匹配，直接返回同步列表
            if (typeEnum != null) {
                // 战技、天赋、终结技互相同步
                if (typeEnum == AscensionSkillTypeEnum.SKILL 
                    || typeEnum == AscensionSkillTypeEnum.TALENT 
                    || typeEnum == AscensionSkillTypeEnum.ULTIMATE) {
                    return List.of(
                        AscensionSkillTypeEnum.SKILL.getCode(),
                        AscensionSkillTypeEnum.TALENT.getCode(),
                        AscensionSkillTypeEnum.ULTIMATE.getCode()
                    );
                }
                
                // 其他类型单独处理，不同步（包括忆灵技能、忆灵天赋、欢愉技）
                return List.of(typeEnum.getCode());
            }
        
        // 尝试将前端格式转换为数据库格式（BREAKTHROUGH -> levelasc 等）
        String dbCode = convertFrontendToDbCode(ascensionType);
        if (dbCode != null) {
            // 递归调用获取同步列表
            return getSyncListByDbCode(dbCode);
        }
        
        // 无法识别，返回原始值
        return List.of(ascensionType);
    }
    
    /**
     * 将前端发送的值转换为数据库存储的值
     * 处理驼峰命名转小写、特殊类型转换
     */
    private String convertFrontendToDbCode(String frontendCode) {
        if (frontendCode == null) {
            return null;
        }
        
        // 转换为小写
        String lowerCode = frontendCode.toLowerCase();
        
        // 特殊转换：level -> levelasc
        if ("level".equals(lowerCode)) {
            return AscensionSkillTypeEnum.LEVEL.getCode(); // "levelasc"
        }
        
        // 驼峰命名转换（前端可能发送 basicAtk, bonusAbility, statBonus）
        // 转换后：basicatk, bonusability, statbonus
        // 由于已经转小写，直接返回即可
        
        // 检查是否是有效的枚举值
        AscensionSkillTypeEnum typeEnum = AscensionSkillTypeEnum.fromCode(lowerCode);
        if (typeEnum != null) {
            return typeEnum.getCode();
        }
        
        // 无法识别，返回小写值
        return lowerCode;
    }
    
    /**
     * 根据数据库code获取同步列表
     */
    private List<String> getSyncListByDbCode(String dbCode) {
        // 战技、天赋、终结技互相同步
        if ("skill".equals(dbCode) || "talent".equals(dbCode) || "ultimate".equals(dbCode)) {
            return List.of("skill", "talent", "ultimate");
        }
        // 忆灵技能、忆灵天赋互相同步
        if ("memospriteskill".equals(dbCode) || "memospritetalent".equals(dbCode)) {
            return List.of("memospriteskill", "memospritetalent");
        }
        // 其他类型单独处理
        return List.of(dbCode);
    }

    /**
     * 查询角色完整信息（包含所有关联数据）
     * 所有材料数据都存储在 sr_character_ascension_material 表中，通过 ascension_type 区分类型
     */
    @Override
    public SrCharacterDetailVO getDetail(Long id) {
        SrCharacterDetailVO vo = new SrCharacterDetailVO();
        vo.setCharacter(srCharacterMapper.selectSrCharacterById(id));
        
        // 查询角色绑定的材料（世界掉落、拟造花萼、凝滞虚影、历战余响）
        vo.setMaterialBinds(materialBindMapper.selectByCharacterId(id));
        
        // 从晋升材料表查询所有数据
        List<SrCharacterAscensionMaterial> allMaterials = ascensionMapper.selectByCharacterId(id);

        // 按类型分类
        vo.setAscensions(allMaterials.stream()
                .filter(m -> m.isLevelType())
                .collect(java.util.stream.Collectors.toList()));
        
        vo.setSkills(allMaterials.stream()
                .filter(m -> m.isSkillType())
                .collect(java.util.stream.Collectors.toList()));
        
        vo.setBonusAbilities(allMaterials.stream()
                .filter(m -> AscensionSkillTypeEnum.BONUS_ABILITY.getCode().equals(m.getAscensionType()))
                .collect(java.util.stream.Collectors.toList()));
        
        vo.setStatBonuses(allMaterials.stream()
                .filter(m -> AscensionSkillTypeEnum.STAT_BONUS.getCode().equals(m.getAscensionType()))
                .collect(java.util.stream.Collectors.toList()));

        // 角色升级经验（全局通用）
        vo.setExpUpgrades(expUpgradeService.selectAllExpUpgrades());

        return vo;
    }

    /**
     * 删除角色材料配置（不删除角色主表）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCharacter(Long id) {
        // 只删除四张材料表，不删除角色表
        ascensionMapper.deleteByCharacterId(id);
        skillMapper.deleteByCharacterId(id);
        bonusAbilityMapper.deleteByCharacterId(id);
        statBonusMapper.deleteByCharacterId(id);
    }

    /**
     * 批量保存角色（包含所有关联数据）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveBatch(List<SrCharacterSaveDTO> dtoList) {
        List<Long> characterIds = new ArrayList<>();
        // 批量场景统一预设 sortOrder，避免 saveAll 各自调用 currentTimeMillis 在同毫秒内重复
        long baseTimestamp = System.currentTimeMillis();
        for (int i = 0; i < dtoList.size(); i++) {
            SrCharacterSaveDTO dto = dtoList.get(i);
            if (dto.getCharacter() != null
                    && dto.getCharacter().getId() == null
                    && dto.getCharacter().getSortOrder() == null) {
                // baseTimestamp + i 保证同一批内唯一；i 远小于 1000，不会与下一毫秒冲突
                dto.getCharacter().setSortOrder(baseTimestamp + i);
            }
            Long characterId = saveAll(dto);
            characterIds.add(characterId);
        }
        return characterIds;
    }

    /**
     * 导入角色数据（按角色名字 + 实装版本去重）
     * 参考 SysUserServiceImpl.importUser / SrGachaRecordServiceImpl.importGachaRecord 写法。
     * saveAll/saveBatch 内部 createBy/updateBy 由 SecurityContext 获取，与 operName 一致。
     *
     * @param list          从 Excel 解析的角色列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName      操作人ID（保留参数以与 RuoYi 标准接口签名一致，便于异步导入扩展）
     * @return 导入结果消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importCharacter(List<SrCharacter> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        List<SrCharacter> toInsert = new ArrayList<>();
        List<SrCharacter> toUpdate = new ArrayList<>();
        int skipNum = 0;

        for (SrCharacter character : list)
        {
            // 校验必填字段：角色名字
            if (StringUtils.isEmpty(character.getCharacterName()))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、角色名字不能为空");
                continue;
            }

            // 校验实装版本：允许为空；非空时必须命中字典 sr_release_version 的有效 dictValue
            // Excel 数字单元格中的 "2.0" 经 POI 整数格式化后会丢失小数变为 "2"，
            // 校验前先按字典把数值形态规整回文本形态（"2" → "2.0"），版本全程按文本处理
            character.setReleaseVersion(matchVersionByDict(character.getReleaseVersion()));
            if (StringUtils.isNotEmpty(character.getReleaseVersion())
                    && StringUtils.isEmpty(DictUtils.getDictLabel("sr_release_version", character.getReleaseVersion())))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、角色[").append(character.getCharacterName()).append("]实装版本错误，导入失败");
                continue;
            }

            // 按角色名字 + 实装版本精确查询是否已存在
            SrCharacter existing = srCharacterMapper.selectSrCharacterByNameAndVersion(
                    character.getCharacterName(), character.getReleaseVersion());

            if (existing != null)
            {
                if (updateSupport)
                {
                    // 复用已存在记录的 id，走 mapper 更新路径
                    character.setId(existing.getId());
                    toUpdate.add(character);
                }
                else
                {
                    skipNum++;
                }
            }
            else
            {
                // 新增：清空 id 防止 Excel 中误带
                character.setId(null);
                toInsert.add(character);
            }
        }

        // 直接走 mapper，不经过 saveAll/SrCharacterSaveDTO（参照 SrItemServiceImpl.importItem 的简单模式）
        int successNum = 0;
        int updateNum = 0;
        if (!toInsert.isEmpty())
        {
            // 批量场景统一预设 sortOrder，避免同毫秒重复
            long baseTimestamp = System.currentTimeMillis();
            for (int i = 0; i < toInsert.size(); i++)
            {
                SrCharacter c = toInsert.get(i);
                if (c.getSortOrder() == null)
                {
                    c.setSortOrder(baseTimestamp + i);
                }
                srCharacterMapper.insertSrCharacter(c);
            }
            successNum = toInsert.size();
        }
        if (!toUpdate.isEmpty())
        {
            for (SrCharacter c : toUpdate)
            {
                srCharacterMapper.updateSrCharacter(c);
            }
            updateNum = toUpdate.size();
        }

        // 构建返回消息
        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("导入完成：新增 ").append(successNum).append(" 条");
        if (updateNum > 0)
        {
            resultMsg.append("，更新 ").append(updateNum).append(" 条");
        }
        if (skipNum > 0)
        {
            resultMsg.append("，跳过重复 ").append(skipNum).append(" 条");
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "，" + failureNum + " 条数据格式不正确：");
            resultMsg.append(failureMsg);
        }

        return resultMsg.toString();
    }

    /**
     * 按字典将数值形态的版本号规整为文本形态（"2" → "2.0"）
     * Excel 数字单元格中的 "2.0" 经 POI 整数格式化后丢失小数变为 "2"，与字典文本值无法直接匹配，
     * 此处按数值相等在字典 sr_release_version 中找回真实文本值；非数字输入或未命中时原样返回
     *
     * @param version 导入的版本号
     * @return 字典中的文本形态版本号；未命中时返回原值，交由字典校验报错
     */
    private String matchVersionByDict(String version)
    {
        if (StringUtils.isEmpty(version))
        {
            return version;
        }
        try
        {
            BigDecimal input = new BigDecimal(version.trim());
            List<SysDictData> datas = DictUtils.getDictCache("sr_release_version");
            if (datas != null)
            {
                for (SysDictData dict : datas)
                {
                    if (StringUtils.isEmpty(dict.getDictValue()))
                    {
                        continue;
                    }
                    try
                    {
                        if (input.compareTo(new BigDecimal(dict.getDictValue().trim())) == 0)
                        {
                            return dict.getDictValue();
                        }
                    }
                    catch (NumberFormatException ignore)
                    {
                    }
                }
            }
        }
        catch (NumberFormatException ignore)
        {
        }
        return version;
    }
}