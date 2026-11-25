package com.quico.srhelper.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 遗器副属性词条对象 sr_relic_sub_affix
 * 
 * @author quico
 * @date 2026-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrRelicSubAffix implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 词条组ID */
    private String affixGroupId;

    /** 组内词条ID */
    private String affixId;

    /** 属性代码 */
    private String property;

    /** 初始数值 */
    private BigDecimal base;

    /** 每次强化提升的增量 */
    private BigDecimal step;

    /** 最大随机强化次数 */
    private Integer stepNum;

    /** 排序值 */
    private Long sortOrder;
}
