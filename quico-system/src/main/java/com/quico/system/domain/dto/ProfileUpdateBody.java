package com.quico.system.domain.dto;

import lombok.Data;

/**
 * 个人中心资料修改请求体
 *
 * 保留 SysUser 的原始字段名，前端无需改动既有字段，仅在修改邮箱时增加 code。
 *
 * @author quico
 */
@Data
public class ProfileUpdateBody
{
    /** 用户昵称 */
    private String nickName;

    /** 用户邮箱 */
    private String email;

    /** 手机号码 */
    private String phonenumber;

    /** 用户性别 */
    private String sex;

    /** 邮箱验证码：仅当邮箱发生变更时必填 */
    private String code;

    /** 手机验证码：仅当手机号发生变更时必填 */
    private String phoneCode;
}
