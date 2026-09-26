package com.quico.srhelper.domain.dto;

import java.util.List;
import lombok.Data;

/**
 * 光锥晋升材料批量保存DTO
 *
 * @author quico
 * @date 2026-05-30
 */
@Data
public class LightConeAscensionBatchSaveDTO
{
    /** 光锥ID */
    private Long lightConeId;

    /** 各阶段晋升材料 */
    private List<AscensionGroup> ascensions;

    @Data
    public static class AscensionGroup
    {
        /** 突破阶段（1-7） */
        private Integer ascensionPhase;

        /** 突破等级 */
        private Integer breakLevel;

        /** 该阶段材料列表 */
        private List<MaterialItem> materials;
    }

    @Data
    public static class MaterialItem
    {
        /** 物品ID */
        private Long itemId;

        /** 材料稀有度 */
        private Integer rarityLevel;

        /** 所需数量 */
        private Integer quantity;
    }
}
