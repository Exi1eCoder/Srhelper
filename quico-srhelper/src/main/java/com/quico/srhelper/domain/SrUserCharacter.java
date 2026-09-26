package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 我的角色对象 sr_user_character
 * 
 * @author quico
 * @date 2026-05-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserCharacter extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 角色ID */
    @Excel(name = "角色ID")
    private Long characterId;

    /** 角色等级(1-80) */
    @Excel(name = "角色等级(1-80)")
    private Long level;

    /** 星魂(0-6) */
    @Excel(name = "星魂(0-6)")
    private Long eidolon;

    /** 普攻等级(1-6) */
    @Excel(name = "普攻等级(1-6)")
    private Long basicAtkLevel;

    /** 战技等级(1-10) */
    @Excel(name = "战技等级(1-10)")
    private Long skillLevel;

    /** 终结技等级(1-10) */
    @Excel(name = "终结技等级(1-10)")
    private Long ultimateLevel;

    /** 天赋等级(1-10) */
    @Excel(name = "天赋等级(1-10)")
    private Long talentLevel;

    /** 额外能力1 */
    @Excel(name = "额外能力1")
    private Long bonusAbility1;

    /** 额外能力2 */
    @Excel(name = "额外能力2")
    private Long bonusAbility2;

    /** 额外能力3 */
    @Excel(name = "额外能力3")
    private Long bonusAbility3;

    /** 额外属性1 */
    @Excel(name = "额外属性1")
    private Long statBonus1;

    /** 额外属性2 */
    @Excel(name = "额外属性2")
    private Long statBonus2;

    /** 额外属性3 */
    @Excel(name = "额外属性3")
    private Long statBonus3;

    /** 额外属性4 */
    @Excel(name = "额外属性4")
    private Long statBonus4;

    /** 额外属性5 */
    @Excel(name = "额外属性5")
    private Long statBonus5;

    /** 额外属性6 */
    @Excel(name = "额外属性6")
    private Long statBonus6;

    /** 额外属性7 */
    @Excel(name = "额外属性7")
    private Long statBonus7;

    /** 额外属性8 */
    @Excel(name = "额外属性8")
    private Long statBonus8;

    /** 额外属性9 */
    @Excel(name = "额外属性9")
    private Long statBonus9;

    /** 额外属性10 */
    @Excel(name = "额外属性10")
    private Long statBonus10;


}
