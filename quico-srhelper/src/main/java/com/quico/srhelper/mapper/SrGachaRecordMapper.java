package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisResultVO;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * 跃迁记录Mapper接口
 * 
 * @author quico
 * @date 2026-08-19
 */
public interface SrGachaRecordMapper 
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
     * 删除跃迁记录
     * 
     * @param gachaRecordId 跃迁记录主键
     * @return 结果
     */
    public int deleteSrGachaRecordByGachaRecordId(String gachaRecordId);

    /**
     * 批量删除跃迁记录
     * 
     * @param gachaRecordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrGachaRecordByGachaRecordIds(String[] gachaRecordIds);

    /**
     * 批量插入跃迁记录
     * 
     * @param list 跃迁记录列表
     * @return 结果
     */
    public int insertBatch(List<SrGachaRecord> list);

    /**
     * 批量查询已存在的 gachaRecordId
     *
     * @param gachaRecordIds 待检查的主键集合
     * @return 数据库中已存在的 gachaRecordId 列表
     */
    public List<String> selectExistingGachaRecordIds(@Param("ids") List<String> gachaRecordIds);

    /**
     * 批量更新跃迁记录
     *
     * @param list 跃迁记录列表
     * @return 影响行数
     */
    public int updateBatch(List<SrGachaRecord> list);

    /**
     * 查询指定维度（createBy+uid+gachaType）的最大总抽数
     *
     * @param srGachaRecord 查询条件（含 createBy、uid、gachaType）
     * @return 该维度已有记录的最大总抽数，无记录返回 null
     */
    public Integer selectMaxTotalPulls(SrGachaRecord srGachaRecord);

    /**
     * 查询指定卡池类型最后一条记录（按时间倒序第一条）
     *
     * @param srGachaRecord 查询条件（含 createBy、uid、gachaType）
     * @return 该卡池类型最新的一条记录
     */
    public SrGachaRecord selectLastRecord(SrGachaRecord srGachaRecord);

    /**
     * 查询当前用户所有去重uid
     *
     * @param srGachaRecord 查询条件（createBy）
     * @return 去重uid列表
     */
    public List<String> selectDistinctUidList(@MonotonicNonNull String srGachaRecord);

    /**
     * 查询指定用户的抽卡记录首末时间
     */
    public GachaRecordAnalysisResultVO selectTimeRangeByUid(@Param("uid") String uid);
}
