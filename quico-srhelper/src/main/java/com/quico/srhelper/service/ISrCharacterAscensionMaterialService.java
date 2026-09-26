package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;

public interface ISrCharacterAscensionMaterialService
{
    public SrCharacterAscensionMaterial selectById(Long id);

    public List<SrCharacterAscensionMaterial> selectList(SrCharacterAscensionMaterial material);

    public List<SrCharacterAscensionMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterAscensionMaterial material);

    public int update(SrCharacterAscensionMaterial material);

    public int deleteByIds(Long[] ids);

    public int deleteById(Long id);

    public int deleteByCharacterId(Long characterId);
}
