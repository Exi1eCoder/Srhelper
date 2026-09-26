package com.quico.srhelper.service.impl;

import com.quico.srhelper.config.SrhelperCacheConstants;
import com.quico.common.core.redis.RedisCache;
import com.quico.srhelper.domain.enums.AscensionTypeEnum;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.domain.SrCharacterBonusAbilityMaterial;
import com.quico.srhelper.domain.SrCharacterExpUpgrade;
import com.quico.srhelper.domain.SrCharacterSkillMaterial;
import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.domain.vo.SrCharacterCultivationVO;
import com.quico.srhelper.domain.vo.SrCultivationCalcVO;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterBonusAbilityMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterExpUpgradeMapper;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterSkillMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterStatBonusMaterialMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrUserCharacterMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.service.ISrCultivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 养成计算Service实现类
 */
@Service
@RequiredArgsConstructor
public class SrCultivationServiceImpl implements ISrCultivationService {
    
    private final SrUserCharacterMapper userCharacterMapper;
    private final SrCharacterMapper characterMapper;
    private final SrCharacterAscensionMaterialMapper ascensionMaterialMapper;
    private final SrCharacterSkillMaterialMapper skillMaterialMapper;
    private final SrCharacterBonusAbilityMaterialMapper bonusAbilityMaterialMapper;
    private final SrCharacterStatBonusMaterialMapper statBonusMaterialMapper;
    private final SrCharacterExpUpgradeMapper expUpgradeMapper;
    private final SrItemMapper itemMapper;
    private final SrUserItemMapper userItemMapper;
    private final RedisCache redisCache;
    
    private static final Integer MAX_LEVEL = 80;
    private static final Integer MAX_SKILL_LEVEL = 10;
    private static final Integer MAX_BASIC_ATK_LEVEL = 6;
    private static final Long EXP_PER_BOOK = 1000L; // 每本旅行见闻提供的经验值
    
    // 基础材料名称（从数据库sr_item表获取）
    private static final String EXP_BOOK_NAME = "旅行见闻";
    private static final String CREDITS_NAME = "信用点";
    
    // 缓存相关常量
    private static final String CULTIVATION_CACHE_PREFIX = SrhelperCacheConstants.CULTIVATION_KEY;
    private static final String CULTIVATION_TOTAL_KEY = CULTIVATION_CACHE_PREFIX + "total:%d:%d"; // userId:targetLevel
    private static final String CULTIVATION_CHARACTER_KEY = CULTIVATION_CACHE_PREFIX + "character:%d"; // userCharacterId
    
