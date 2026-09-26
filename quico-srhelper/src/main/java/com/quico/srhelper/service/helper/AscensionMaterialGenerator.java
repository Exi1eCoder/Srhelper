package com.quico.srhelper.service.helper;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.quico.common.utils.DateUtils;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.enums.CommonItemEnum;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterAscensionTemplateMapper;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 晋升材料生成器
 * 从 sr_character_ascension_template 模板表读取数据，
 * 根据角色星级和命途展开模板，替换占位符为真实物品 ID，
 * 批量写入 sr_character_ascension_material 表
 */
@Slf4j
@Component
public class AscensionMaterialGenerator {

    /** 五星常规角色模板 */
    private static final String TPL_FIVE_STAR_NORMAL = "FiveStarNormal";
    /** 四星常规角色模板 */
    private static final String TPL_FOUR_STAR_NORMAL = "FourStarNormal";
    /** 记忆命途模板 */
    private static final String TPL_REMEMBRANCE = "Remembrance";
    /** 欢愉命途模板 */
    private static final String TPL_ELATION = "Elation";

    // 固定物品使用 CommonItemEnum 统一管理：CREDIT(32L), TOD(70L)

    @Autowired
    private SrCharacterAscensionTemplateMapper templateMapper;

    @Autowired
    private SrCharacterMaterialBindMapper materialBindMapper;

    @Autowired
    private SrCharacterAscensionMaterialMapper ascensionMaterialMapper;

    @Autowired
    private SrItemMapper itemMapper;

    /**
     * 根据角色信息生成晋升材料并写入数据库
     * 
     * @param characterId   角色ID
     * @param character     角色基本信息
     * @param materialBinds 该角色绑定的材料列表（已存入库）
     * @return 生成的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateAndSave(Long characterId, SrCharacter character,
                                List<SrCharacterMaterialBind> materialBinds) {
        // 1. 确定使用哪个模板
        String templateType = resolveTemplateType(character);

        // 2. 加载模板数据
        List<SrCharacterAscensionTemplate> templates = templateMapper.selectByTemplateType(templateType);

        // 3. 按命途展开模板（skill→talent/ultimate, basicAtk→memosprite, etc.）
        List<SrCharacterAscensionTemplate> expanded = expandTemplates(templates, character, templateType);

        // 4. 加载材料绑定用于占位符替换 + itemId→rarity 映射
        Map<String, Long> placeholderMap = buildPlaceholderMap(characterId);
        Map<Long, Integer> itemRarityMap = buildItemRarityMap(characterId);

        // 5. 转换为真实材料记录
        List<SrCharacterAscensionMaterial> materials = new ArrayList<>();
        Date now = DateUtils.getNowDate();
        int sortOrder = 0;
        for (SrCharacterAscensionTemplate tpl : expanded) {
            String placeholder = tpl.getItemId();
            Long realItemId = resolvePlaceholder(placeholder, placeholderMap);

            // rarity从绑定表取，不以模板为准
            int rarity = itemRarityMap.getOrDefault(realItemId, 2);

            SrCharacterAscensionMaterial material = SrCharacterAscensionMaterial.builder()
                    .characterId(characterId)
                    .ascensionType(tpl.getAscensionType())
                    .breakLevel(tpl.getBreakLevel() != null ? tpl.getBreakLevel().intValue() : 0)
                    .materialSlot(tpl.getMaterialSlot() != null ? tpl.getMaterialSlot().intValue() : 1)
                    .itemId(realItemId)
                    .quantity(tpl.getQuantity() != null ? tpl.getQuantity().intValue() : 0)
                    .rarityLevel(rarity)
                    .expRequired(0L)
                    .sortOrder(sortOrder++)
                    .build();
            material.setCreateTime(now);
            materials.add(material);
        }

        // 6. 先删后插
        ascensionMaterialMapper.deleteByCharacterId(characterId);
        if (!materials.isEmpty()) {
            ascensionMaterialMapper.insertBatch(materials);
        }

        log.info("角色[{}]({})晋升材料生成完成，模板={}，共{}条",
                character.getCharacterName(), characterId, templateType, materials.size());
        return materials.size();
    }

    // ======================== 模板类型解析 ========================

    /**
     * 根据角色星级和命途确定使用哪个模板
     */
    private String resolveTemplateType(SrCharacter character) {
        Long starLevel = character.getStarLevel();
        String path = character.getPath();

        if (starLevel != null && starLevel <= 4) {
            return TPL_FOUR_STAR_NORMAL;
        }
        // 五星及以上
        if (path != null) {
            String lowerPath = path.toLowerCase();
            if (lowerPath.contains("memory") || lowerPath.contains("remembrance") || lowerPath.contains("记忆")) {
                return TPL_REMEMBRANCE;
            }
            if (lowerPath.contains("elation") || lowerPath.contains("欢愉")) {
                return TPL_ELATION;
            }
        }
        return TPL_FIVE_STAR_NORMAL;
    }

