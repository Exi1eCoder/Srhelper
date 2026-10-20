package com.quico.srhelper.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quico.srhelper.domain.contract.MaterialRequirementView;
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
    
    /** 该角色材料的合成折算结果（整体状态 + 需刷取材料明细 + 合成方案） */
    private MaterialCalculationResultVO calculation;
    
    /** 角色信息 */
    @Data
    public static class CharacterInfo {
        private Long userCharacterId;
        private Long characterId;
        private String characterName;
        private String characterImage;
        private Integer currentLevel;
        private Integer targetLevel;
        /** 是否已培养完成（全等级满级且额外能力/额外属性全部为1） */
        private Boolean isMaxLevel;
    }
    
    /** 材料需求 */
    @Data
    public static class MaterialRequirement implements MaterialRequirementView {
        private Long itemId;
        private String itemName;
        private String itemImage;
        private Integer rarityLevel;
        private Long requiredQuantity;
        private Long ownedQuantity;
        private Long difference;
        /** 材料类型排序优先级（内部排序使用，不返回前端） */
        @JsonIgnore
        private Integer sortOrder;
    }
}