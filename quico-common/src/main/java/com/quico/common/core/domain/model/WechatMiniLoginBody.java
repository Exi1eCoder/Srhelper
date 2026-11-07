package com.quico.common.core.domain.model;

/**
 * 微信小程序登录对象
 *
 * @author quico
 */
public class WechatMiniLoginBody
{
    /**
     * wx.login 返回的临时登录凭证
     */
    private String code;

    /**
     * 加密数据（可选，当前前端默认不传）
     */
    private String encryptedData;

    /**
     * 加密算法初始向量（可选，当前前端默认不传）
     */
    private String iv;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getEncryptedData()
    {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData)
    {
        this.encryptedData = encryptedData;
    }

    public String getIv()
    {
        return iv;
    }

    public void setIv(String iv)
    {
        this.iv = iv;
    }
}