    // ======================== 模板展开 ========================

    /**
     * 根据命途展开模板
     * - 所有角色：skill → talent + ultimate
     * - 记忆：在基础上，basicatk → memospriteskill + memospritetalent
     * - 欢愉：在基础上，skill → elationskill
     */
    private List<SrCharacterAscensionTemplate> expandTemplates(
            List<SrCharacterAscensionTemplate> templates, SrCharacter character, String templateType) {

        List<SrCharacterAscensionTemplate> result = new ArrayList<>(templates);

        // 通用展开：skill → talent + ultimate（四星和五星都需要）
        expandSkillToTalentAndUltimate(templates, result);

        // 记忆命途：basicatk → memospriteskill + memospritetalent
        if (TPL_REMEMBRANCE.equals(templateType)) {
            expandBasicAtkToMemosprite(templates, result);
        }

        // 欢愉命途：skill → elationskill
        if (TPL_ELATION.equals(templateType)) {
            expandSkillToElation(templates, result);
        }

        return result;
    }

    /**
     * 通用展开：战技(skill) → 天赋(talent) + 终结技(ultimate)
     * 四星和五星角色都需要
     */
    private void expandSkillToTalentAndUltimate(List<SrCharacterAscensionTemplate> originals,
                                                 List<SrCharacterAscensionTemplate> result) {
        for (SrCharacterAscensionTemplate tpl : originals) {
            if ("skill".equalsIgnoreCase(tpl.getAscensionType())) {
                // 复制为天赋
                result.add(copyWithNewType(tpl, "talent"));
                // 复制为终结技
                result.add(copyWithNewType(tpl, "ultimate"));
            }
        }
    }

    /**
     * 记忆命途：普攻(basicatk) → 忆灵技(memospriteskill) + 忆灵天赋(memospritetalent)
     */
    private void expandBasicAtkToMemosprite(List<SrCharacterAscensionTemplate> originals,
                                             List<SrCharacterAscensionTemplate> result) {
        for (SrCharacterAscensionTemplate tpl : originals) {
            if ("basicatk".equalsIgnoreCase(tpl.getAscensionType())) {
                result.add(copyWithNewType(tpl, "memospriteskill"));
                result.add(copyWithNewType(tpl, "memospritetalent"));
            }
        }
    }

    /**
     * 欢愉命途：战技(skill) → 欢愉技(elationskill)
     */
    private void expandSkillToElation(List<SrCharacterAscensionTemplate> originals,
                                       List<SrCharacterAscensionTemplate> result) {
        for (SrCharacterAscensionTemplate tpl : originals) {
            if ("skill".equalsIgnoreCase(tpl.getAscensionType())) {
                result.add(copyWithNewType(tpl, "elationskill"));
            }
        }
    }

    /**
     * 复制模板记录并替换 ascensionType
     */
    private SrCharacterAscensionTemplate copyWithNewType(SrCharacterAscensionTemplate source, String newType) {
        SrCharacterAscensionTemplate copy = new SrCharacterAscensionTemplate();
        copy.setId(null);
        copy.setTemplateType(source.getTemplateType());
        copy.setAscensionType(newType);
        copy.setBreakLevel(source.getBreakLevel());
        copy.setMaterialSlot(source.getMaterialSlot());
        copy.setItemId(source.getItemId());
        copy.setQuantity(source.getQuantity());
        copy.setRarityLevel(source.getRarityLevel());
        copy.setSortOrder(source.getSortOrder());
        return copy;
    }