    @Override
    public SrCultivationCalcVO calculateTotalCultivation(Long userId, Integer targetLevel) {
        if (targetLevel == null || targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        
        // 构建缓存key
        String cacheKey = String.format(CULTIVATION_TOTAL_KEY, userId, targetLevel);
        
        // 尝试从缓存获取
        SrCultivationCalcVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        SrCultivationCalcVO result = new SrCultivationCalcVO();
        
        // 查询用户所有角色
        SrUserCharacter query = new SrUserCharacter();
        query.setUserId(userId);
        List<SrUserCharacter> userCharacters = userCharacterMapper.selectSrUserCharacterList(query);
        
        // 分类：未完成培养和已完成培养
        List<SrCultivationCalcVO.CharacterInfo> normalCharacters = new ArrayList<>();
        List<SrCultivationCalcVO.CharacterInfo> completedCharacters = new ArrayList<>();
        
        // 计算总体材料需求
        Map<Long, Long> totalMaterialMap = new HashMap<>();
        
        for (SrUserCharacter uc : userCharacters) {
            SrCharacter character = characterMapper.selectSrCharacterById(uc.getCharacterId());
            if (character == null) continue;
            
            SrCultivationCalcVO.CharacterInfo info = new SrCultivationCalcVO.CharacterInfo();
            info.setUserCharacterId(uc.getId());
            info.setCharacterId(uc.getCharacterId());
            info.setCharacterName(character.getCharacterName());
            info.setCharacterImage(character.getAvatar());
            info.setCurrentLevel(uc.getLevel() != null ? uc.getLevel().intValue() : 1);
            info.setTargetLevel(targetLevel);
            
            // 计算该角色所有需要的材料
            Map<Long, Long> characterMaterials = calculateAllMaterialsForCharacter(uc, targetLevel);
            
            // 判断是否已完成培养（没有需要的材料即为已完成培养）
            boolean isCompleted = characterMaterials.isEmpty();
            info.setIsMaxLevel(isCompleted);
            
            if (isCompleted) {
                completedCharacters.add(info);
            } else {
                normalCharacters.add(info);
                
                // 累加到总体材料
                for (Map.Entry<Long, Long> entry : characterMaterials.entrySet()) {
                    totalMaterialMap.merge(entry.getKey(), entry.getValue(), Long::sum);
                }
            }
        }
        
        // 添加"总体"到角色列表首位
        SrCultivationCalcVO.CharacterInfo totalInfo = new SrCultivationCalcVO.CharacterInfo();
        totalInfo.setUserCharacterId(0L);
        totalInfo.setCharacterId(0L);
        totalInfo.setCharacterName("总体");
        totalInfo.setCharacterImage("");
        totalInfo.setIsMaxLevel(false);
        
        List<SrCultivationCalcVO.CharacterInfo> allCharacters = new ArrayList<>();
        allCharacters.add(totalInfo);
        allCharacters.addAll(normalCharacters);
        
        result.setCharacters(allCharacters);
        result.setMaxLevelCharacters(completedCharacters);
        
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(userId);
        
        // 构建材料需求列表
        List<SrCultivationCalcVO.MaterialRequirement> materialList = buildMaterialList(totalMaterialMap, userItemMap);
        result.setTotalMaterials(materialList);
        
        // 缓存计算结果，有效期30分钟
        redisCache.setCacheObject(cacheKey, result, (int) SrhelperCacheConstants.TTL_CALC, TimeUnit.MINUTES);
        
        return result;
    }
    
    @Override
    public SrCharacterCultivationVO calculateCharacterCultivation(Long userCharacterId, Integer targetLevel) {
        if (targetLevel == null || targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        
        // 构建缓存key
        String cacheKey = String.format(CULTIVATION_CHARACTER_KEY, userCharacterId);
        
        // 尝试从缓存获取
        SrCharacterCultivationVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        SrUserCharacter uc = userCharacterMapper.selectSrUserCharacterById(userCharacterId);
        if (uc == null) {
            return null;
        }
        
        SrCharacter character = characterMapper.selectSrCharacterById(uc.getCharacterId());
        if (character == null) {
            return null;
        }
        
        SrCharacterCultivationVO result = new SrCharacterCultivationVO();
        
        // 角色信息
        SrCharacterCultivationVO.CharacterInfo info = new SrCharacterCultivationVO.CharacterInfo();
        info.setUserCharacterId(uc.getId());
        info.setCharacterId(uc.getCharacterId());
        info.setCharacterName(character.getCharacterName());
        info.setCharacterImage(character.getAvatar());
        info.setCurrentLevel(uc.getLevel() != null ? uc.getLevel().intValue() : 1);
        info.setTargetLevel(targetLevel);
        result.setCharacter(info);
        
        // 计算材料需求（所有养成内容）
        Map<Long, Long> materialMap = calculateAllMaterialsForCharacter(uc, targetLevel);
        
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(uc.getUserId());
        
        // 构建材料需求列表
        List<SrCharacterCultivationVO.MaterialRequirement> materialList = buildCharacterMaterialList(materialMap, userItemMap);
        result.setMaterials(materialList);
        
        // 缓存计算结果，有效期30分钟
        redisCache.setCacheObject(cacheKey, result, (int) SrhelperCacheConstants.TTL_CALC, java.util.concurrent.TimeUnit.MINUTES);
        
        return result;
    }
    
    /**
     * 计算单个角色的所有养成材料
     * 所有材料都存储在 sr_character_ascension_material 表中
     * 简化逻辑：不考虑解锁顺序，直接计算从当前等级到目标等级所需的所有材料
     */
    private Map<Long, Long> calculateAllMaterialsForCharacter(SrUserCharacter uc, Integer targetLevel) {
        Map<Long, Long> materialMap = new HashMap<>();
        
        Long characterId = uc.getCharacterId();
        Integer currentLevel = uc.getLevel() != null ? uc.getLevel().intValue() : 1;
        
        // 获取该角色的所有材料配置（从同一张表获取所有材料）
        List<SrCharacterAscensionMaterial> allMaterials = ascensionMaterialMapper.selectByCharacterId(characterId);
        if (allMaterials == null || allMaterials.isEmpty()) {
            // 尝试通用查询
            SrCharacterAscensionMaterial query = new SrCharacterAscensionMaterial();
            query.setCharacterId(characterId);
            allMaterials = ascensionMaterialMapper.selectList(query);
        }
        
        // 遍历所有材料，计算从当前等级到目标等级所需的材料
        for (SrCharacterAscensionMaterial material : allMaterials) {
            String ascensionType = material.getAscensionType();
            Integer breakLevel = material.getBreakLevel();
            
            // 获取该类型的目标进度
            Integer targetProgress = getTargetProgressForCalc(targetLevel, ascensionType);
            
            // 获取当前进度
            Integer currentProgress = getCurrentProgressSimple(uc, ascensionType);
            
            // 判断是否需要该材料：当前进度 < breakLevel <= 目标进度
            if (currentProgress != null && breakLevel != null && targetProgress != null
                    && currentProgress < breakLevel && breakLevel <= targetProgress) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
            }
        }
        
        // 计算经验书和信用点（根据等级差距）
        calculateExpMaterials(currentLevel, targetLevel, materialMap);
        
        return materialMap;
    }
    
    /**
     * 根据材料类型获取用户在对应维度上的当前进度
     */
    private Integer getCurrentProgress(SrUserCharacter uc, String ascensionType, Integer breakLevel) {
        AscensionTypeEnum type = AscensionTypeEnum.fromCode(ascensionType);
        if (type == null) {
            return null;
        }
        
        switch (type) {
            // level: 突破阶段材料
            case LEVEL:
                Integer level = uc.getLevel() != null ? uc.getLevel().intValue() : 1;
                // 计算当前已达到的突破阶段
                if (level >= 70) return 6;
                if (level >= 60) return 5;
                if (level >= 50) return 4;
                if (level >= 40) return 3;
                if (level >= 20) return 2;
                return 1;
            
            // basicAtk: 普攻
            case BASIC_ATK:
                return uc.getBasicAtkLevel() != null ? uc.getBasicAtkLevel().intValue() : 1;
            
            // skill: 战技
            case SKILL:
                return uc.getSkillLevel() != null ? uc.getSkillLevel().intValue() : 1;
            
            // ultimate: 终结技
            case ULTIMATE:
                return uc.getUltimateLevel() != null ? uc.getUltimateLevel().intValue() : 1;
            
            // talent: 天赋
            case TALENT:
                return uc.getTalentLevel() != null ? uc.getTalentLevel().intValue() : 1;
            
            // technique: 秘技（暂无对应字段，暂时返回1）
            case TECHNIQUE:
                return 1;
            
            // elationSkill: 欢愉技（暂无对应字段，暂时返回1）
            case ELATION_SKILL:
                return 1;
            
            // bonusAbility: 额外能力（break_level = 1,2,3 表示第1/2/3个能力）
            // 返回已解锁的能力数量，用于判断是否需要解锁下一个
            case BONUS_ABILITY:
                if (breakLevel != null) {
                    int unlockedCount = 0;
                    if (uc.getBonusAbility1() != null && uc.getBonusAbility1() == 1L) unlockedCount++;
                    if (uc.getBonusAbility2() != null && uc.getBonusAbility2() == 1L) unlockedCount++;
                    if (uc.getBonusAbility3() != null && uc.getBonusAbility3() == 1L) unlockedCount++;
                    return unlockedCount;
                }
                return 0;
            
            // statBonus: 额外属性（break_level = 1-10 表示第1-10个属性）
            // 返回已解锁的属性数量，用于判断是否需要解锁下一个
            case STAT_BONUS:
                if (breakLevel != null && breakLevel >= 1 && breakLevel <= 10) {
                    int unlockedCount = 0;
                    if (uc.getStatBonus1() != null && uc.getStatBonus1() == 1L) unlockedCount++;
                    if (uc.getStatBonus2() != null && uc.getStatBonus2() == 1L) unlockedCount++;
                    if (uc.getStatBonus3() != null && uc.getStatBonus3() == 1L) unlockedCount++;
                    if (uc.getStatBonus4() != null && uc.getStatBonus4() == 1L) unlockedCount++;
                    if (uc.getStatBonus5() != null && uc.getStatBonus5() == 1L) unlockedCount++;
                    if (uc.getStatBonus6() != null && uc.getStatBonus6() == 1L) unlockedCount++;
                    if (uc.getStatBonus7() != null && uc.getStatBonus7() == 1L) unlockedCount++;
                    if (uc.getStatBonus8() != null && uc.getStatBonus8() == 1L) unlockedCount++;
                    if (uc.getStatBonus9() != null && uc.getStatBonus9() == 1L) unlockedCount++;
                    if (uc.getStatBonus10() != null && uc.getStatBonus10() == 1L) unlockedCount++;
                    return unlockedCount;
                }
                return 0;
            
            default:
                return null;
        }
    }
    
    /**
     * 获取额外属性值
     */
    private Integer getStatBonus(SrUserCharacter uc, int index) {
        switch (index) {
            case 1: return uc.getStatBonus1() != null ? uc.getStatBonus1().intValue() : 0;
            case 2: return uc.getStatBonus2() != null ? uc.getStatBonus2().intValue() : 0;
            case 3: return uc.getStatBonus3() != null ? uc.getStatBonus3().intValue() : 0;
            case 4: return uc.getStatBonus4() != null ? uc.getStatBonus4().intValue() : 0;
            case 5: return uc.getStatBonus5() != null ? uc.getStatBonus5().intValue() : 0;
            case 6: return uc.getStatBonus6() != null ? uc.getStatBonus6().intValue() : 0;
            case 7: return uc.getStatBonus7() != null ? uc.getStatBonus7().intValue() : 0;
            case 8: return uc.getStatBonus8() != null ? uc.getStatBonus8().intValue() : 0;
            case 9: return uc.getStatBonus9() != null ? uc.getStatBonus9().intValue() : 0;
            case 10: return uc.getStatBonus10() != null ? uc.getStatBonus10().intValue() : 0;
            default: return 0;
        }
    }
    
    /**
     * 根据目标等级和材料类型获取目标进度
     */
    private Integer getTargetProgress(Integer targetLevel, String ascensionType, Integer breakLevel) {
        if (ascensionType == null) {
            return null;
        }
        
        // BREAKTHROUGH: 突破阶段材料
        if ("BREAKTHROUGH".equals(ascensionType)) {
            // 根据目标等级确定目标突破阶段
            if (targetLevel >= 80) return 6;
            if (targetLevel >= 70) return 6;
            if (targetLevel >= 60) return 5;
            if (targetLevel >= 50) return 4;
            if (targetLevel >= 40) return 3;
            if (targetLevel >= 20) return 2;
            return 1;
        }
        
        // BASIC_ATK: 普攻（满级6级）
        if ("BASIC_ATK".equals(ascensionType)) {
            return MAX_BASIC_ATK_LEVEL;
        }
        
        // SKILL: 战技（满级10级）
        if ("SKILL".equals(ascensionType)) {
            return MAX_SKILL_LEVEL;
        }
        
        // ULTIMATE: 终结技（满级10级）
        if ("ULTIMATE".equals(ascensionType)) {
            return MAX_SKILL_LEVEL;
        }
        
        // TALENT: 天赋（满级10级）
        if ("TALENT".equals(ascensionType)) {
            return MAX_SKILL_LEVEL;
        }
        
        // TECHNIQUE: 秘技（满级6级）
        if ("TECHNIQUE".equals(ascensionType)) {
            return MAX_BASIC_ATK_LEVEL;
        }
        
        // ELATION_SKILL: 欢愉技（满级6级）
        if ("ELATION_SKILL".equals(ascensionType)) {
            return MAX_BASIC_ATK_LEVEL;
        }
        
        // BONUS_ABILITY: 额外能力（解锁即满级，break_level表示第几个能力）
        if ("BONUS_ABILITY".equals(ascensionType)) {
            return 1;
        }
        
        // STAT_BONUS: 额外属性（解锁即满级，break_level表示第几个属性）
        if ("STAT_BONUS".equals(ascensionType)) {
            return 1;
        }
        
        return null;
    }
    
    /**
     * 计算等级晋升材料
     */
    private void calculateAscensionMaterials(Long characterId, Integer currentLevel, Integer targetLevel, Map<Long, Long> materialMap) {
        List<SrCharacterAscensionMaterial> ascensionMaterials = ascensionMaterialMapper.selectByCharacterId(characterId);
        // 如果没有配置晋升材料，尝试查询所有角色的晋升材料配置
        if (ascensionMaterials.isEmpty()) {
            SrCharacterAscensionMaterial query = new SrCharacterAscensionMaterial();
            query.setCharacterId(characterId);
            ascensionMaterials = ascensionMaterialMapper.selectList(query);
        }
        
        for (SrCharacterAscensionMaterial material : ascensionMaterials) {
            Integer breakLevel = material.getBreakLevel();
            // 如果breakLevel为null，默认计算所有材料
            if (breakLevel == null || (breakLevel > currentLevel && breakLevel <= targetLevel)) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
            }
        }
    }
    
