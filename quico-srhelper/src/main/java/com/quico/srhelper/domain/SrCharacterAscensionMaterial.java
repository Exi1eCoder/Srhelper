package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import com.quico.srhelper.domain.enums.AscensionSkillTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色晋升材料配置对象 sr_character_ascension_material
 *
 * @author quico
 * @date 2026-05-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrCharacterAscensionMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "角色ID")
    private Long characterId;

    @Excel(name = "材料类型")
    private String ascensionType;

    @Excel(name = "突破等级")
    private Integer breakLevel;

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

    @Excel(name = "所需经验")
    private Long expRequired;

    @Excel(name = "排序")
    private Integer sortOrder;

    /**
     * 获取晋升类型显示名称
     * @return 类型显示名称
     */
    public String getAscensionTypeName() {
        AscensionSkillTypeEnum typeEnum = AscensionSkillTypeEnum.fromCode(ascensionType);
        return typeEnum != null ? typeEnum.getDesc() : ascensionType;
    }

    /**
     * 判断是否为技能类型（普攻/战技/终结技/天赋）
     * @return true/false
     */
    public boolean isSkillType() {
        AscensionSkillTypeEnum typeEnum = AscensionSkillTypeEnum.fromCode(ascensionType);
        return typeEnum != null && typeEnum.isSkillType();
    }

    /**
     * 判断是否为等级突破类型
     * @return true/false
     */
    public boolean isLevelType() {
        AscensionSkillTypeEnum typeEnum = AscensionSkillTypeEnum.fromCode(ascensionType);
        return typeEnum != null && typeEnum.isLevelType();
    }
}
