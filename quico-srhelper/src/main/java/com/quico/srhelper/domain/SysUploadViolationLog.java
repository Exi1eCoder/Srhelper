package com.quico.srhelper.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传违规审计日志对象 sys_upload_violation_log
 * 只追加不更新，因此不继承 BaseEntity
 *
 * @author quico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUploadViolationLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** IP地址 */
    private String ipAddress;

    /** 文件名 */
    private String fileName;

    /** 文件大小 */
    private Long fileSize;

    /** 违规类型 */
    private String violationType;

    /** 严重程度：0-轻度，1-重度 */
    private Integer isMalicious;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private Date createTime;
}
