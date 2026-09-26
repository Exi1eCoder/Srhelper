package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrLightconeMaterialBind;

/**
 * 光锥材料绑定Mapper接口
 */
public interface SrLightconeMaterialBindMapper {

    SrLightconeMaterialBind selectByLightConeIdAndItemId(Long lightConeId, Long itemId);

    List<SrLightconeMaterialBind> selectByLightConeId(Long lightConeId);

    int insert(SrLightconeMaterialBind bind);

    int update(SrLightconeMaterialBind bind);

    int deleteByLightConeId(Long lightConeId);

    /**
     * 查询光锥材料绑定
     *
     * @param id 光锥材料绑定主键
     * @return 光锥材料绑定
     */
    SrLightconeMaterialBind selectSrLightconeMaterialBindById(Long id);

    /**
     * 查询光锥材料绑定列表
     *
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 光锥材料绑定集合
     */
    List<SrLightconeMaterialBind> selectSrLightconeMaterialBindList(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 新增光锥材料绑定
     *
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    int insertSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 修改光锥材料绑定
     *
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    int updateSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 删除光锥材料绑定
     *
     * @param id 光锥材料绑定主键
     * @return 结果
     */
    int deleteSrLightconeMaterialBindById(Long id);

    /**
     * 批量删除光锥材料绑定
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSrLightconeMaterialBindByIds(Long[] ids);

    
    /**
     * 更新光锥名称（同步用）
     * 
     * @param lightconeId 光锥ID
     * @param lightconeName 光锥名称
     * @return 结果
     */
    public int updateLightconeName(Long lightconeId, String lightconeName);
}
