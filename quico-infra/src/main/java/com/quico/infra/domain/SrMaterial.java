package com.quico.infra.domain;

import java.util.List;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 素材库对象 sr_material
 * 
 * @author quico
 * @date 2026-05-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 创建者 */
    @Excel(name = "创建者")
    private String creator;

    /** 类型1、图片；2、视频 */
    @Excel(name = "类型1、图片；2、视频")
    private String type;

    /** 分组ID */
    @Excel(name = "分组ID")
    private String groupId;

    /** 素材名 */
    @Excel(name = "素材名")
    private String name;

    /** 素材链接 */
    @Excel(name = "素材链接")
    private String url;

    /** 更新者 */
    @Excel(name = "更新者")
    private String updater;

    /** 是否删除 */
    private Integer deleted;

    /** 租户编号 */
    private Long tenantId;

    /** 素材分组信息 */
    private List<SrMaterialGroup> srMaterialGroupList;


}
