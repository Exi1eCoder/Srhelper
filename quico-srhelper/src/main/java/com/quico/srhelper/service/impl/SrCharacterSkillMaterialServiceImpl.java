package com.quico.srhelper.service.impl;

import com.quico.common.utils.DateUtils;
import com.quico.srhelper.domain.SrCharacterSkillMaterial;
import com.quico.srhelper.mapper.SrCharacterSkillMaterialMapper;
import com.quico.srhelper.service.ISrCharacterSkillMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SrCharacterSkillMaterialServiceImpl implements ISrCharacterSkillMaterialService
{
    @Autowired
    private SrCharacterSkillMaterialMapper mapper;

    @Override
    public SrCharacterSkillMaterial selectById(Long id) { return mapper.selectById(id); }

    @Override
    public List<SrCharacterSkillMaterial> selectList(SrCharacterSkillMaterial material) { return mapper.selectList(material); }

    @Override
    public List<SrCharacterSkillMaterial> selectByCharacterId(Long characterId) { return mapper.selectByCharacterId(characterId); }

    @Override
    public int insert(SrCharacterSkillMaterial material) {
        material.setCreateTime(DateUtils.getNowDate());
        return mapper.insert(material);
    }

    @Override
    public int update(SrCharacterSkillMaterial material) {
        material.setUpdateTime(DateUtils.getNowDate());
        return mapper.update(material);
    }

    @Override
    public int deleteByIds(Long[] ids) { return mapper.deleteByIds(ids); }

    @Override
    public int deleteById(Long id) { return mapper.deleteById(id); }

    @Override
    public int deleteByCharacterId(Long characterId) { return mapper.deleteByCharacterId(characterId); }
}
