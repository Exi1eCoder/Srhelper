package com.quico.srhelper.mapper;

import com.quico.srhelper.domain.SrCharacterBonusAbilityMaterial;

import java.util.List;

public interface SrCharacterBonusAbilityMaterialMapper
{
    public SrCharacterBonusAbilityMaterial selectById(Long id);

    public List<SrCharacterBonusAbilityMaterial> selectList(SrCharacterBonusAbilityMaterial material);

    public List<SrCharacterBonusAbilityMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterBonusAbilityMaterial material);

    public int update(SrCharacterBonusAbilityMaterial material);

    public int deleteById(Long id);

    public int deleteByIds(Long[] ids);

    public int deleteByCharacterId(Long characterId);
}
