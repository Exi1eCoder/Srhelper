package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色额外属性材料配置对象 sr_character_stat_bonus_material
 *
 * @author quico
 * @date 2026-05-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrCharacterStatBonusMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "角色ID")
    private Long characterId;

    @Excel(name = "属性序号")
    private Integer statIndex;

    @Excel(name = "材料槽位")
    private Integer materialSlot;

    private Long itemId;

    @Excel(name = "物品名称")
    private String itemName;

    @Excel(name = "物品图片")
    private String itemImage;

    @Excel(name = "材料稀有度", readConverterExp = "2=绿,3=蓝,4=紫,5=金")
    private Integer rarityLevel;

    @Excel(name = "所需数量")
    private Integer quantity;

    @Excel(name = "排序")
    private Integer sortOrder;

}
