package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.dto.GachaImportResult;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisResultVO;

/**
 * 跃迁记录Service接口
 * 
 * @author quico
 * @date 2026-08-19
 */
public interface ISrGachaRecordService 
{
    /**
     * 查询跃迁记录
     * 
     * @param gachaRecordId 跃迁记录主键
     * @return 跃迁记录
     */
    public SrGachaRecord selectSrGachaRecordByGachaRecordId(String gachaRecordId);

    /**
     * 查询跃迁记录列表
     * 
     * @param srGachaRecord 跃迁记录
     * @return 跃迁记录集合
     */
    public List<SrGachaRecord> selectSrGachaRecordList(SrGachaRecord srGachaRecord);

    /**
     * 新增跃迁记录
     * 
     * @param srGachaRecord 跃迁记录
     * @return 结果
     */
    public int insertSrGachaRecord(SrGachaRecord srGachaRecord);

    /**
     * 修改跃迁记录
     * 
     * @param srGachaRecord 跃迁记录
     * @return 结果
     */
    public int updateSrGachaRecord(SrGachaRecord srGachaRecord);

    /**
     * 批量删除跃迁记录
     * 
     * @param gachaRecordIds 需要删除的跃迁记录主键集合
     * @return 结果
     */
    public int deleteSrGachaRecordByGachaRecordIds(String[] gachaRecordIds);

    /**
     * 删除跃迁记录信息
     * 
     * @param gachaRecordId 跃迁记录主键
     * @return 结果
     */
    public int deleteSrGachaRecordByGachaRecordId(String gachaRecordId);

    /**
     * 导入跃迁记录（从Excel的rawData sheet解析）
     *
     * @param list 解析后的跃迁记录列表
     * @param updateSupport 是否更新已存在数据
     * @param operName 操作者
     * @return 结构化导入结果（含 needRecalc/uid/message）
     */
    public GachaImportResult importGachaRecord(List<SrGachaRecord> list, boolean updateSupport, String operName);

    /**
     * 抽卡分析：按卡池类型分组返回五星记录
     *
     * @param srGachaRecord 查询条件（uid/gachaType/gachaId/时间范围）
     * @return 按 gachaType 分组的五星记录
     */
    public GachaRecordAnalysisResultVO analysisGachaRecord(SrGachaRecord srGachaRecord);

    /**
     * 查询当前用户所有去重uid
     *
     * @param srGachaRecord 查询条件（createBy）
     * @return 去重uid列表
     */
    public List<String> selectDistinctUidList(String srGachaRecord);
}
