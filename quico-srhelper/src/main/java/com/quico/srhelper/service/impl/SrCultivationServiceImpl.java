package com.quico.srhelper.service.impl;

import com.quico.srhelper.domain.enums.AscensionTypeEnum;
import com.quico.srhelper.domain.enums.CharacterPathEnum;
import com.quico.srhelper.domain.enums.MaterialSortEnum;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.domain.SrCharacterBonusAbilityMaterial;
import com.quico.srhelper.domain.SrCharacterSkillMaterial;
import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.domain.vo.SrCharacterCultivationVO;
import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;
import com.quico.srhelper.domain.vo.SrCharactersCultivationVO;
import com.quico.srhelper.domain.vo.SrCultivationCalcVO;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterBonusAbilityMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterStatBonusMaterialMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrUserCharacterMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import com.quico.srhelper.service.ISrCultivationService;
import com.quico.srhelper.service.helper.UsableChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 养成计算Service实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrCultivationServiceImpl implements ISrCultivationService {
    
    private final SrUserCharacterMapper userCharacterMapper;
    private final SrCharacterMapper characterMapper;
    private final SrCharacterAscensionMaterialMapper ascensionMaterialMapper;
    private final SrCharacterBonusAbilityMaterialMapper bonusAbilityMaterialMapper;
    private final SrCharacterStatBonusMaterialMapper statBonusMaterialMapper;
    private final SrItemMapper itemMapper;
    private final SrUserItemMapper userItemMapper;
    private final ISrCharacterExpUpgradeService expUpgradeService;
    private final UsableChecker usableChecker;
    
    private static final Integer MAX_LEVEL = 80;
    private static final Integer MAX_SKILL_LEVEL = 10;
    private static final Integer MAX_BASIC_ATK_LEVEL = 6;
    private static final Long EXP_PER_BOOK = 1000L; // 每本旅行见闻提供的经验值
    /** 角色升级实际消耗的经验书物品名（与详情展示用的"经验值"物品解耦） */
    private static final String EXP_BOOK_NAME = "旅行见闻";
    
    @Override
    public SrCultivationCalcVO calculateTotalCultivation(Long userId, Integer targetLevel) {
        if (targetLevel == null || targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        
        SrCultivationCalcVO result = new SrCultivationCalcVO();
        
        // 分类：未完成培养和已完成培养
        List<SrCultivationCalcVO.CharacterInfo> normalCharacters = new ArrayList<>();
        List<SrCultivationCalcVO.CharacterInfo> completedCharacters = new ArrayList<>();
        
        // 计算总体材料消耗（按持有角色进度过滤后的消耗）
        Map<Long, Long> totalMaterialMap = new HashMap<>();
        
        for (CharacterMaterials cm : calculateUserCharacterMaterials(userId, targetLevel)) {
            // 已完成培养的角色直接返回，不参与材料汇总
            boolean isCompleted = cm.completed;
            
            SrCultivationCalcVO.CharacterInfo info = new SrCultivationCalcVO.CharacterInfo();
            info.setUserCharacterId(cm.userCharacter.getId());
            info.setCharacterId(cm.userCharacter.getCharacterId());
            info.setCharacterName(cm.character.getCharacterName());
            info.setCharacterImage(cm.character.getAvatar());
            info.setCurrentLevel(cm.userCharacter.getLevel() != null ? cm.userCharacter.getLevel().intValue() : 1);
            info.setTargetLevel(targetLevel);
            info.setIsMaxLevel(isCompleted);
            
            if (isCompleted) {
                completedCharacters.add(info);
            } else {
                normalCharacters.add(info);
                
                // 累加到总体材料
                cm.materials.forEach((k, v) -> totalMaterialMap.merge(k, v, Long::sum));
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
        result.setTotalMaterials(buildMaterialList(totalMaterialMap, userItemMap));
        
        return result;
    }
    
    @Override
    public SrCharactersCultivationVO calculateUserCharactersCultivation(Long userId, Integer targetLevel) {
        if (targetLevel == null || targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        
        // 查询用户背包材料（各角色材料列表共用）
        Map<Long, Long> userItemMap = getUserItemMap(userId);
        
        List<SrCharactersCultivationVO.CharacterInfo> characters = new ArrayList<>();
        for (CharacterMaterials cm : calculateUserCharacterMaterials(userId, targetLevel)) {
            SrCharactersCultivationVO.CharacterInfo info = new SrCharactersCultivationVO.CharacterInfo();
            info.setUserCharacterId(cm.userCharacter.getId());
            info.setCharacterId(cm.userCharacter.getCharacterId());
            info.setCharacterName(cm.character.getCharacterName());
            info.setCharacterImage(cm.character.getAvatar());
            info.setCurrentLevel(cm.userCharacter.getLevel() != null ? cm.userCharacter.getLevel().intValue() : 1);
            info.setTargetLevel(targetLevel);
            info.setIsMaxLevel(cm.completed);
            info.setMaterials(buildMaterialList(cm.materials, userItemMap));
            characters.add(info);
        }
        
        SrCharactersCultivationVO result = new SrCharactersCultivationVO();
        result.setCharacters(characters);
        log.debug("用户{}的养成材料计算结果：{}", userId, result);
        return result;
    }
    
    /**
     * 查询用户所有角色并逐个计算养成材料
     * 总计接口与角色明细接口共用，总计接口在其结果上再做聚合
     */
    private List<CharacterMaterials> calculateUserCharacterMaterials(Long userId, Integer targetLevel) {
        List<CharacterMaterials> results = new ArrayList<>();
        
        SrUserCharacter query = new SrUserCharacter();
        query.setUserId(userId);
        List<SrUserCharacter> userCharacters = userCharacterMapper.selectSrUserCharacterList(query);

        // 未完成材料绑定的角色不参与养成计算
        Set<Long> usableCharacterIds = usableChecker.getUsableCharacterIds();

        for (SrUserCharacter uc : userCharacters) {
            if (!usableCharacterIds.contains(uc.getCharacterId())) continue;
            SrCharacter character = characterMapper.selectSrCharacterById(uc.getCharacterId());
            if (character == null) continue;
            
            results.add(new CharacterMaterials(uc, character, calculateAllMaterialsForCharacter(uc, character, targetLevel)));
        }
        
        return results;
    }
    
    /**
     * 单个角色的材料计算结果（内部传递用，不含任何总计）
     */
    private static class CharacterMaterials {
        private final SrUserCharacter userCharacter;
        private final SrCharacter character;
        private final boolean completed;
        private final Map<Long, Long> materials;
        
        private CharacterMaterials(SrUserCharacter userCharacter, SrCharacter character, MaterialCalc calc) {
            this.userCharacter = userCharacter;
            this.character = character;
            this.completed = calc.completed;
            this.materials = calc.materials;
        }
    }
    
    /**
     * 单角色材料计算结果（内部传递用）
     * materials：按持有角色(sr_user_character)进度过滤后的材料消耗，即前端"消耗"列
     * 前端"差额"列 = materials - 用户持有(sr_user_item)
     */
    private static class MaterialCalc {
        private boolean completed = false;
        private final Map<Long, Long> materials = new HashMap<>();
    }
    
    @Override
    public SrCharacterCultivationVO calculateCharacterCultivation(Long userCharacterId, Integer targetLevel) {
        if (targetLevel == null || targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        
        SrUserCharacter uc = userCharacterMapper.selectSrUserCharacterById(userCharacterId);
        if (uc == null) {
            return null;
        }
        
        SrCharacter character = characterMapper.selectSrCharacterById(uc.getCharacterId());
        if (character == null) {
            return null;
        }
        
        // 兜底校验：材料绑定未完成（非8行）的角色不可进行养成计算
        usableChecker.assertCharacterUsable(uc.getCharacterId());
        
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
        
        // 计算材料消耗（按持有角色进度过滤）
        MaterialCalc calc = calculateAllMaterialsForCharacter(uc, character, targetLevel);
        log.debug("计算角色{}的材料消耗，当前等级：{}，目标等级：{}，消耗={}",
                uc.getCharacterId(), uc.getLevel(), targetLevel, calc.materials);
        info.setIsMaxLevel(calc.completed);
        
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(uc.getUserId());
        
        // 构建材料需求列表
        List<SrCharacterCultivationVO.MaterialRequirement> materialList = buildMaterialList(calc.materials, userItemMap);
        result.setMaterials(materialList);
        
        return result;
    }
    
    /**
     * 计算单个角色的材料消耗
     * 所有材料都存储在 sr_character_ascension_material 表中
     * - 已完成培养（全等级满级且额外能力/额外属性全为1）：直接返回，不查材料表、不做任何计算
     * - 未完成培养：按 sr_user_character 的进度过滤出实际需要的材料消耗（前端"消耗"列），
     *   差额 = 消耗 - 用户持有(sr_user_item)
     */
    private MaterialCalc calculateAllMaterialsForCharacter(SrUserCharacter uc, SrCharacter character, Integer targetLevel) {
        MaterialCalc calc = new MaterialCalc();
        
        // 已完成培养的角色直接返回，不需要查表等材料计算
        if (isCultivationCompleted(uc, character)) {
            calc.completed = true;
            return calc;
        }
        
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
        
        // 只计入按持有角色进度过滤后的材料消耗
        for (SrCharacterAscensionMaterial material : allMaterials) {
            if (material.getItemId() == null || material.getQuantity() == null) {
                continue;
            }
            if (isMaterialNeeded(uc, character, material)) {
                calc.materials.merge(material.getItemId(), material.getQuantity().longValue(), Long::sum);
            }
        }
        
        // 经验书和信用点按等级差距计算
        calculateExpMaterials(currentLevel, targetLevel, calc.materials);
        
        return calc;
    }
    
    /**
     * 判断角色是否已完成培养
     * 等级满级 + 普攻满级 + 战技/终结技/天赋满级 + 额外能力1~3全为1 + 额外属性1~10全为1
     * 命途特有维度：记忆命途还需忆灵技能/忆灵天赋满级，欢愉命途还需欢愉技满级
     */
    private boolean isCultivationCompleted(SrUserCharacter uc, SrCharacter character) {
        boolean baseCompleted = isReachMax(uc.getLevel(), MAX_LEVEL)
                && isReachMax(uc.getBasicAtkLevel(), MAX_BASIC_ATK_LEVEL)
                && isReachMax(uc.getSkillLevel(), MAX_SKILL_LEVEL)
                && isReachMax(uc.getUltimateLevel(), MAX_SKILL_LEVEL)
                && isReachMax(uc.getTalentLevel(), MAX_SKILL_LEVEL)
                && isUnlocked(uc.getBonusAbility1())
                && isUnlocked(uc.getBonusAbility2())
                && isUnlocked(uc.getBonusAbility3())
                && isUnlocked(uc.getStatBonus1())
                && isUnlocked(uc.getStatBonus2())
                && isUnlocked(uc.getStatBonus3())
                && isUnlocked(uc.getStatBonus4())
                && isUnlocked(uc.getStatBonus5())
                && isUnlocked(uc.getStatBonus6())
                && isUnlocked(uc.getStatBonus7())
                && isUnlocked(uc.getStatBonus8())
                && isUnlocked(uc.getStatBonus9())
                && isUnlocked(uc.getStatBonus10());
        if (!baseCompleted) {
            return false;
        }
        
        // 命途特有维度
        CharacterPathEnum path = CharacterPathEnum.fromPath(character != null ? character.getPath() : null);
        if (path.hasMemosprite()
                && (!isReachMax(uc.getMemospriteskillLevel(), path.getMaxMemospriteskillLevel())
                    || !isReachMax(uc.getMemospritetalentLevel(), path.getMaxMemospritetalentLevel()))) {
            return false;
        }
        if (path.hasElationSkill() && !isReachMax(uc.getElationskillLevel(), path.getMaxElationskillLevel())) {
            return false;
        }
        return true;
    }
    
    /**
     * 等级类进度是否已达到满级
     */
    private boolean isReachMax(Long value, int maxLevel) {
        return value != null && value >= maxLevel;
    }
    
    /**
     * 额外能力/额外属性是否已解锁（表中值为1表示已完成培养）
     */
    private boolean isUnlocked(Long flag) {
        return flag != null && flag == 1L;
    }
    
    /**
     * 判断某条材料是否仍需要消耗（依据 sr_user_character 的当前进度）
     * 未满级/未点亮的部分计入消耗，已完成的维度不参与计算
     */
    private boolean isMaterialNeeded(SrUserCharacter uc, SrCharacter character, SrCharacterAscensionMaterial material) {
        String ascensionType = material.getAscensionType();
        Integer breakLevel = material.getBreakLevel();
        if (ascensionType == null || breakLevel == null) {
            return true;
        }
        
        CharacterPathEnum path = CharacterPathEnum.fromPath(character != null ? character.getPath() : null);
        
        switch (ascensionType.toLowerCase()) {
            // 等级突破：共6次，1阶对应20级上限，6阶对应70级（70级为最后一次突破）
            // 已完成阶数 = max(0, (等级 - 1) / 10 - 1)，只计入大于已完成阶数的突破
            case "levelasc": {
                int level = uc.getLevel() != null ? uc.getLevel().intValue() : 1;
                int completedStages = Math.max(0, (level - 1) / 10 - 1);
                return breakLevel > completedStages;
            }
            // 普攻：按表中普攻等级过滤
            case "basicatk": {
                int currentLevel = uc.getBasicAtkLevel() != null ? uc.getBasicAtkLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            // 战技/终结技/天赋：按各自表中等级过滤
            case "skill": {
                int currentLevel = uc.getSkillLevel() != null ? uc.getSkillLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            case "ultimate": {
                int currentLevel = uc.getUltimateLevel() != null ? uc.getUltimateLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            case "talent": {
                int currentLevel = uc.getTalentLevel() != null ? uc.getTalentLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            // 额外能力：break_level 1~3 对应 bonus_ability_1~3，表中为1表示已完成培养，不参与材料计算
            case "bonusability":
                return getBonusAbilityFlag(uc, breakLevel) == 0;
            // 额外属性：break_level 1~10 对应 stat_bonus_1~10，表中为1表示已完成培养，不参与材料计算
            case "statbonus":
                return getStatBonus(uc, breakLevel) == 0;
            // 忆灵技能/忆灵天赋：记忆命途特有维度，非记忆命途不参与计算（默认1到1消耗为0）
            case "memospriteskill": {
                if (!path.hasMemosprite()) {
                    return false;
                }
                int currentLevel = uc.getMemospriteskillLevel() != null ? uc.getMemospriteskillLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            case "memospritetalent": {
                if (!path.hasMemosprite()) {
                    return false;
                }
                int currentLevel = uc.getMemospritetalentLevel() != null ? uc.getMemospritetalentLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            // 欢愉技：欢愉命途特有维度，非欢愉命途不参与计算（默认1到1消耗为0）
            case "elationskill": {
                if (!path.hasElationSkill()) {
                    return false;
                }
                int currentLevel = uc.getElationskillLevel() != null ? uc.getElationskillLevel().intValue() : 1;
                return breakLevel > currentLevel;
            }
            // TODO 秘技：sr_user_character 表暂无秘技等级字段，暂按未培养处理（全部计入消耗）
            default:
                return true;
        }
    }
    
    /**
     * 获取额外能力解锁标记（1=已解锁，0=未解锁）
     */
    private int getBonusAbilityFlag(SrUserCharacter uc, int index) {
        Long flag;
        switch (index) {
            case 1: flag = uc.getBonusAbility1(); break;
            case 2: flag = uc.getBonusAbility2(); break;
            case 3: flag = uc.getBonusAbility3(); break;
            default: return 0;
        }
        return flag != null && flag == 1L ? 1 : 0;
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
        // 目标等级与当前等级相同，不需要升级经验和信用点
        if (currentLevel != null && currentLevel.equals(targetLevel)) {
            log.debug("当前等级与目标等级相同，不需要升级经验和信用点");
            return;
        }
        
        log.debug("计算经验书和信用点，当前等级：{}，目标等级：{}", currentLevel, targetLevel);
        // 复用角色经验升级配置的 Redis 缓存获取各等级区间的经验/信用点数值；
        // 实际消耗的经验书为「旅行见闻」，与配置表中仅作展示的「经验值」物品解耦，这里独立按名称解析
        SrItem expBook = itemMapper.selectSrItemByName(EXP_BOOK_NAME);
        Long expBookItemId = expBook != null ? expBook.getId() : null;
        List<SrCharacterExpUpgradeVO> expUpgrades = expUpgradeService.selectAllExpUpgrades();
        
        for (SrCharacterExpUpgradeVO upgrade : expUpgrades) {
            // 只计算当前等级之后需要的经验
            if (upgrade.getMinLevel() != null && upgrade.getMaxLevel() != null
                    && upgrade.getMinLevel() >= currentLevel && upgrade.getMaxLevel() <= targetLevel) {
                if (expBookItemId != null && upgrade.getExpRequired() != null) {
                    // 将经验值转换为经验书数量（向上取整）
                    Long expBooks = (upgrade.getExpRequired() + EXP_PER_BOOK - 1) / EXP_PER_BOOK;
                    materialMap.merge(expBookItemId, expBooks, Long::sum);
                    log.debug("等级区间 {} → {}：经验书 {} 本，信用点 {}",
                            upgrade.getMinLevel(), upgrade.getMaxLevel(), expBooks, upgrade.getCreditsRequired());
                }
                if (upgrade.getCreditsItemId() != null && upgrade.getCreditsRequired() != null) {
                    materialMap.merge(upgrade.getCreditsItemId(), upgrade.getCreditsRequired(), Long::sum);
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
        log.debug("查询用户持有材料，userId={}，记录数={}，持有材料={}", userId, items.size(), map);
        return map;
    }
    
    /**
     * 构建材料需求列表
     * - requiredQuantity（消耗）：按持有角色进度过滤后的材料消耗
     * - difference（差额）：消耗 - 用户持有数量
     */
    private List<SrCharacterCultivationVO.MaterialRequirement> buildMaterialList(Map<Long, Long> materialMap,
                                                                                Map<Long, Long> userItemMap) {
        List<SrCharacterCultivationVO.MaterialRequirement> list = new ArrayList<>();
        if (materialMap == null || materialMap.isEmpty()) {
            return list;
        }
        
        log.debug("构建材料需求列表，材料消耗={}，用户持有材料={}", materialMap, userItemMap);
        
        // 收集 itemId 后一次性批量查询，避免逐条查库
        Map<Long, SrItem> itemMap = selectItemsByIds(materialMap.keySet());
        
        for (Map.Entry<Long, Long> entry : materialMap.entrySet()) {
            SrItem item = itemMap.get(entry.getKey());
            if (item == null) continue;
            
            long required = entry.getValue();
            long owned = userItemMap.getOrDefault(entry.getKey(), 0L);
            // 差额 = 材料消耗 - 用户持有数量，不足为0
            long difference = Math.max(required - owned, 0L);
            
            SrCharacterCultivationVO.MaterialRequirement req = new SrCharacterCultivationVO.MaterialRequirement();
            req.setItemId(item.getId());
            req.setItemName(item.getItemName());
            req.setItemImage(item.getImage());
            req.setRarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : null);
            req.setRequiredQuantity(required);
            req.setOwnedQuantity(owned);
            req.setDifference(difference);
            // 材料类型排序优先级（内部使用，不返回前端）
            req.setSortOrder(MaterialSortEnum.resolveOrder(item));
            log.debug("材料[{}]{}：消耗={}，持有={}，差额={}", req.getItemId(), req.getItemName(),
                    req.getRequiredQuantity(), req.getOwnedQuantity(), req.getDifference());
            
            list.add(req);
        }
        
        // 按材料类型优先级（信用点/旅行见闻/TOD/EOW/SS/TRA/CAL）、星级从高到低、itemId 稳定排序
        list.sort(Comparator
                .comparingInt(SrCharacterCultivationVO.MaterialRequirement::getSortOrder)
                .thenComparing(SrCharacterCultivationVO.MaterialRequirement::getRarityLevel,
                        Comparator.nullsLast(Comparator.<Integer>reverseOrder()))
                .thenComparing(SrCharacterCultivationVO.MaterialRequirement::getItemId));
        
        return list;
    }
    
    /**
     * 按 itemId 集合批量查询材料，返回 itemId -> SrItem 映射
     */
    private Map<Long, SrItem> selectItemsByIds(Set<Long> itemIds) {
        Map<Long, SrItem> itemMap = new HashMap<>();
        if (itemIds == null || itemIds.isEmpty()) {
            return itemMap;
        }
        List<SrItem> items = itemMapper.selectSrItemByIds(new ArrayList<>(itemIds));
        for (SrItem item : items) {
            itemMap.put(item.getId(), item);
        }
        return itemMap;
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