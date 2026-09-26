package com.quico.srhelper.domain.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 抽卡分析 - 按卡池类型分组的五星记录
 */
@Data
public class GachaRecordAnalysisVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卡池类型 */
    private String gachaType;

    /** 该卡池类型下的五星记录明细（按时间升序） */
    private List<GachaRecordAnalysisItemVO> items;
}
