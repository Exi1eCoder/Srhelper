package com.quico.srhelper.domain;

import java.math.BigDecimal;

import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户遗器副词条明细对象 sr_user_relic_sub_affix
 * 
 * @author quico
 * @date 2026-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserRelicSubAffix extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联 sr_user_relic.id */
    private Long userRelicId;

    /** 属性代码 */
    private String property;

    /** 初始档位序号 */
    private Integer baseTier;

    /** 初始数值 */
    private BigDecimal baseValue;

    /** 强化档位序列，逗号分隔 */
    private String upgradeTiers;

    /** 累计强化增量 */
    private BigDecimal upgradeValue;

    /** 最终数值 */
    private BigDecimal totalValue;

    /** 排序值 */
    private Long sortOrder;
}
