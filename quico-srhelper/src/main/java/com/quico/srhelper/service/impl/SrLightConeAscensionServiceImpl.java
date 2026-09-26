package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrLightConeAscensionMapper;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.dto.LightConeAscensionBatchSaveDTO;
import com.quico.srhelper.service.ISrLightConeAscensionService;

/**
 * 光锥晋升材料Service业务层处理
 *
 * @author quico
 * @date 2026-05-30
 */
@Service
public class SrLightConeAscensionServiceImpl implements ISrLightConeAscensionService
{
    @Autowired
    private SrLightConeAscensionMapper srLightConeAscensionMapper;

    @Override
    public SrLightConeAscension selectSrLightConeAscensionById(Long id)
    {
        return srLightConeAscensionMapper.selectSrLightConeAscensionById(id);
    }

    @Override
    public List<SrLightConeAscension> selectSrLightConeAscensionList(SrLightConeAscension srLightConeAscension)
    {
        return srLightConeAscensionMapper.selectSrLightConeAscensionList(srLightConeAscension);
    }

    @Override
    public List<SrLightConeAscension> selectSrLightConeAscensionByConeId(Long lightConeId)
    {
        return srLightConeAscensionMapper.selectSrLightConeAscensionByConeId(lightConeId);
    }

    @Override
    public int insertSrLightConeAscension(SrLightConeAscension srLightConeAscension)
    {
        srLightConeAscension.setCreateTime(DateUtils.getNowDate());
        return srLightConeAscensionMapper.insertSrLightConeAscension(srLightConeAscension);
    }

    @Override
    public int updateSrLightConeAscension(SrLightConeAscension srLightConeAscension)
    {
        srLightConeAscension.setUpdateTime(DateUtils.getNowDate());
        return srLightConeAscensionMapper.updateSrLightConeAscension(srLightConeAscension);
    }

    @Override
    public int deleteSrLightConeAscensionByIds(Long[] ids)
    {
        return srLightConeAscensionMapper.deleteSrLightConeAscensionByIds(ids);
    }

    @Override
    public int deleteSrLightConeAscensionById(Long id)
    {
        return srLightConeAscensionMapper.deleteSrLightConeAscensionById(id);
    }

    @Override
    public int deleteSrLightConeAscensionByConeId(Long lightConeId)
    {
        return srLightConeAscensionMapper.deleteSrLightConeAscensionByConeId(lightConeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchSaveLightConeAscension(LightConeAscensionBatchSaveDTO dto)
    {
        srLightConeAscensionMapper.deleteSrLightConeAscensionByConeId(dto.getLightConeId());

        List<Long> insertedIds = new java.util.ArrayList<>();
        for (LightConeAscensionBatchSaveDTO.AscensionGroup group : dto.getAscensions())
        {
            int slot = 1;
            for (LightConeAscensionBatchSaveDTO.MaterialItem material : group.getMaterials())
            {
                SrLightConeAscension item = SrLightConeAscension.builder()
                        .lightConeId(dto.getLightConeId())
                        .ascensionPhase(group.getAscensionPhase())
                        .breakLevel(group.getBreakLevel())
                        .materialSlot(slot)
                        .itemId(material.getItemId())
                        .rarityLevel(material.getRarityLevel())
                        .quantity(material.getQuantity())
                        .sortOrder(slot)
                        .build();
                item.setCreateTime(DateUtils.getNowDate());
                srLightConeAscensionMapper.insertSrLightConeAscension(item);
                insertedIds.add(item.getId());
                slot++;
            }
        }
        return insertedIds;
    }
}
