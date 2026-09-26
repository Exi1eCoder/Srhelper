package com.quico.srhelper.service.impl;

import com.quico.common.utils.DateUtils;
import com.quico.srhelper.domain.SrCharacterBonusAbilityMaterial;
import com.quico.srhelper.mapper.SrCharacterBonusAbilityMaterialMapper;
import com.quico.srhelper.service.ISrCharacterBonusAbilityMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SrCharacterBonusAbilityMaterialServiceImpl implements ISrCharacterBonusAbilityMaterialService
{
    @Autowired
    private SrCharacterBonusAbilityMaterialMapper mapper;

    @Override
    public SrCharacterBonusAbilityMaterial selectById(Long id) { return mapper.selectById(id); }

    @Override
    public List<SrCharacterBonusAbilityMaterial> selectList(SrCharacterBonusAbilityMaterial material) { return mapper.selectList(material); }

    @Override
    public List<SrCharacterBonusAbilityMaterial> selectByCharacterId(Long characterId) { return mapper.selectByCharacterId(characterId); }

    @Override
    public int insert(SrCharacterBonusAbilityMaterial material) {
        material.setCreateTime(DateUtils.getNowDate());
        return mapper.insert(material);
    }

    @Override
    public int update(SrCharacterBonusAbilityMaterial material) {
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
