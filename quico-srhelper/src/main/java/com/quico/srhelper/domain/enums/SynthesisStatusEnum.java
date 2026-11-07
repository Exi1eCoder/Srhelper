package com.quico.srhelper.domain.enums;

/**
 * 材料养成折算的整体状态
 * 描述一个角色/光锥（或"总体"维度）在"低阶合成高阶"折算后的完成情况：
 * 是否存在可用的合成方案，以及是否仍有材料必须刷取。
 */
public enum SynthesisStatusEnum {

    /** 完全够：无任何缺口，无需合成、无需刷取 */
    ENOUGH,

    /** 通过合成足够：存在合成方案，且合成后无缺口 */
    ENOUGH_BY_SYNTHESIS,

    /** 合成一部分仍不够：存在合成方案，但仍有材料需刷取 */
    PARTIAL_SYNTHESIS,

    /** 需刷取：不存在任何合成方案，缺口全部需刷取 */
    NEED_FARM;

    /**
     * 追加一项"无法合成、必须刷取"的需求（如经验书缺口）后的整体状态：
     * - 原本全部满足（ENOUGH）→ 只剩刷取，变为 NEED_FARM；
     * - 原本可由合成满足（ENOUGH_BY_SYNTHESIS）→ 变为合成一部分仍需刷取 PARTIAL_SYNTHESIS；
     * - 已存在刷取缺口（PARTIAL_SYNTHESIS / NEED_FARM）→ 不变。
     */
    public SynthesisStatusEnum withFarmRequired() {
        if (this == ENOUGH) {
            return NEED_FARM;
        }
        if (this == ENOUGH_BY_SYNTHESIS) {
            return PARTIAL_SYNTHESIS;
        }
        return this;
    }
}
