package com.quico.srhelper.service.impl;

import com.quico.common.utils.DateUtils;
import com.quico.srhelper.domain.SrCharacterStatBonusMaterial;
import com.quico.srhelper.mapper.SrCharacterStatBonusMaterialMapper;
import com.quico.srhelper.service.ISrCharacterStatBonusMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SrCharacterStatBonusMaterialServiceImpl implements ISrCharacterStatBonusMaterialService
{
    @Autowired
    private SrCharacterStatBonusMaterialMapper mapper;

    @Override
    public SrCharacterStatBonusMaterial selectById(Long id) { return mapper.selectById(id); }

    @Override
    public List<SrCharacterStatBonusMaterial> selectList(SrCharacterStatBonusMaterial material) { return mapper.selectList(material); }

    @Override
    public List<SrCharacterStatBonusMaterial> selectByCharacterId(Long characterId) { return mapper.selectByCharacterId(characterId); }

    @Override
    public int insert(SrCharacterStatBonusMaterial material) {
        material.setCreateTime(DateUtils.getNowDate());
        return mapper.insert(material);
    }

    @Override
    public int update(SrCharacterStatBonusMaterial material) {
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
