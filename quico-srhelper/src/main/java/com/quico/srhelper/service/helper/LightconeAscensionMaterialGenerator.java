package com.quico.srhelper.service.helper;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.quico.common.utils.DateUtils;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.enums.CommonItemEnum;
import com.quico.srhelper.mapper.SrLightConeAscensionMapper;
import com.quico.srhelper.mapper.SrLightconeAscensionTemplateMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 光锥晋升材料生成器
 * 从 sr_lightcone_ascension_template 模板表读取数据，
 * 根据光锥星级选择模板，替换占位符为真实物品 ID，
 * 批量写入 sr_lightcone_ascension_material 表
 */
@Slf4j
@Component
public class LightconeAscensionMaterialGenerator {

    @Autowired
    private SrLightconeAscensionTemplateMapper templateMapper;

    @Autowired
    private SrLightconeMaterialBindMapper materialBindMapper;

    @Autowired
    private SrLightConeAscensionMapper ascensionMapper;

    @Autowired
    private SrItemMapper itemMapper;

    /**
     * 根据光锥信息生成晋升材料并写入数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateAndSave(Long lightConeId, SrLightCones lightCone,
                                List<SrLightconeMaterialBind> materialBinds) {
        // 1. 确定模板类型（3星→"3", 4星→"4", 5星→"5"）
        String templateType = String.valueOf(lightCone.getStarLevel());

        // 2. 加载模板数据
        SrLightconeAscensionTemplate query = new SrLightconeAscensionTemplate();
        query.setTemplateType(templateType);
        List<SrLightconeAscensionTemplate> templates = templateMapper
                .selectSrLightconeAscensionTemplateList(query);

        // 3. 构建占位符映射
        Map<String, Long> placeholderMap = buildPlaceholderMap(lightConeId);
        Map<Long, Integer> itemRarityMap = buildItemRarityMap(lightConeId);

        // 4. 转换为真实材料
        List<SrLightConeAscension> materials = new ArrayList<>();
        Date now = DateUtils.getNowDate();
        int sortOrder = 0;
        for (SrLightconeAscensionTemplate tpl : templates) {
            Long realItemId = resolvePlaceholder(tpl.getItemId(), placeholderMap);
            if (realItemId == null) {
                log.warn("光锥[{}]无法解析占位符: {} (templateType={}, breakLevel={})",
                        lightCone.getLightConeName(), tpl.getItemId(), templateType, tpl.getBreakLevel());
                continue;
            }
            int rarity = itemRarityMap.getOrDefault(realItemId, 2);

            SrLightConeAscension material = SrLightConeAscension.builder()
                    .lightConeId(lightConeId)
                    .ascensionPhase(tpl.getBreakLevel() != null ? tpl.getBreakLevel().intValue() : 0)
                    .breakLevel(tpl.getBreakLevel() != null ? tpl.getBreakLevel().intValue() : 0)
                    .materialSlot(tpl.getMaterialSlot() != null ? tpl.getMaterialSlot().intValue() : 1)
                    .itemId(realItemId)
                    .rarityLevel(rarity)
                    .quantity(tpl.getQuantity() != null ? tpl.getQuantity().intValue() : 0)
                    .sortOrder(sortOrder++)
                    .build();
            material.setCreateTime(now);
            materials.add(material);
        }

        // 5. 先删后插
        ascensionMapper.deleteSrLightConeAscensionByConeId(lightConeId);
        if (!materials.isEmpty()) {
            ascensionMapper.insertBatch(materials);
        }

        log.info("光锥[{}]({})晋升材料生成完成，星级={}，共{}条",
                lightCone.getLightConeName(), lightConeId, templateType, materials.size());
        return materials.size();
    }

    // ======================== 占位符解析（与角色共用相同逻辑） ========================

    private Map<String, Long> buildPlaceholderMap(Long lightConeId) {
        Map<String, Long> map = new HashMap<>();
        List<SrLightconeMaterialBind> allBinds = materialBindMapper.selectByLightConeId(lightConeId);

        map.put("Credit", CommonItemEnum.CREDIT.getItemId());

        for (SrLightconeMaterialBind bind : allBinds) {
            String type = bind.getItemAscensionType();
            Integer rarity = bind.getRarityLevel();
            Long itemId = bind.getItemId();
            if (type == null || itemId == null) continue;

            if (("TRA".equalsIgnoreCase(type) || "CAL".equalsIgnoreCase(type)) && rarity != null) {
                map.put(type.toUpperCase() + "_" + rarity, itemId);
            }
            if ("SS".equalsIgnoreCase(type)) {
                map.put("SS", itemId);
                map.put("SS_4", itemId);
            }
            if ("EOW".equalsIgnoreCase(type)) {
                map.put("EOW", itemId);
                map.put("EOW_4", itemId);
            }
            if ("TOD".equalsIgnoreCase(type)) {
                map.put("TOD", itemId);
                map.put("TOD_4", itemId);
            }

            if (bind.getSeriesId() != null
                    && ("TRA".equalsIgnoreCase(type) || "CAL".equalsIgnoreCase(type))) {
                List<SrItem> seriesItems = itemMapper.selectBySeriesId(bind.getSeriesId());
                for (SrItem item : seriesItems) {
                    if (item.getStarLevel() != null) {
                        map.put(type.toUpperCase() + "_" + item.getStarLevel(), item.getId());
                    }
                }
            }
        }
        return map;
    }

    private Map<Long, Integer> buildItemRarityMap(Long lightConeId) {
        Map<Long, Integer> map = new HashMap<>();
        map.put(CommonItemEnum.CREDIT.getItemId(), 3);

        List<SrLightconeMaterialBind> allBinds = materialBindMapper.selectByLightConeId(lightConeId);
        for (SrLightconeMaterialBind bind : allBinds) {
            if (bind.getItemId() == null || bind.getRarityLevel() == null) continue;
            map.put(bind.getItemId(), bind.getRarityLevel());
            if (bind.getSeriesId() != null) {
                List<SrItem> seriesItems = itemMapper.selectBySeriesId(bind.getSeriesId());
                for (SrItem item : seriesItems) {
                    if (item.getStarLevel() != null) {
                        map.put(item.getId(), item.getStarLevel().intValue());
                    }
                }
            }
        }
        return map;
    }

    private Long resolvePlaceholder(String placeholder, Map<String, Long> map) {
        if (placeholder == null || placeholder.isEmpty()) return null;
        Long resolved = map.get(placeholder);
        if (resolved != null) return resolved;
        resolved = map.get(placeholder.toUpperCase());
        if (resolved != null) return resolved;
        try {
            return Long.parseLong(placeholder);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
