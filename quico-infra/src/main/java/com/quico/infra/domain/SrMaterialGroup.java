package com.quico.infra.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;

/**
 * 素材分组对象 sr_material_group
 * 
 * @author quico
 * @date 2026-05-23
 */
public class SrMaterialGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 创建者 */
    @Excel(name = "创建者")
    private String creator;

    /** 分组名 */
    @Excel(name = "分组名")
    private String name;

    /** 更新者 */
    @Excel(name = "更新者")
    private String updater;

    /** 是否删除 */
    @Excel(name = "是否删除")
    private Integer deleted;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCreator(String creator) 
    {
        this.creator = creator;
    }

    public String getCreator() 
    {
        return creator;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setUpdater(String updater) 
    {
        this.updater = updater;
    }

    public String getUpdater() 
    {
        return updater;
    }
    public void setDeleted(Integer deleted) 
    {
        this.deleted = deleted;
    }

    public Integer getDeleted() 
    {
        return deleted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("createTime", getCreateTime())
            .append("creator", getCreator())
            .append("name", getName())
            .append("updater", getUpdater())
            .append("updateTime", getUpdateTime())
            .append("deleted", getDeleted())
            .toString();
    }
}
