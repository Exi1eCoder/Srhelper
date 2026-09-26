package com.quico.srhelper.domain.vo;

import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import lombok.Data;

import java.util.List;

/**
 * 角色详情VO
 * 所有材料数据都存储在 sr_character_ascension_material 表中，通过 ascension_type 区分类型
 */
@Data
public class SrCharacterDetailVO {

    /**
     * 角色基本信息
     */
    private SrCharacter character;

    /**
     * 材料绑定列表（世界掉落、拟造花萼、凝滞虚影、历战余响）
     */
    private List<SrCharacterMaterialBind> materialBinds;

    /**
     * 晋升材料列表（等级突破，ascension_type = 'level'）
     */
    private List<SrCharacterAscensionMaterial> ascensions;

    /**
     * 技能材料列表（普攻/战技/终结技/天赋，ascension_type = 'skill'/'talent'/'ultimate'/'basicatk'）
     */
    private List<SrCharacterAscensionMaterial> skills;

    /**
     * 额外能力材料列表（ascension_type = 'bonusability'）
     */
    private List<SrCharacterAscensionMaterial> bonusAbilities;

    /**
     * 额外属性材料列表（ascension_type = 'statbonus'）
     */
    private List<SrCharacterAscensionMaterial> statBonuses;

    /**
     * 角色升级经验配置
     */
    private List<SrCharacterExpUpgradeVO> expUpgrades;
}