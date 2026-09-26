package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import org.apache.ibatis.annotations.Param;

public interface SrCharacterAscensionMaterialMapper
{
    public SrCharacterAscensionMaterial selectById(Long id);

    public List<SrCharacterAscensionMaterial> selectList(SrCharacterAscensionMaterial material);

    public List<SrCharacterAscensionMaterial> selectByCharacterId(Long characterId);

    public int insert(SrCharacterAscensionMaterial material);

    /**
     * 批量插入晋升材料
     * @param list 材料列表
     * @return 插入条数
     */
    public int insertBatch(@Param("list") List<SrCharacterAscensionMaterial> list);

    public int update(SrCharacterAscensionMaterial material);

    public int deleteById(Long id);

    public int deleteByIds(Long[] ids);

    public int deleteByCharacterId(Long characterId);
}
