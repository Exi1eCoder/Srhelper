package com.quico.srhelper.domain.dto;

import com.quico.common.annotation.Excel;
import lombok.Data;

/**
 * 角色材料绑定导入导出对象（透视格式：一行一个角色，四类材料各一列，填材料名称）
 *
 * @author quico
 * @date 2026-09-27
 */
@Data
public class SrCharacterMaterialBindExcel
{
    /** 角色名称 */
    @Excel(name = "角色名称")
    private String characterName;

    /** 世界掉落（TRA，填二星基础材料名称） */
    @Excel(name = "世界掉落")
    private String traItemName;

    /** 拟造花萼（CAL，填二星基础材料名称） */
    @Excel(name = "拟造花萼")
    private String calItemName;

    /** 凝滞虚影（SS） */
    @Excel(name = "凝滞虚影")
    private String ssItemName;

    /** 历战余响（EOW） */
    @Excel(name = "历战余响")
    private String eowItemName;
}
