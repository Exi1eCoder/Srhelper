package com.quico.srhelper.domain.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡池物品常驻信息（抽卡“是否歪”判定用）
 *
 * 独立值对象，与 GachaRecordImageCacheManager 解耦，
 * 由缓存加载时构造，供抽卡分析逻辑读取。
 *
 * @author quico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 常驻类型：见 StandardType（1-基础常驻 2-星缘相遇） */
    private Integer standardType;

    /** 加入常驻池时间，为空表示一直在常驻池 */
    private Date standardStartTime;
}
