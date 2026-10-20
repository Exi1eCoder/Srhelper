package com.quico.srhelper.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quico.srhelper.domain.contract.MaterialRequirementView;
import lombok.Data;
import java.util.List;

/**
 * 光锥养成计算VO
 * 
 * @author quico
 * @date 2026-06-07
 */
@Data
public class SrLightConeCultivationVO
{
    /** 光锥信息 */
    private LightConeInfo lightCone;

    /** 材料需求列表 */
    private List<MaterialRequirement> materials;

    /** 是否已完成培养（无材料需求） */
    private Boolean completed;

    /** 材料的合成折算结果（整体状态 + 需刷取材料明细 + 合成方案） */
    private MaterialCalculationResultVO calculation;

    /**
     * 光锥信息
     */
    @Data
    public static class LightConeInfo
    {
        /** 用户持有光锥ID */
        private Long userLightConeId;

        /** 光锥ID */
        private Long lightConeId;

        /** 光锥名称 */
        private String lightConeName;

        /** 光锥图片 */
        private String lightConeImage;

        /** 星级 */
        private Integer starLevel;

        /** 当前等级 */
        private Integer currentLevel;

        /** 目标等级 */
        private Integer targetLevel;

        /** 当前叠影 */
        private Integer currentSuperimposition;

        /** 叠影（罗马数字显示） */
        private String superimpositionRoman;
    }

    /**
     * 材料需求
     */
    @Data
    public static class MaterialRequirement implements MaterialRequirementView
    {
        /** 物品ID */
        private Long itemId;

        /** 物品名称 */
        private String itemName;

        /** 物品图片 */
        private String itemImage;

        /** 稀有度 */
        private Integer rarityLevel;

        /** 所需总数 */
        private Long requiredQuantity;

        /** 背包数量 */
        private Long ownedQuantity;

        /** 差额（不足为0） */
        private Long difference;

        /** 材料类型排序优先级（内部排序使用，不返回前端） */
        @JsonIgnore
        private Integer sortOrder;
    }
}