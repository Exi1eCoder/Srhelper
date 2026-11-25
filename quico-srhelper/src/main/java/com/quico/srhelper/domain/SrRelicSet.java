package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 遗器一览对象 sr_relic_set
 * 
 * @author quico
 * @date 2026-11-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrRelicSet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 套装业务ID */
    @Excel(name = "套装业务ID")
    private String setId;

    /** 套装名称 */
    @Excel(name = "套装名称")
    private String setName;

    /** 2件套效果描述 */
    @Excel(name = "2件套效果描述")
    private String desc2pc;

    /** 4件套效果描述 */
    @Excel(name = "4件套效果描述")
    private String desc4pc;

    /** 遗器类型 */
    @Excel(name = "遗器类型")
    private String setCategory;

    /** 套装图标URL */
    @Excel(name = "套装图标URL")
    private String icon;

    /** 实装版本 */
    @Excel(name = "实装版本")
    private String releaseVersion;

    /** 排序值 */
    private Long sortOrder;


}
