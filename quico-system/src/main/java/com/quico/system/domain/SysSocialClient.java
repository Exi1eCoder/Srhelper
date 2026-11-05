package com.quico.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;

/**
 * JustAuth三方登录配置对象 sys_social_client
 * 
 * @author quico
 * @date 2026-11-03
 */
public class SysSocialClient extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long clientId;

    /** 平台标识 */
    @Excel(name = "平台标识")
    private String source;

    /** 客户端名称 */
    @Excel(name = "客户端名称")
    private String clientName;

    /** 平台 AppID */
    @Excel(name = "平台 AppID")
    private String appId;

    /** 平台 AppSecret */
    @Excel(name = "平台 AppSecret")
    private String appSecret;

    /** 回调地址 */
    @Excel(name = "回调地址")
    private String redirectUri;

    /** 企业微信等平台的 AgentId */
    @Excel(name = "企业微信等平台的 AgentId")
    private String agentId;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getClientId()
    {
        return clientId;
    }

    public void setClientId(Long clientId)
    {
        this.clientId = clientId;
    }

    @NotBlank(message = "平台标识不能为空")
    @Size(min = 0, max = 32, message = "平台标识长度不能超过32个字符")
    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    @Size(min = 0, max = 64, message = "客户端名称长度不能超过64个字符")
    public String getClientName()
    {
        return clientName;
    }

    public void setClientName(String clientName)
    {
        this.clientName = clientName;
    }

    @NotBlank(message = "平台 AppID 不能为空")
    @Size(min = 0, max = 128, message = "平台 AppID 长度不能超过128个字符")
    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    @NotBlank(message = "平台 AppSecret 不能为空")
    @Size(min = 0, max = 512, message = "平台 AppSecret 长度不能超过512个字符")
    public String getAppSecret()
    {
        return appSecret;
    }

    public void setAppSecret(String appSecret)
    {
        this.appSecret = appSecret;
    }

    @Size(min = 0, max = 512, message = "回调地址长度不能超过512个字符")
    public String getRedirectUri()
    {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri)
    {
        this.redirectUri = redirectUri;
    }

    @Size(min = 0, max = 64, message = "AgentId 长度不能超过64个字符")
    public String getAgentId()
    {
        return agentId;
    }

    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("clientId", getClientId())
            .append("source", getSource())
            .append("clientName", getClientName())
            .append("appId", getAppId())
            .append("appSecret", getAppSecret())
            .append("redirectUri", getRedirectUri())
            .append("agentId", getAgentId())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
