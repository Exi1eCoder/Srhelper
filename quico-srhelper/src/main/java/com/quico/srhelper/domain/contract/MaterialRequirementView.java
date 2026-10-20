package com.quico.srhelper.domain.vo;

/**
 * 材料需求视图（角色、光锥材料需求列表的统一读取契约）
 *
 * 合成折算只依赖三个字段：材料ID、所需数量、用户持有数量。
 * 差额（difference）仅用于前端展示，不能参与折算——
 * 因为它丢失了"持有量大于需求量"的富余信息，而低阶富余才是向上合成的来源。
 *
 * @author quico
 */
public interface MaterialRequirementView {

    /** 物品ID */
    Long getItemId();

    /** 所需总数 */
    Long getRequiredQuantity();

    /** 背包持有数量 */
    Long getOwnedQuantity();
}