    // ======================== 占位符解析 ========================

    /**
     * 构建占位符 → 真实 item_id 的映射表
     * 优先从 material_bind 取，缺失时通过 seriesId 从 sr_item 补充
     */
    private Map<String, Long> buildPlaceholderMap(Long characterId) {
        Map<String, Long> map = new HashMap<>();
        List<SrCharacterMaterialBind> allBinds = materialBindMapper.selectByCharacterId(characterId);

        // Credit / TOD 固定映射（全球通用）
        map.put("Credit", CommonItemEnum.CREDIT.getItemId());
        map.put("TOD", CommonItemEnum.TOD.getItemId());
        map.put("TOD_4", CommonItemEnum.TOD.getItemId());

        for (SrCharacterMaterialBind bind : allBinds) {
            String type = bind.getItemAscensionType();
            Integer rarity = bind.getRarityLevel();
            Long itemId = bind.getItemId();


            if (type == null || itemId == null) continue;

            // TRA_N / CAL_N
            if (("TRA".equalsIgnoreCase(type) || "CAL".equalsIgnoreCase(type)) && rarity != null) {
                String key = type.toUpperCase() + "_" + rarity;
                map.put(key, itemId);
            }
            // SS
            if ("SS".equalsIgnoreCase(type)) {
                map.put("SS", itemId);
                map.put("SS_4", itemId);
            }
            // EOW（随角色变动，从绑表取）
            if ("EOW".equalsIgnoreCase(type)) {
                map.put("EOW", itemId);
                map.put("EOW_4", itemId);
            }

            // 通过 seriesId 扩展同一系列的所有稀有度（覆盖 bind 中可能缺失的稀有度）
            if (bind.getSeriesId() != null
                    && ("TRA".equalsIgnoreCase(type) || "CAL".equalsIgnoreCase(type))) {
                List<SrItem> seriesItems = itemMapper.selectBySeriesId(bind.getSeriesId());
                for (SrItem item : seriesItems) {
                    String sKey = type.toUpperCase() + "_" + item.getStarLevel();
                    if (item.getStarLevel() != null) {
                        map.put(sKey, item.getId());
                    }
                }
            }
        }

        return map;
    }

    /**
     * 构建 itemId → rarityLevel 的反向映射（从绑定表和系列表取，不以模板为准）
     */
    private Map<Long, Integer> buildItemRarityMap(Long characterId) {
        Map<Long, Integer> map = new HashMap<>();
        List<SrCharacterMaterialBind> allBinds = materialBindMapper.selectByCharacterId(characterId);

        // Credit / TOD 固定稀有度
        map.put(CommonItemEnum.CREDIT.getItemId(), 3);   // 信用点 3星
        map.put(CommonItemEnum.TOD.getItemId(), 5);      // TOD 5星

        for (SrCharacterMaterialBind bind : allBinds) {
            if (bind.getItemId() == null || bind.getRarityLevel() == null) continue;
            map.put(bind.getItemId(), bind.getRarityLevel());

            // 通过 seriesId 扩展同系列物品的稀有度
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

    /**
     * 将占位符转换为真实 item_id
     * 
     * @param placeholder 模板中的 item_id（可能是占位符如 "TRA_2"，也可能是数字ID如 "32"）
     * @param map         占位符映射表
     * @return 真实 item_id，解析失败返回 null
     */
    private Long resolvePlaceholder(String placeholder, Map<String, Long> map) {
        if (placeholder == null || placeholder.isEmpty()) {
            return null;
        }

        // 先尝试占位符映射
        Long resolved = map.get(placeholder);
        if (resolved != null) {
            return resolved;
        }
        // 兼容大小写
        resolved = map.get(placeholder.toUpperCase());
        if (resolved != null) {
            return resolved;
        }

        // 尝试解析为纯数字（模板中直接使用数字 ID 的情况）
        try {
            return Long.parseLong(placeholder);
        } catch (NumberFormatException e) {
            log.warn("占位符 [{}] 既不是已知占位符也不是有效数字 ID", placeholder);
            return null;
        }
    }
}
