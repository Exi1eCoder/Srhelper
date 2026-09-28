package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.SrLightconeMaterialBindExcel;

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

    /**
     * 查询光锥材料绑定透视数据（一行一个光锥，世界掉落/拟造花萼各一列，取最低稀有度基础材料名）
     *
     * @param query 查询条件
     * @return 透视格式数据
     */
    public List<SrLightconeMaterialBindExcel> selectBindExcelList(SrLightconeMaterialBind query);

    /**
     * 导入光锥材料绑定透视数据（按光锥名称匹配光锥，按材料名称匹配材料）
     *
     * @param list          Excel 解析的透视数据
     * @param updateSupport 光锥已存在绑定时是否覆盖（false=跳过）
     * @param operName      操作人ID
     * @return 导入结果（含消息摘要与失败光锥名列表）
     */
    public BindImportResult importBindExcel(List<SrLightconeMaterialBindExcel> list, boolean updateSupport, String operName);
}
