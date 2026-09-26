package com.quico.srhelper.mapper;

import com.quico.srhelper.domain.SrCharacterSkillMaterial;

import java.util.List;

public interface SrCharacterSkillMaterialMapper
{
    public SrCharacterSkillMaterial selectById(Long id);

    public List<SrCharacterSkillMaterial> selectList(SrCharacterSkillMaterial material);

    public List<SrCharacterSkillMaterial> selectByCharacterId(Long characterId);

    public List<SrCharacterSkillMaterial> selectByCharacterIdAndType(Long characterId, String skillType);

    public int insert(SrCharacterSkillMaterial material);

    public int update(SrCharacterSkillMaterial material);

    public int deleteById(Long id);

    public int deleteByIds(Long[] ids);

    public int deleteByCharacterId(Long characterId);
}
