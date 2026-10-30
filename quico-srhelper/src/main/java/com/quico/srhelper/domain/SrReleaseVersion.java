package com.quico.srhelper.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 游戏版本信息对象 sr_release_version
 * 
 * @author quico
 * @date 2026-10-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrReleaseVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 大版本号 */
    @Excel(name = "大版本号")
    private String majorVersion;

    /** 版本号 */
    @Excel(name = "版本号")
    private String versionCode;

    /** 版本主题名称 */
    @Excel(name = "版本主题名称")
    private String versionName;

    /** 版本图片URL */
    @Excel(name = "版本图片URL")
    private String image;

    /** 实装时间  */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实装时间 ", width = 30, dateFormat = "yyyy-MM-dd")
    private Date releaseTime;

    /** 排序值 */
    private Long sortOrder;

    /** 状态 (0-正常, 1-停用) */
    @Excel(name = "状态 (0-正常, 1-停用)")
    private Long status;


}
