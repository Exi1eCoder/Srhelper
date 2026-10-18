package com.quico.srhelper.service.impl;

import com.quico.srhelper.domain.SrLightconeExpUpgrade;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;
import com.quico.srhelper.domain.dto.SrLightconeExpUpgradeDTO;
import com.quico.srhelper.domain.vo.SrLightconeExpUpgradeVO;
import com.quico.srhelper.domain.vo.SrLightconeExpFullVO;
import com.quico.srhelper.mapper.SrLightconeExpUpgradeMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightconeAscensionTemplateMapper;
import com.quico.srhelper.service.ISrLightconeExpUpgradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 光锥经验升级Service实现类
 */
@Service
@RequiredArgsConstructor
public class SrLightconeExpUpgradeServiceImpl implements ISrLightconeExpUpgradeService {

    private final SrLightconeExpUpgradeMapper expUpgradeMapper;
    private final SrItemMapper srItemMapper;
    private final SrLightconeAscensionTemplateMapper ascensionTemplateMapper;

    /** 光锥升级经验书物品名 */
    private static final String EXP_ITEM_NAME = "稀薄以太";
    /** 信用点物品名 */
    private static final String CREDITS_ITEM_NAME = "信用点";

    @Override
    public List<SrLightconeExpUpgradeVO> selectAllExpUpgrades() {
        // 经验书/信用点物品固定，整表转换前只解析一次，避免逐行重复查库
        SrItem expItem = srItemMapper.selectSrItemByName(EXP_ITEM_NAME);
        SrItem creditsItem = srItemMapper.selectSrItemByName(CREDITS_ITEM_NAME);
        return expUpgradeMapper.selectAllOrderByStarAndLevel()
                .stream()
                .map(entity -> convertToVO(entity, expItem, creditsItem))
                .collect(Collectors.toList());
    }

    @Override
    public List<SrLightconeExpUpgradeVO> selectExpUpgradesByStarLevel(Integer starLevel) {
        SrItem expItem = srItemMapper.selectSrItemByName(EXP_ITEM_NAME);
        SrItem creditsItem = srItemMapper.selectSrItemByName(CREDITS_ITEM_NAME);
        return expUpgradeMapper.selectByStarLevel(starLevel)
                .stream()
                .map(entity -> convertToVO(entity, expItem, creditsItem))
                .collect(Collectors.toList());
    }

    @Override
    public SrLightconeExpUpgradeVO selectExpUpgradeById(Long id) {
        SrLightconeExpUpgrade entity = expUpgradeMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public SrLightconeExpUpgradeVO selectExpUpgradeByStarAndLevel(Integer starLevel, Integer level) {
        SrLightconeExpUpgrade entity = expUpgradeMapper.selectByStarAndLevel(starLevel, level);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public SrLightconeExpFullVO selectExpFullByStarLevel(Integer starLevel) {
        List<SrLightconeExpUpgradeVO> expUpgrades = selectExpUpgradesByStarLevel(starLevel);

        SrLightconeAscensionTemplate query = new SrLightconeAscensionTemplate();
        query.setTemplateType(String.valueOf(starLevel));
        List<SrLightconeAscensionTemplate> ascensionTemplates = ascensionTemplateMapper
                .selectSrLightconeAscensionTemplateList(query);

        SrLightconeExpFullVO vo = new SrLightconeExpFullVO();
        vo.setExpUpgrades(expUpgrades);
        vo.setAscensionTemplates(ascensionTemplates);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertExpUpgrade(SrLightconeExpUpgradeDTO dto) {
        SrLightconeExpUpgrade entity = SrLightconeExpUpgrade.builder()
                .starLevel(dto.getStarLevel())
                .minLevel(dto.getMinLevel())
                .maxLevel(dto.getMaxLevel())
                .expRequired(dto.getExpRequired())
                .creditsRequired(dto.getCreditsRequired())
                .build();
        return expUpgradeMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExpUpgrade(SrLightconeExpUpgradeDTO dto) {
        SrLightconeExpUpgrade entity = SrLightconeExpUpgrade.builder()
                .id(dto.getId())
                .starLevel(dto.getStarLevel())
                .minLevel(dto.getMinLevel())
                .maxLevel(dto.getMaxLevel())
                .expRequired(dto.getExpRequired())
                .creditsRequired(dto.getCreditsRequired())
                .build();
        return expUpgradeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpUpgradeById(Long id) {
        return expUpgradeMapper.deleteById(id);
    }

    private SrLightconeExpUpgradeVO convertToVO(SrLightconeExpUpgrade entity) {
        return convertToVO(entity,
                srItemMapper.selectSrItemByName(EXP_ITEM_NAME),
                srItemMapper.selectSrItemByName(CREDITS_ITEM_NAME));
    }

    private SrLightconeExpUpgradeVO convertToVO(SrLightconeExpUpgrade entity, SrItem expItem, SrItem creditsItem) {
        SrLightconeExpUpgradeVO vo = new SrLightconeExpUpgradeVO();
        vo.setId(entity.getId());
        vo.setStarLevel(entity.getStarLevel());
        vo.setMinLevel(entity.getMinLevel());
        vo.setMaxLevel(entity.getMaxLevel());
        vo.setExpRequired(entity.getExpRequired());
        vo.setCreditsRequired(entity.getCreditsRequired());

        // 经验书/信用点物品按名称精确匹配 sr_item（避免 LIKE 命中多条同名记录导致取值漂移）
        if (expItem != null) {
            vo.setExpItemId(expItem.getId());
            vo.setExpItemName(expItem.getItemName());
            vo.setExpItemImage(expItem.getImage());
        }

        if (creditsItem != null) {
            vo.setCreditsItemId(creditsItem.getId());
            vo.setCreditsItemName(creditsItem.getItemName());
            vo.setCreditsItemImage(creditsItem.getImage());
        }

        return vo;
    }
}
