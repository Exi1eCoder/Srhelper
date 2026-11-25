package com.quico.srhelper.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 遗器套装静态属性效果对象 sr_relic_set_property
 * 
 * @author quico
 * @date 2026-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrRelicSetProperty implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 套装业务ID */
    private String setId;

    /** 生效件数档位 */
    private Integer pieceCount;

    /** 属性代码 */
    private String property;

    /** 属性数值 */
    private BigDecimal value;

    /** 排序值 */
    private Long sortOrder;
}
