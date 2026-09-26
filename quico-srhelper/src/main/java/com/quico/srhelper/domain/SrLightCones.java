package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 光锥一览对象 sr_light_cones
 * 
 * @author quico
 * @date 2026-05-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrLightCones extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 光锥名称 */
    @Excel(name = "光锥名称")
    private String lightConeName;

    /** 光锥图片URL */
    @Excel(name = "光锥图片URL")
    private String image;

    /** 星级 */
    @Excel(name = "星级", dictType = "star_level")
    private Long starLevel;

    /** 命途 */
    @Excel(name = "命途", dictType = "sr_path")
    private String path;

    /** 标签 */
    @Excel(name = "标签")
    private String tag;

    /** 实装版本 */
    @Excel(name = "实装版本")
    private String releaseVersion;

    /** 简介 */
    @Excel(name = "简介")
    private String description;

//    /** 排序值（新增时自动设时间戳，值越大越靠前） */
//    @Excel(name = "排序值")
    private Long sortOrder;
}
