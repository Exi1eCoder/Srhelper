package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 角色晋升材料模板对象 sr_character_ascension_template
 * 
 * @author quico
 * @date 2026-06-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrCharacterAscensionTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 角色模板类型 */
    @Excel(name = "角色模板类型")
    private String templateType;

    /** 晋升技能类型 */
    @Excel(name = "晋升技能类型")
    private String ascensionType;

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

    /** 材料稀有度 */
    @Excel(name = "材料稀有度")
    private Long rarityLevel;

    /** 排序 */
    private Long sortOrder;


}
