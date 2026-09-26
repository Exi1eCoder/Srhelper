package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 角色材料绑定对象 sr_character_material_bind
 * 
 * @author quico
 * @date 2026-06-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrCharacterMaterialBind extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 角色ID */
    @Excel(name = "角色ID")
    private Long characterId;

    /** 角色名称 */
    @Excel(name = "角色名称")
    private String characterName;

    /** 材料类型 */
    @Excel(name = "材料类型")
    private String itemAscensionType;

    /** 材料ID */
    @Excel(name = "材料ID")
    private Long itemId;

    /** 材料系列ID */
    @Excel(name = "材料系列ID")
    private Long seriesId;

    /** 材料名称 */
    @Excel(name = "材料名称")
    private String itemName;

    /** 材料图片 */
    @Excel(name = "材料图片")
    private String itemImage;

    /** 材料稀有度 */
    @Excel(name = "材料稀有度")
    private Integer rarityLevel;

    /** 排序 */
    private Integer sortOrder;


}
