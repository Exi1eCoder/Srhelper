package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户持有光锥对象 sr_user_light_cone
 * 
 * @author quico
 * @date 2026-06-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserLightCone extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 光锥ID */
    @Excel(name = "光锥ID")
    private Long lightConeId;

    /** 光锥等级(1-80) */
    @Excel(name = "光锥等级")
    private Integer level;

    /** 叠影等级(1-5) */
    @Excel(name = "叠影等级")
    private Integer superimposition;

    /** 光锥名称（查询时关联） */
    private String lightConeName;

    /** 光锥图片（查询时关联） */
    private String lightConeImage;

    /** 星级（查询时关联） */
    private Integer starLevel;

    /** 命途（查询时关联） */
    private String path;

}