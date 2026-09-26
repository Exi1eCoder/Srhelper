package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.service.ISrCharacterAscensionMaterialService;

@Service
public class SrCharacterAscensionMaterialServiceImpl implements ISrCharacterAscensionMaterialService
{
    @Autowired
    private SrCharacterAscensionMaterialMapper mapper;

    @Override
    public SrCharacterAscensionMaterial selectById(Long id) { return mapper.selectById(id); }

    @Override
    public List<SrCharacterAscensionMaterial> selectList(SrCharacterAscensionMaterial material) { return mapper.selectList(material); }

    @Override
    public List<SrCharacterAscensionMaterial> selectByCharacterId(Long characterId) { return mapper.selectByCharacterId(characterId); }

    @Override
    public int insert(SrCharacterAscensionMaterial material) {
        material.setCreateTime(DateUtils.getNowDate());
        return mapper.insert(material);
    }

    @Override
    public int update(SrCharacterAscensionMaterial material) {
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
