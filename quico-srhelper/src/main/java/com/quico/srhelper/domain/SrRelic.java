package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 遗器图鉴对象 sr_relic
 * 
 * @author quico
 * @date 2026-11-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrRelic extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 遗器ID */
    @Excel(name = "遗器ID")
    private String relicId;

    /** 套装ID */
    @Excel(name = "套装ID")
    private String setId;

    /** 遗器名称 */
    @Excel(name = "遗器名称")
    private String relicName;

    /** 稀有度 */
    @Excel(name = "稀有度")
    private Long rarity;

    /** 部位 */
    @Excel(name = "部位")
    private String slot;

    /** 最高等级 */
    @Excel(name = "最高等级")
    private Long maxLevel;

    /** 主属性组ID */
    @Excel(name = "主属性组ID")
    private String mainAffixId;

    /** 副属性组ID */
    @Excel(name = "副属性组ID")
    private String subAffixId;

    /** 图标URL */
    @Excel(name = "图标URL")
    private String icon;

    /** 排序值 */
    private Long sortOrder;


}
