package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrLightconeMaterialBind;

/**
 * 光锥材料绑定Service接口
 * 
 * @author quico
 * @date 2026-08-14
 */
public interface ISrLightconeMaterialBindService 
{
    /**
     * 查询光锥材料绑定
     * 
     * @param id 光锥材料绑定主键
     * @return 光锥材料绑定
     */
    public SrLightconeMaterialBind selectSrLightconeMaterialBindById(Long id);

    /**
     * 查询光锥材料绑定列表
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 光锥材料绑定集合
     */
    public List<SrLightconeMaterialBind> selectSrLightconeMaterialBindList(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 新增光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    public int insertSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 修改光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    public int updateSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind);

    /**
     * 批量删除光锥材料绑定
     * 
     * @param ids 需要删除的光锥材料绑定主键集合
     * @return 结果
     */
    public int deleteSrLightconeMaterialBindByIds(Long[] ids);

    /**
     * 删除光锥材料绑定信息
     * 
     * @param id 光锥材料绑定主键
     * @return 结果
     */
    public int deleteSrLightconeMaterialBindById(Long id);
}