    /**
     * 计算经验书和信用点
     */
    private void calculateExpMaterials(Integer currentLevel, Integer targetLevel, Map<Long, Long> materialMap) {
        List<SrCharacterExpUpgrade> expUpgrades = expUpgradeMapper.selectAllOrderByMinLevel();
        Long expBookId = findItemIdByName(EXP_BOOK_NAME);
        Long creditsId = findItemIdByName(CREDITS_NAME);
        
        for (SrCharacterExpUpgrade upgrade : expUpgrades) {
            // 只计算当前等级之后需要的经验
            if (upgrade.getMinLevel() != null && upgrade.getMaxLevel() != null
                    && upgrade.getMinLevel() >= currentLevel && upgrade.getMaxLevel() <= targetLevel) {
                if (expBookId != null && upgrade.getExpRequired() != null) {
                    // 将经验值转换为经验书数量（向上取整）
                    Long expBooks = (upgrade.getExpRequired() + EXP_PER_BOOK - 1) / EXP_PER_BOOK;
                    materialMap.merge(expBookId, expBooks, Long::sum);
                }
                if (creditsId != null && upgrade.getCreditsRequired() != null) {
                    materialMap.merge(creditsId, upgrade.getCreditsRequired(), Long::sum);
                }
            }
        }
    }
    
