package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 单个角色养成计算VO
 */
@Data
public class SrCharacterCultivationVO {
    
    /** 角色信息 */
    private CharacterInfo character;
    
    /** 该角色所需材料 */
    private List<MaterialRequirement> materials;
    
    /** 角色信息 */
    @Data
    public static class CharacterInfo {
        private Long userCharacterId;
        private Long characterId;
        private String characterName;
        private String characterImage;
        private Integer currentLevel;
        private Integer targetLevel;
    }
    
    /** 材料需求 */
    @Data
    public static class MaterialRequirement {
        private Long itemId;
        private String itemName;
        private String itemImage;
        private Integer rarityLevel;
        private Long requiredQuantity;
        private Long ownedQuantity;
        private Long difference;
    }
}