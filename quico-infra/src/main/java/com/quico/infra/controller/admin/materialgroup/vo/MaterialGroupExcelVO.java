package com.quico.infra.controller.admin.materialgroup.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 素材分组 Excel VO
 *
 * @author quico
 */
@Data
public class MaterialGroupExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("分组名")
    private String name;

}
