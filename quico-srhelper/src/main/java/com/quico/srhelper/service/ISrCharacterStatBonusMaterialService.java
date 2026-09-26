package com.quico.srhelper.service;

import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;

import java.util.List;

public interface ISrCharacterStatBonusMaterialService
{
    public SrCharacterStatBonusMaterial selectById(Long id);

    public List<SrCharacterStatBonusMaterial> selectList(SrCharacterStatBonusMaterial material);

    public List<SrCharacterStatBonusMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterStatBonusMaterial material);

    public int update(SrCharacterStatBonusMaterial material);

    public int deleteByIds(Long[] ids);

    public int deleteById(Long id);

    public int deleteByCharacterId(Long characterId);
}
