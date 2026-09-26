package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.util.List;

// 晋升材料分组VO
@Data
public class AscensionGroupVO {
    private String typeName;           // 类型名称
    private List<AscensionLevelVO> levels;  // 各等级配置
}
