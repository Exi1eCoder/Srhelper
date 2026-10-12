package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 养成计算VO
 */
@Data
public class SrCultivationCalcVO {
    
    /** 角色列表（包含总体） */
    private List<CharacterInfo> characters;
    
    /** 总体所需材料 */
    private List<SrCharacterCultivationVO.MaterialRequirement> totalMaterials;
    
    /** 已满级角色列表 */
    private List<CharacterInfo> maxLevelCharacters;
    
    /** 角色信息 */
    @Data
    public static class CharacterInfo {
        private Long userCharacterId;
        private Long characterId;
        private String characterName;
        private String characterImage;
        private Integer currentLevel;
        private Integer targetLevel;
        private Boolean isMaxLevel;
    }
}