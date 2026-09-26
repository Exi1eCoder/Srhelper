package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 光锥晋升材料对象 sr_light_cone_ascension
 *
 * @author quico
 * @date 2026-05-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrLightConeAscension extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 光锥ID */
    @Excel(name = "光锥ID")
    private Long lightConeId;

    /** 突破阶段（1-7） */
    @Excel(name = "突破阶段")
    private Integer ascensionPhase;

    /** 突破等级（20,30,40,50,60,70,80） */
    @Excel(name = "突破等级")
    private Integer breakLevel;

    /** 材料槽位（同阶段多材料时区分） */
    @Excel(name = "材料槽位")
    private Integer materialSlot;

    /** 所需物品ID */
    private Long itemId;

    /** 物品名称（前端展示用） */
    @Excel(name = "物品名称")
    private String itemName;

    /** 物品图片（前端展示用） */
    private String itemImage;

    /** 材料稀有度（2绿 3蓝 4紫 5金） */
    @Excel(name = "材料稀有度", readConverterExp = "2=绿,3=蓝,4=紫,5=金")
    private Integer rarityLevel;

    /** 所需数量 */
    @Excel(name = "所需数量")
    private Integer quantity;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sortOrder;

}
