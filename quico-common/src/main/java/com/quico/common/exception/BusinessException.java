package com.quico.common.exception;

import com.quico.common.constant.HttpStatus;

/**
 * 业务校验异常
 *
 * 用于业务校验失败场景，携带目标 HTTP 状态码，由全局异常处理器按该状态码返回，
 * 便于前端精确拦截和回滚。
 *
 * 说明：ServiceException 声明为 final（作者不允许被继承），因此本类独立继承 RuntimeException，
 * 与其无继承关系。
 *
 * 约定：
 * - 400 用于入参/格式校验失败（{@link #badRequest(String)}）
 * - 422 用于业务规则校验失败，如超出上限（{@link #unprocessable(String)}，默认）
 *
 * @author quico
 */
public class BusinessException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 目标 HTTP 状态码
     */
    private final Integer code;

    /**
     * 业务规则校验失败，默认返回 422
     */
    public BusinessException(String message)
    {
        this(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    /**
     * 指定 HTTP 状态码
     */
    public BusinessException(Integer code, String message)
    {
        super(message);
        this.code = code;
    }

    public Integer getCode()
    {
        return code;
    }

    /**
     * 入参/格式校验失败，返回 400
     */
    public static BusinessException badRequest(String message)
    {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 业务规则校验失败，返回 422
     */
    public static BusinessException unprocessable(String message)
    {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