    /**
     * 从列表计算技能材料
     */
    private void calculateSkillMaterialsFromList(List<SrCharacterSkillMaterial> skillMaterials, SrUserCharacter uc, Map<Long, Long> materialMap) {
        if (skillMaterials == null || skillMaterials.isEmpty()) {
            return;
        }
        
        // 获取当前技能等级（Long转Integer），默认为1
        Integer basicAtkLevel = uc.getBasicAtkLevel() != null ? uc.getBasicAtkLevel().intValue() : 1;
        Integer skillLevel = uc.getSkillLevel() != null ? uc.getSkillLevel().intValue() : 1;
        Integer ultimateLevel = uc.getUltimateLevel() != null ? uc.getUltimateLevel().intValue() : 1;
        Integer talentLevel = uc.getTalentLevel() != null ? uc.getTalentLevel().intValue() : 1;
        
        for (SrCharacterSkillMaterial material : skillMaterials) {
            String skillType = material.getSkillType();
            Integer targetLevel = material.getTargetLevel();
            
            // 如果没有目标等级，默认计算所有技能材料（假设需要升级）
            if (targetLevel == null) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
                continue;
            }
            
            if (skillType == null) continue;
            
            Integer currentSkillLevel = 1;
            Integer maxSkillLevel = MAX_SKILL_LEVEL;
            
            switch (skillType) {
                case "basic_atk":
                    currentSkillLevel = basicAtkLevel;
                    maxSkillLevel = MAX_BASIC_ATK_LEVEL;
                    break;
                case "skill":
                    currentSkillLevel = skillLevel;
                    break;
                case "ultimate":
                    currentSkillLevel = ultimateLevel;
                    break;
                case "talent":
                    currentSkillLevel = talentLevel;
                    break;
            }
            
            // 只计算当前等级之后需要的技能材料
            if (targetLevel > currentSkillLevel && targetLevel <= maxSkillLevel) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
            }
        }
    }
    
    /**
     * 计算额外能力材料
     */
    /**
     * 从列表计算额外能力材料
     */
    private void calculateBonusAbilityMaterialsFromList(List<SrCharacterBonusAbilityMaterial> bonusAbilityMaterials, SrUserCharacter uc, Map<Long, Long> materialMap) {
        if (bonusAbilityMaterials == null || bonusAbilityMaterials.isEmpty()) {
            return;
        }
        
        // 获取当前额外能力状态（0=未解锁，1=已解锁，null=未设置则视为未解锁）
        Long bonus1 = uc.getBonusAbility1();
        Long bonus2 = uc.getBonusAbility2();
        Long bonus3 = uc.getBonusAbility3();
        
        for (SrCharacterBonusAbilityMaterial material : bonusAbilityMaterials) {
            Integer abilityIndex = material.getAbilityIndex();
            
            // 如果没有配置abilityIndex，默认计算所有额外能力材料
            if (abilityIndex == null) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
                continue;
            }
            
            // 判断该额外能力是否已解锁（值为1表示已解锁）
            boolean isUnlocked = false;
            switch (abilityIndex) {
                case 1:
                    isUnlocked = bonus1 != null && bonus1 == 1L;
                    break;
                case 2:
                    isUnlocked = bonus2 != null && bonus2 == 1L;
                    break;
                case 3:
                    isUnlocked = bonus3 != null && bonus3 == 1L;
                    break;
            }
            
            // 只计算未解锁的额外能力材料
            if (!isUnlocked) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
            }
        }
    }
    
    private void calculateBonusAbilityMaterials(Long characterId, SrUserCharacter uc, Map<Long, Long> materialMap) {
        List<SrCharacterBonusAbilityMaterial> bonusAbilityMaterials = bonusAbilityMaterialMapper.selectByCharacterId(characterId);
        
        // 如果没有配置额外能力材料，尝试查询所有角色的额外能力材料配置
        if (bonusAbilityMaterials.isEmpty()) {
            SrCharacterBonusAbilityMaterial query = new SrCharacterBonusAbilityMaterial();
            query.setCharacterId(characterId);
            bonusAbilityMaterials = bonusAbilityMaterialMapper.selectList(query);
        }
        
        calculateBonusAbilityMaterialsFromList(bonusAbilityMaterials, uc, materialMap);
    }
    
    /**
     * 从列表计算额外属性材料
     */
    private void calculateStatBonusMaterialsFromList(List<SrCharacterStatBonusMaterial> statBonusMaterials, SrUserCharacter uc, Map<Long, Long> materialMap) {
        if (statBonusMaterials == null || statBonusMaterials.isEmpty()) {
            return;
        }
        
        // 获取当前额外属性状态（0=未解锁，1=已解锁，null=未设置则视为未解锁）
        Long stat1 = uc.getStatBonus1();
        Long stat2 = uc.getStatBonus2();
        Long stat3 = uc.getStatBonus3();
        Long stat4 = uc.getStatBonus4();
        Long stat5 = uc.getStatBonus5();
        Long stat6 = uc.getStatBonus6();
        Long stat7 = uc.getStatBonus7();
        Long stat8 = uc.getStatBonus8();
        Long stat9 = uc.getStatBonus9();
        Long stat10 = uc.getStatBonus10();
        
        for (SrCharacterStatBonusMaterial material : statBonusMaterials) {
            Integer statIndex = material.getStatIndex();
            
            // 如果没有配置statIndex，默认计算所有额外属性材料
            if (statIndex == null) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
                continue;
            }
            
            if (statIndex < 1 || statIndex > 10) continue;
            
            // 判断该额外属性是否已解锁（值为1表示已解锁）
            boolean isUnlocked = false;
            switch (statIndex) {
                case 1: isUnlocked = stat1 != null && stat1 == 1L; break;
                case 2: isUnlocked = stat2 != null && stat2 == 1L; break;
                case 3: isUnlocked = stat3 != null && stat3 == 1L; break;
                case 4: isUnlocked = stat4 != null && stat4 == 1L; break;
                case 5: isUnlocked = stat5 != null && stat5 == 1L; break;
                case 6: isUnlocked = stat6 != null && stat6 == 1L; break;
                case 7: isUnlocked = stat7 != null && stat7 == 1L; break;
                case 8: isUnlocked = stat8 != null && stat8 == 1L; break;
                case 9: isUnlocked = stat9 != null && stat9 == 1L; break;
                case 10: isUnlocked = stat10 != null && stat10 == 1L; break;
            }
            
            // 只计算未解锁的额外属性材料
            if (!isUnlocked) {
                if (material.getItemId() != null && material.getQuantity() != null) {
                    materialMap.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
                }
            }
        }
    }
    
    /**
     * 计算额外属性材料
     */
    private void calculateStatBonusMaterials(Long characterId, SrUserCharacter uc, Map<Long, Long> materialMap) {
        List<SrCharacterStatBonusMaterial> statBonusMaterials = statBonusMaterialMapper.selectByCharacterId(characterId);
        
        // 如果没有配置额外属性材料，尝试查询所有角色的额外属性材料配置
        if (statBonusMaterials.isEmpty()) {
            SrCharacterStatBonusMaterial query = new SrCharacterStatBonusMaterial();
            query.setCharacterId(characterId);
            statBonusMaterials = statBonusMaterialMapper.selectList(query);
        }
        
        calculateStatBonusMaterialsFromList(statBonusMaterials, uc, materialMap);
    }
    
    /**
     * 获取用户背包材料映射
     */
    private Map<Long, Long> getUserItemMap(Long userId) {
        Map<Long, Long> map = new HashMap<>();
        SrUserItem query = new SrUserItem();
        query.setUserId(userId);
        List<SrUserItem> items = userItemMapper.selectSrUserItemList(query);
        for (SrUserItem item : items) {
            if (item.getItemId() != null && item.getQuantity() != null) {
                map.put(item.getItemId(), item.getQuantity());
            }
        }
        return map;
    }
    
    /**
     * 根据名称查找材料ID
     */
    private Long findItemIdByName(String name) {
        SrItem query = new SrItem();
        query.setItemName(name);
        List<SrItem> items = itemMapper.selectSrItemList(query);
        return items.isEmpty() ? null : items.get(0).getId();
    }
    
    /**
     * 构建材料需求列表（总体）
     */
    private List<SrCultivationCalcVO.MaterialRequirement> buildMaterialList(Map<Long, Long> materialMap, Map<Long, Long> userItemMap) {
        List<SrCultivationCalcVO.MaterialRequirement> list = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : materialMap.entrySet()) {
            SrItem item = itemMapper.selectSrItemById(entry.getKey());
            if (item == null) continue;
            
            SrCultivationCalcVO.MaterialRequirement req = new SrCultivationCalcVO.MaterialRequirement();
            req.setItemId(item.getId());
            req.setItemName(item.getItemName());
            req.setItemImage(item.getImage());
            req.setRarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : null);
            req.setRequiredQuantity(entry.getValue());
            req.setOwnedQuantity(userItemMap.getOrDefault(entry.getKey(), 0L));
            req.setDifference(req.getRequiredQuantity() - req.getOwnedQuantity());
            
            list.add(req);
        }
        
        // 按稀有度排序（高稀有度在前）
        list.sort((a, b) -> {
            Integer aLevel = a.getRarityLevel();
            Integer bLevel = b.getRarityLevel();
            if (aLevel == null) return 1;
            if (bLevel == null) return -1;
            return bLevel.compareTo(aLevel);
        });
        
        return list;
    }
    
    /**
     * 构建材料需求列表（单个角色）
     */
    private List<SrCharacterCultivationVO.MaterialRequirement> buildCharacterMaterialList(Map<Long, Long> materialMap, Map<Long, Long> userItemMap) {
        List<SrCharacterCultivationVO.MaterialRequirement> list = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : materialMap.entrySet()) {
            SrItem item = itemMapper.selectSrItemById(entry.getKey());
            if (item == null) continue;
            
            SrCharacterCultivationVO.MaterialRequirement req = new SrCharacterCultivationVO.MaterialRequirement();
            req.setItemId(item.getId());
            req.setItemName(item.getItemName());
            req.setItemImage(item.getImage());
            req.setRarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : null);
            req.setRequiredQuantity(entry.getValue());
            req.setOwnedQuantity(userItemMap.getOrDefault(entry.getKey(), 0L));
            req.setDifference(req.getRequiredQuantity() - req.getOwnedQuantity());
            
            list.add(req);
        }
        
        // 按稀有度排序（高稀有度在前）
        list.sort((a, b) -> {
            Integer aLevel = a.getRarityLevel();
            Integer bLevel = b.getRarityLevel();
            if (aLevel == null) return 1;
            if (bLevel == null) return -1;
            return bLevel.compareTo(aLevel);
        });
        
        return list;
    }
    
    /**
     * 简化的当前进度获取方法 - 根据材料类型返回对应的当前等级
     */
    private Integer getCurrentProgressSimple(SrUserCharacter uc, String ascensionType) {
        if (ascensionType == null) {
            return null;
        }
        
        switch (ascensionType.toLowerCase()) {
            case "level":
                return uc.getLevel() != null ? uc.getLevel().intValue() : 1;
            case "basicatk":
                return uc.getBasicAtkLevel() != null ? uc.getBasicAtkLevel().intValue() : 1;
            case "skill":
                return uc.getSkillLevel() != null ? uc.getSkillLevel().intValue() : 1;
            case "ultimate":
                return uc.getUltimateLevel() != null ? uc.getUltimateLevel().intValue() : 1;
            case "talent":
                return uc.getTalentLevel() != null ? uc.getTalentLevel().intValue() : 1;
            case "technique":
                return 1;
            case "elationskill":
                return 1;
            case "bonusability":
                return 0;
            case "statbonus":
                return 0;
            default:
                return null;
        }
    }
    
    /**
     * 简化的目标进度获取方法 - 根据材料类型返回对应的目标等级
     */
    private Integer getTargetProgressForCalc(Integer targetLevel, String ascensionType) {
        if (ascensionType == null) {
            return null;
        }
        
        switch (ascensionType.toLowerCase()) {
            case "level":
                return targetLevel;
            case "basicatk":
                return MAX_BASIC_ATK_LEVEL;
            case "skill":
            case "ultimate":
            case "talent":
                return MAX_SKILL_LEVEL;
            case "technique":
            case "elationskill":
                return MAX_BASIC_ATK_LEVEL;
            case "bonusability":
                return 3;
            case "statbonus":
                return 10;
            default:
                return null;
        }
    }
}