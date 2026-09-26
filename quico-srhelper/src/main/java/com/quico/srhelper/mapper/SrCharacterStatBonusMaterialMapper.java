package com.quico.srhelper.mapper;

import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;

import java.util.List;

public interface SrCharacterStatBonusMaterialMapper
{
    public SrCharacterStatBonusMaterial selectById(Long id);

    public List<SrCharacterStatBonusMaterial> selectList(SrCharacterStatBonusMaterial material);

    public List<SrCharacterStatBonusMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterStatBonusMaterial material);

    public int update(SrCharacterStatBonusMaterial material);

    public int deleteById(Long id);

    public int deleteByIds(Long[] ids);

    public int deleteByCharacterId(Long characterId);
}
