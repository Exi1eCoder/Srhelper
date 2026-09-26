package com.quico.srhelper.domain.dto;

import com.quico.srhelper.domain.SrItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 材料高阶素材绑定DTO
 * 继承SrItem，添加高阶素材绑定字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SrItemBindDTO extends SrItem {

    private static final long serialVersionUID = 1L;

    /** 三星高阶素材ID */
    private Long advancedItem3Star;

    /** 四星高阶素材ID */
    private Long advancedItem4Star;
}