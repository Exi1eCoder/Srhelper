package com.quico.srhelper.exception;

import com.quico.common.constant.HttpStatus;
import com.quico.common.exception.BusinessException;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;

/**
 * 抽卡记录上传校验异常
 * 携带真实死因，供风控审计落库与信用分级使用
 *
 *
 * @author quico
 */
public class UploadValidateException extends BusinessException
{
    private static final long serialVersionUID = 1L;

    private final ViolationTypeEnum violationType;

    public UploadValidateException(ViolationTypeEnum violationType, String message)
    {
        super(HttpStatus.BAD_REQUEST, message);
        this.violationType = violationType;
    }

    public ViolationTypeEnum getViolationType()
    {
        return violationType;
    }
}
