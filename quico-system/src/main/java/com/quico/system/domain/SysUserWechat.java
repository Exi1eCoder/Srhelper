package com.quico.system.domain;

import com.quico.common.core.domain.BaseEntity;

/**
 * 微信小程序用户绑定表 sys_user_wechat
 *
 * @author quico
 */
public class SysUserWechat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 系统用户ID */
    private Long userId;

    /** 小程序AppId */
    private String appid;

    /** 微信openId */
    private String openid;

    /** 微信unionId */
    private String unionid;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getAppid()
    {
        return appid;
    }

    public void setAppid(String appid)
    {
        this.appid = appid;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public String getUnionid()
    {
        return unionid;
    }

    public void setUnionid(String unionid)
    {
        this.unionid = unionid;
    }
}
