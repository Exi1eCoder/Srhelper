package com.quico.srhelper.domain.vo;

import com.quico.srhelper.domain.SrItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 材料详情VO
 * 包含当前材料信息和相关高阶素材
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SrItemDetailVO extends SrItem {

    private static final long serialVersionUID = 1L;

    /** 二星素材（当当前为三星/四星时返回） */
    private SrItem item2Star;

    /** 三星素材（当当前为二星/四星时返回） */
    private SrItem item3Star;

    /** 四星素材（当当前为二星/三星时返回） */
    private SrItem item4Star;
}