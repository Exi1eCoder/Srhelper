package com.quico.srhelper.domain.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 套装下的遗器部件视图
 * 仅包含前端展示所需的名称、部位、图标
 *
 * @author quico
 */
@Data
public class SrRelicPieceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套装业务ID */
    private String setId;

    /** 套装名称 */
    private String setName;

    /** 遗器ID */
    private String relicId;

    /** 遗器名称 */
    private String relicName;

    /** 部位 */
    private String slot;

    /** 图标URL */
    private String icon;
}
