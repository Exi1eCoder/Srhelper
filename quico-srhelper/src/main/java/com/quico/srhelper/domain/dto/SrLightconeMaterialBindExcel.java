package com.quico.srhelper.domain.dto;

import com.quico.common.annotation.Excel;
import lombok.Data;

/**
 * 光锥材料绑定导入导出对象（透视格式：一行一个光锥，两列填材料名称）
 *
 * @author quico
 * @date 2026-09-28
 */
@Data
public class SrLightconeMaterialBindExcel
{
    /** 光锥名称 */
    @Excel(name = "光锥名称")
    private String lightconeName;

    /** 世界掉落（TRA，填二星基础材料名称） */
    @Excel(name = "世界掉落")
    private String traItemName;

    /** 拟造花萼（CAL，填二星基础材料名称） */
    @Excel(name = "拟造花萼")
    private String calItemName;
}
