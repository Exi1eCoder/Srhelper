package com.quico.srhelper.service.impl;

import java.util.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisResultVO;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.config.GachaRecordImageCacheManager;
import com.quico.srhelper.mapper.SrGachaRecordMapper;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisItemVO;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisVO;
import com.quico.srhelper.service.ISrGachaRecordService;

/**
 * 跃迁记录Service业务层处理
 * 
 * @author quico
 * @date 2026-08-19
 */
@Service
@Slf4j
public class SrGachaRecordServiceImpl implements ISrGachaRecordService
{
    @Autowired
    private SrGachaRecordMapper srGachaRecordMapper;

    @Autowired
    private GachaRecordImageCacheManager imageCacheManager;

    /**
     * 查询跃迁记录
     * 
     * @param gachaRecordId 跃迁记录主键
     * @return 跃迁记录
     */
    @Override
    public SrGachaRecord selectSrGachaRecordByGachaRecordId(String gachaRecordId)
    {
        SrGachaRecord record = srGachaRecordMapper.selectSrGachaRecordByGachaRecordId(gachaRecordId);
        if (record != null) {
            fillImageUrl(record);
        }
        return record;
    }

    /**
     * 查询跃迁记录列表
     * 
     * @param srGachaRecord 跃迁记录
     * @return 跃迁记录
     */
    @Override
    public List<SrGachaRecord> selectSrGachaRecordList(SrGachaRecord srGachaRecord)
    {
        List<SrGachaRecord> list = srGachaRecordMapper.selectSrGachaRecordList(srGachaRecord);
        fillImageUrls(list);
        return list;
    }

    /**
     * 新增跃迁记录
     * 
     * @param srGachaRecord 跃迁记录
     * @return 结果
     */
    @Override
    public int insertSrGachaRecord(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setCreateTime(DateUtils.getNowDate());
        return srGachaRecordMapper.insertSrGachaRecord(srGachaRecord);
    }

    /**
     * 修改跃迁记录
     * 
     * @param srGachaRecord 跃迁记录
     * @return 结果
     */
    @Override
    public int updateSrGachaRecord(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setUpdateTime(DateUtils.getNowDate());
        return srGachaRecordMapper.updateSrGachaRecord(srGachaRecord);
    }

    /**
     * 批量删除跃迁记录
     * 
     * @param gachaRecordIds 需要删除的跃迁记录主键
     * @return 结果
     */
    @Override
    public int deleteSrGachaRecordByGachaRecordIds(String[] gachaRecordIds)
    {
        return srGachaRecordMapper.deleteSrGachaRecordByGachaRecordIds(gachaRecordIds);
    }

    /**
     * 删除跃迁记录信息
     * 
     * @param gachaRecordId 跃迁记录主键
     * @return 结果
     */
    @Override
    public int deleteSrGachaRecordByGachaRecordId(String gachaRecordId)
    {
        return srGachaRecordMapper.deleteSrGachaRecordByGachaRecordId(gachaRecordId);
    }

