package com.quico.srhelper.domain.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 可选限定光锥（standard_type=0）下拉选项
 * 仅用于“添加星缘相遇光锥”弹窗，不含其它字段
 *
 * @author quico
 */
@Data
public class LimitedLightconeOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 光锥ID */
    private Long id;

    /** 光锥名称 */
    private String lightConeName;

    /** 光锥图片 */
    private String image;

    /** 实装版本（便于前端提示光锥来源版本） */
    private String releaseVersion;
}
