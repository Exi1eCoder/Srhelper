package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 光锥晋升素材模板对象 sr_lightcone_ascension_template
 * 
 * @author quico
 * @date 2026-06-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrLightconeAscensionTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 光锥模板类型 */
    @Excel(name = "光锥模板类型")
    private String templateType;

    /** 突破等级 */
    @Excel(name = "突破等级")
    private Long breakLevel;

    /** 材料槽位 */
    @Excel(name = "材料槽位")
    private Long materialSlot;

    /** 材料ID或占位符 */
    @Excel(name = "材料ID或占位符")
    private String itemId;

    /** 所需数量 */
    @Excel(name = "所需数量")
    private Long quantity;

    /** 排序 */
    private Long sortOrder;


}