    /**
     * 导入跃迁记录（从Excel的rawData sheet解析）
     * 
     * @param list 解析后的跃迁记录列表
     * @param updateSupport 是否更新已存在数据
     * @param operName 操作者
     * @return 导入结果消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importGachaRecord(List<SrGachaRecord> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        // 1. 校验必填字段，分离有效/无效记录
        List<SrGachaRecord> validRecords = new ArrayList<>();
        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();

        for (SrGachaRecord record : list)
        {
            if (StringUtils.isEmpty(record.getGachaRecordId()))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、抽卡记录流水号不能为空");
                continue;
            }
            validRecords.add(record);
        }

        // 2. 批量查询已存在的记录（1 次 DB 查询替代 N 次）
        List<String> allIds = validRecords.stream()
                .map(SrGachaRecord::getGachaRecordId)
                .collect(Collectors.toList());
        Set<String> existingIds = new HashSet<>(srGachaRecordMapper.selectExistingGachaRecordIds(allIds));

        // 3. 分类：待插入 / 待更新 / 跳过（重复不计为失败）
        List<SrGachaRecord> toInsert = new ArrayList<>();
        List<SrGachaRecord> toUpdate = new ArrayList<>();
        int skipNum = 0;
        Date now = DateUtils.getNowDate();

        for (SrGachaRecord record : validRecords)
        {
            if (existingIds.contains(record.getGachaRecordId()))
            {
                if (updateSupport)
                {
                    record.setUpdateBy(operName);
                    record.setUpdateTime(now);
                    toUpdate.add(record);
                }
                else
                {
                    skipNum++;
                }
            }
            else
            {
                record.setCreateBy(operName);
                record.setCreateTime(now);
                toInsert.add(record);
            }
        }

        // 4. 批量插入（含保底计算）
        int successNum = 0;
        if (!toInsert.isEmpty())
        {
            calculatePity(toInsert);
            srGachaRecordMapper.insertBatch(toInsert);
            successNum = toInsert.size();
        }

        // 5. 批量更新
        int updateNum = 0;
        if (!toUpdate.isEmpty())
        {
            srGachaRecordMapper.updateBatch(toUpdate);
            updateNum = toUpdate.size();
        }

        // 6. 构建返回消息（只有真正的校验错误才抛异常）
        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("导入完成：新增 ").append(successNum).append(" 条");
        if (updateNum > 0)
        {
            resultMsg.append("，更新 ").append(updateNum).append(" 条");
        }
        if (skipNum > 0)
        {
            resultMsg.append("，跳过重复 ").append(skipNum).append(" 条");
        }

        if (failureNum > 0)
        {
            failureMsg.insert(0, "，" + failureNum + " 条数据格式不正确：");
            resultMsg.append(failureMsg);
        }

        return resultMsg.toString();
    }

    /**
     * 计算总抽数和保底内抽数
     * 按 createBy + uid + gachaId 分组，组内按抽卡时间排序：
     * - 总抽数：从数据库该维度已有的最大总抽数继续累加
     * - 保底内抽数：遇到五星(rankType=5)后，下一条记录从 1 开始；否则在上一条基础上 +1
     */
    private void calculatePity(List<SrGachaRecord> toInsert)
    {
        // 按 createBy + uid + gachaId 分组（保持顺序用 LinkedHashMap）
        Map<String, List<SrGachaRecord>> groupMap = new LinkedHashMap<>();
        for (SrGachaRecord record : toInsert)
        {
            String key = buildGroupKey(record);
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }

        // 每组内计算
        for (List<SrGachaRecord> group : groupMap.values())
        {
            // 组内按抽卡时间升序排序（同时间按流水号排序，保证稳定）
            group.sort(Comparator
                    .comparing(SrGachaRecord::getTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(SrGachaRecord::getGachaRecordId));

            SrGachaRecord first = group.get(0);
            SrGachaRecord query = new SrGachaRecord();
            query.setCreateBy(first.getCreateBy());
            query.setUid(first.getUid());
            query.setGachaType(first.getGachaType());

            // 数据库该维度已有的最大总抽数
            Integer maxTotalPulls = srGachaRecordMapper.selectMaxTotalPulls(query);
            int totalPulls = (maxTotalPulls != null) ? maxTotalPulls : 0;

            // 数据库该卡池最后一条记录的保底抽数（用于接续）
            SrGachaRecord lastRecord = srGachaRecordMapper.selectLastRecord(query);
            int pityCount = (lastRecord != null && lastRecord.getPityCount() != null)
                    ? lastRecord.getPityCount() : 0;

            for (SrGachaRecord record : group)
            {
                totalPulls++;

                // 保底计算：如果上一条是五星，则本条从 1 开始；否则 +1
                if (isFiveStar(lastRecord))
                {
                    pityCount = 1;
                }
                else
                {
                    pityCount++;
                }

                record.setTotalPulls(totalPulls);
                record.setPityCount(pityCount);

                // 当前记录作为下一条的"上一条"
                lastRecord = record;
            }
        }
    }

    /**
     * 构建分组 key：createBy + uid + gachaType
     */
    private String buildGroupKey(SrGachaRecord record)
    {
        return record.getCreateBy() + "|" + record.getUid() + "|" + record.getGachaType();
    }

    /**
     * 判断是否为五星
     */
    private boolean isFiveStar(SrGachaRecord record)
    {
        return record != null && "5".equals(record.getRankType());
    }

    /**
     * 抽卡分析：按卡池类型分组返回五星记录
     *
     * @param srGachaRecord 查询条件（uid/gachaType/gachaId/时间范围）
     * @return 按 gachaType 分组的五星记录
     */
    @Override
    public GachaRecordAnalysisResultVO analysisGachaRecord(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setRankType("5");
        List<SrGachaRecord> list = srGachaRecordMapper.selectSrGachaRecordList(srGachaRecord);

        log.debug("uid: {}", srGachaRecord.getUid());
        GachaRecordAnalysisResultVO resultVO = Optional
                .ofNullable(srGachaRecordMapper.selectTimeRangeByUid(srGachaRecord.getUid()))
                .orElseGet(GachaRecordAnalysisResultVO::new);
        log.debug("first pull time: {}", resultVO.getFirstPullTime());
        log.debug("last pull time: {}", resultVO.getLastPullTime());

        Map<String, List<SrGachaRecord>> groupMap = new LinkedHashMap<>();
        for (SrGachaRecord record : list)
        {
            String gachaType = record.getGachaType();
            groupMap.computeIfAbsent(gachaType, k -> new ArrayList<>()).add(record);
        }

        List<GachaRecordAnalysisVO> result = new ArrayList<>();
        for (Map.Entry<String, List<SrGachaRecord>> entry : groupMap.entrySet())
        {
            List<SrGachaRecord> group = entry.getValue();
            group.sort(Comparator.comparing(SrGachaRecord::getTime, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(SrGachaRecord::getGachaRecordId));

            GachaRecordAnalysisVO vo = new GachaRecordAnalysisVO();
            vo.setGachaType(entry.getKey());

            List<GachaRecordAnalysisItemVO> items = new ArrayList<>();
            for (SrGachaRecord record : group)
            {
                GachaRecordAnalysisItemVO item = new GachaRecordAnalysisItemVO();
                item.setName(record.getName());
                item.setItemType(record.getItemType());
                item.setPityCount(record.getPityCount());
                item.setTime(record.getTime());
                item.setImageUrl(imageCacheManager.getImageUrl(record.getItemType(), record.getItemId()));
                items.add(item);
            }
            vo.setItems(items);
            result.add(vo);
        }

        resultVO.setGroups(result);
        return resultVO;
    }

    @Override
    public List<String> selectDistinctUidList(@MonotonicNonNull String srGachaRecord)
    {
        List<String> uidList = srGachaRecordMapper.selectDistinctUidList(srGachaRecord);
        return uidList;
    }

    private void fillImageUrls(List<SrGachaRecord> list) {
        if (list == null) {
            return;
        }
        for (SrGachaRecord record : list) {
            fillImageUrl(record);
        }
    }

    private void fillImageUrl(SrGachaRecord record) {
        record.setImageUrl(imageCacheManager.getImageUrl(record.getItemType(), record.getItemId()));
    }
}
