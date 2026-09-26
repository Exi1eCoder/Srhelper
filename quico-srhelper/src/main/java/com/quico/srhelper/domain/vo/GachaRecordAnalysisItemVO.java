package com.quico.srhelper.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 抽卡分析 - 五星记录明细
 */
@Data
public class GachaRecordAnalysisItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 名称 */
    private String name;

    /** 物品类型 */
    private String itemType;

    /** 保底内抽数 */
    private Integer pityCount;

    /** 抽卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /** 图片URL */
    private String imageUrl;
}
