package com.quico.srhelper.domain.vo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 星缘相遇光锥（standard_type=2）精简视图
 * 仅用于后台“已加入星缘相遇”光锥列表，不含其它冗余字段
 *
 * @author quico
 */
@Data
public class CelestialLightconeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 光锥ID */
    private Long id;

    /** 光锥名称 */
    private String lightConeName;

    /** 光锥图片 */
    private String image;

    /** 加入常驻池时间（取版本实装时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date standardStartTime;
}
