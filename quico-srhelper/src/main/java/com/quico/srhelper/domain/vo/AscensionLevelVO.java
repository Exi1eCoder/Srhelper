package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.util.List;

// 单个晋升等级VO
@Data
public class AscensionLevelVO {
    private Integer level;             // 等级
    private String levelRange;        // 等级范围描述（如 "0→20级"）
    private List<MaterialDisplayVO> materials;  // 材料列表
}
