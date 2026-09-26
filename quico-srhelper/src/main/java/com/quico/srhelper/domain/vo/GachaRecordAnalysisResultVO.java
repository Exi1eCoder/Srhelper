package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class GachaRecordAnalysisResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 整体首次抽卡时间 */
    private Date firstPullTime;

    /** 整体末次抽卡时间 */
    private Date lastPullTime;

    /** 按卡池类型分组的五星记录 */
    private List<GachaRecordAnalysisVO> groups;
}