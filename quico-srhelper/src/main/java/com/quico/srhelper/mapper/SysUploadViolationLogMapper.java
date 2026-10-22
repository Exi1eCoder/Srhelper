package com.quico.srhelper.mapper;

import com.quico.srhelper.domain.SysUploadViolationLog;

/**
 * 上传违规审计日志Mapper接口
 *
 * @author quico
 */
public interface SysUploadViolationLogMapper
{
    /**
     * 新增上传违规审计日志
     *
     * @param log 审计日志
     * @return 结果
     */
    int insertSysUploadViolationLog(SysUploadViolationLog log);
}
