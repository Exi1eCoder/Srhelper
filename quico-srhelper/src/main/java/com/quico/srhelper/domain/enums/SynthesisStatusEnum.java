package com.quico.srhelper.domain.enums;

/**
 * 材料合成折算状态
 * 用于描述单个目标材料在"低阶合成高阶"折算后的缺口情况
 */
public enum SynthesisStatusEnum {

    /** 完全够：持有量已满足自身需求，无需合成 */
    ENOUGH,

    /** 通过合成足够：自身持有不足，但可由低阶材料合成补足 */
    ENOUGH_BY_SYNTHESIS,

    /** 合成了一部分仍不够：低阶仅能补足一部分，仍有缺口需刷取 */
    PARTIAL_SYNTHESIS,

    /** 需刷取：低阶不足以合成，缺口全部需刷取 */
    NEED_FARM
}
