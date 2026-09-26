package com.quico.srhelper.service;

import com.quico.srhelper.domain.SrCharacterSkillMaterial;

import java.util.List;

public interface ISrCharacterSkillMaterialService
{
    public SrCharacterSkillMaterial selectById(Long id);

    public List<SrCharacterSkillMaterial> selectList(SrCharacterSkillMaterial material);

    public List<SrCharacterSkillMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterSkillMaterial material);

    public int update(SrCharacterSkillMaterial material);

    public int deleteByIds(Long[] ids);

    public int deleteById(Long id);

    public int deleteByCharacterId(Long characterId);
}
