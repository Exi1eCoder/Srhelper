package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 材料一览对象 sr_item
 * 
 * @author quico
 * @date 2026-05-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 物品名称 */
    @Excel(name = "物品名称")
    private String itemName;

    /** 物品图像URL */
    @Excel(name = "物品图像URL")
    private String image;

    /** 星级（1-5星） */
    @Excel(name = "星级", dictType = "star_level")
    private Long starLevel;

    /** 类型 */
    @Excel(name = "类型", dictType = "sr_item_type")
    private String itemType;

    /** 晋升素材类型 */
    @Excel(name = "晋升素材类型", dictType = "sr_ascension_type")
    private String ascensionType;

    /** 素材系列ID */
    private Long seriesId;

    /** 战斗属性 */
    @Excel(name = "战斗属性", dictType = "sr_combat_types")
    private String combatType;

    /** 命途 */
    @Excel(name = "命途", dictType = "sr_path")
    private String path;

    /** 标签 */
    @Excel(name = "标签")
    private String tag;

    
    /** 实装版本（字典 sr_release_version 的 dictValue 与 dictLabel 相同，导入导出均按文本处理，字典校验在业务层完成） */
    @Excel(name = "实装版本")
    private String releaseVersion;

    /** 简介 */
    @Excel(name = "简介")
    private String description;
//
//    /** 排序值（新增时自动设时间戳，值越大越靠前） */
//    @Excel(name = "排序值")
    private Long sortOrder;
}
