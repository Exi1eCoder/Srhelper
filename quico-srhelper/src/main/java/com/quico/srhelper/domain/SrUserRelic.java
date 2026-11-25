package com.quico.srhelper.domain;

import java.math.BigDecimal;
import java.util.List;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户遗器对象 sr_user_relic
 * 
 * @author quico
 * @date 2026-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserRelic extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 遗器ID */
    @Excel(name = "遗器ID")
    private String relicId;

    /** 当前等级 */
    @Excel(name = "当前等级")
    private Integer level;

    /** 初始副词条数 */
    @Excel(name = "初始副词条数")
    private Integer initialSubCount;

    /** 已强化次数 */
    @Excel(name = "已强化次数")
    private Integer subUpgradeUsed;

    /** 当前主属性代码 */
    @Excel(name = "主属性")
    private String mainProperty;

    /** 当前主属性数值 */
    private BigDecimal mainValue;

    /** 装备角色ID */
    private Long equippedBy;

    /** 是否锁定 */
    private Boolean locked;

    /** 来源：SIMULATED / IMPORTED / IMPORTED_MODIFIED */
    @Excel(name = "来源")
    private String origin;

    /** 排序值 */
    private Long sortOrder;

    /** 遗器名称（查询关联） */
    private String relicName;

    /** 部位（冗余自 sr_relic） */
    private String slot;

    /** 图标URL（查询关联） */
    private String icon;

    /** 稀有度（查询关联） */
    private Integer rarity;

    /** 套装ID（查询关联） */
    private String setId;

    /** 套装名称（查询关联） */
    private String setName;

    /** 副词条明细（详情/保存返回） */
    private List<SrUserRelicSubAffix> subAffixes;
}
