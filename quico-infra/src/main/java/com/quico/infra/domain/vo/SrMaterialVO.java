package com.quico.infra.domain.vo;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrMaterialVO {
    /**
     * 素材库ID
     */
    private Long id;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 创建人名称
     */
    private String creatorName;
    /**
     * 素材类型
     */
    private String type;
    /**
     * 素材分组ID
     */
    private String groupId;
    /**
     * 素材分组名称
     */
    private String groupName;
    /**
     * 素材名称
     */
    private String name;
    /**
     * 素材URL
     */
    private String url;
    /**
     * 更新人
     */
    private String updater;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 删除标识
     */
    private Integer deleted;
    /**
     * 租户ID
     */
    private Long tenantId;
}
