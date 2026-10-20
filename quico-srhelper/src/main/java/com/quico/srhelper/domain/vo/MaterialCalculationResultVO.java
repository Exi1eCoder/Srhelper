package com.quico.srhelper.domain.vo;

import java.util.List;

import com.quico.srhelper.domain.enums.SynthesisStatusEnum;
import com.quico.srhelper.domain.contract.MaterialRequirementView;

import lombok.Data;

/**
 * 合成折算结果VO（角色、光锥维度）
 *
 * 游戏中 3 个低阶材料可合成 1 个高阶材料（仅 TRA 世界掉落 / CAL 拟造花萼，
 * 二星→三星→四星，不支持高阶转低阶）。同一 series 内各稀有度视为整体逐级折算。
 * 本 VO 汇总一个角色/光锥的全部材料后，给出整体状态、折算后仍需刷取的材料及差额、以及各系列的合成方案。
 *
 * @author quico
 */
@Data
public class MaterialCalculationResultVO {

    /** 整体状态：完全够 / 通过合成足够 / 合成一部分仍不够 / 需刷取（取全部材料中的最坏情况） */
    private SynthesisStatusEnum status;

    /** 折算（合成）后仍需刷取的材料及各自差额；全部满足时为空列表 */
    private List<RequiredItem> required;

    /** 合成方案列表（一个目标材料对应一条）；无需合成或无法合成时为 null */
    private List<Synthesis> synthesis;

    /**
     * 材料基础信息（物品ID + 图标URL），供各内部类引用
     */
    @Data
    public static class ItemInfo {

        /** 物品ID */
        private Long itemId;

        /** 物品图标URL */
        private String itemImage;
    }

    /**
     * 需要刷取的材料
     */
    @Data
    public static class RequiredItem {

        /** 材料信息 */
        private ItemInfo item;

        /** 需要刷取的差额（折算后仍缺的数量） */
        private Long difference;
    }

    /**
     * 合成方案
     */
    @Data
    public static class Synthesis {

        /** 目标材料 */
        private ItemInfo targetItem;

        /** 使用低阶材料合成出的数量 */
        private Integer synthesizeCount;

        /** 消耗的低阶材料列表 */
        private List<SynthesisMaterial> materials;
    }

    /**
     * 合成消耗的低阶材料
     */
    @Data
    public static class SynthesisMaterial {

        /** 材料信息 */
        private ItemInfo item;

        /** 消耗数量 */
        private Integer consumeCount;
    }
}
