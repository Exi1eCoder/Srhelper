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
     * 单条更新计数器（total_pulls / pity_count）
     * 通过 SqlSessionFactory 的 ExecutorType.BATCH 批量提交，不依赖 allowMultiQueries
     *
     * @param gachaRecordId 抽卡流水号
     * @param totalPulls 总抽数
     * @param pityCount 保底内抽数
     * @return 影响行数
     */
    public int updateCounters(@Param("gachaRecordId") String gachaRecordId,
                              @Param("totalPulls") int totalPulls,
                              @Param("pityCount") int pityCount);

    /**
     * 查询指定用户（createBy+uid）已有的最大 gacha_record_id
     * 用于判断本次导入是纯追加还是历史插入
     *
     * @param createBy 平台用户ID
     * @param uid 游戏账号
     * @return 已有最大流水号，无记录返回 null
     */
    public String selectMaxRecordIdByOwner(@Param("createBy") String createBy,
                                           @Param("uid") String uid);

    /**
     * 查询指定卡池（createBy+uid+gachaType）按流水号升序的最后一条记录
     * 纯追加时用于接续 total_pulls / pity_count
     *
     * @param createBy 平台用户ID
     * @param uid 游戏账号
     * @param gachaType 卡池类型
     * @return 最后一条记录，无则返回 null
     */
    public SrGachaRecord selectLastByGroup(@Param("createBy") String createBy,
                                           @Param("uid") String uid,
                                           @Param("gachaType") String gachaType);

    /**
     * 查询指定用户（createBy+uid）全部记录，按 gacha_record_id 升序（重算用）
     *
     * @param createBy 平台用户ID
     * @param uid 游戏账号
     * @return 全部记录（升序）
     */
    public List<SrGachaRecord> selectByOwnerUidOrderByRecordId(@Param("createBy") String createBy,
                                                               @Param("uid") String uid);

    /**
     * 根据游戏 uid 反查归属的平台用户 create_by（重算任务定位归属）
     *
     * @param uid 游戏账号
     * @return create_by 列表
     */
    public List<String> selectCreateByByUid(@Param("uid") String uid);

    /**
     * 校验游戏 uid 是否归属于指定平台用户（手动重算权限校验）
     *
     * @param uid 游戏账号
     * @param createBy 平台用户ID
     * @return 记录数
     */
    public int countByUidAndCreateBy(@Param("uid") String uid,
                                     @Param("createBy") String createBy);

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
