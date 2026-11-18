package com.quico.srhelper.domain.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 卡池项目可绑定候选（角色/光锥）下拉选项
 * 仅返回尚未绑定到 sr_gacha_item.sr_id 的数据，供新增/编辑卡池项目时选择
 *
 * @author quico
 */
@Data
public class GachaItemBindOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色/光锥主键ID（写入 sr_gacha_item.sr_id） */
    private Long id;

    /** 名称（写入 sr_gacha_item.item_name） */
    private String itemName;

    /** 项目类型：character / lightcone */
    private String itemType;

    /** 头像或图片URL */
    private String iconUrl;

    /** 星级（来源表 star_level，写入 sr_gacha_item.rank_type） */
    private Long starLevel;

    /** 命途 */
    private String path;
}
