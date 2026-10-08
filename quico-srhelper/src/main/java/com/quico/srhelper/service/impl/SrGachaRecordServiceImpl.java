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
import com.quico.srhelper.domain.dto.GachaImportResult;
import com.quico.srhelper.mapper.SrGachaRecordMapper;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisItemVO;
import com.quico.srhelper.domain.vo.GachaRecordAnalysisVO;
import com.quico.srhelper.service.GachaRecalcService;
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

    @Autowired
    private GachaRecalcService gachaRecalcService;

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
     * 计数器（total_pulls / pity_count）由抽卡时间决定，不由导入顺序决定：
     * - 纯追加（本批最小 gacha_record_id 晚于该用户已有最大流水号）：
     *   同步接续最后一条记录的计数器，导入即正确
     * - 历史插入（补录更早的数据）：
     *   先以临时计数器写入保证用户可查询，再把 uid 放入 Redis 异步重算队列，
     *   返回 needRecalc=true
     *
     * @param list 解析后的跃迁记录列表
     * @param updateSupport 是否更新已存在数据
     * @param operName 操作者（平台用户ID）
     * @return 结构化导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GachaImportResult importGachaRecord(List<SrGachaRecord> list, boolean updateSupport, String operName)
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

        // 4. 计算新记录计数器，并收集发生历史插入的 uid
        Set<String> historicalUids = calculateCountersForImport(toInsert);

        // 5. 批量插入（计数器已计算）
        int successNum = 0;
        if (!toInsert.isEmpty())
        {
            srGachaRecordMapper.insertBatch(toInsert);
            successNum = toInsert.size();
        }

        // 6. 更新已存在记录（单条更新，参与当前事务保证原子性）
        int updateNum = 0;
        for (SrGachaRecord record : toUpdate)
        {
            srGachaRecordMapper.updateSrGachaRecord(record);
            updateNum++;
        }

        // 7. 历史插入的 uid 提交异步重算
        for (String uid : historicalUids)
        {
            gachaRecalcService.submit(uid);
        }

        // 8. 构建返回消息
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

        boolean needRecalc = !historicalUids.isEmpty();
        String recalcUid = historicalUids.stream().findFirst().orElse(null);
        if (needRecalc)
        {
            resultMsg.append("；检测到补录历史数据，统计信息稍后自动修正。请耐心等待");
        }

        return new GachaImportResult(needRecalc, recalcUid, resultMsg.toString());
    }

    /**
     * 为待插入记录计算计数器
     *
     * 按 createBy+uid 判断追加/历史插入，按 createBy+uid+gachaType 分组计算：
     * - 纯追加：取该卡池已有最后一条记录接续，计数器导入即正确
     * - 历史插入：同样接续算出临时值保证可查询，uid 收集后异步整体重算
     *
     * @return 发生历史插入的 uid 集合（需异步重算）
     */
    private Set<String> calculateCountersForImport(List<SrGachaRecord> toInsert)
    {
        Set<String> historicalUids = new LinkedHashSet<>();
        if (toInsert.isEmpty())
        {
            return historicalUids;
        }

        // 按 createBy+uid+gachaType 分组（保持顺序用 LinkedHashMap）
        Map<String, List<SrGachaRecord>> groupMap = new LinkedHashMap<>();
        for (SrGachaRecord record : toInsert)
        {
            groupMap.computeIfAbsent(buildGroupKey(record), k -> new ArrayList<>()).add(record);
        }

        // 缓存每个 createBy+uid 的已有最大流水号与历史插入判定
        Map<String, String> maxExistingIdCache = new HashMap<>();

        for (List<SrGachaRecord> group : groupMap.values())
        {
            // 组内按 gacha_record_id 升序（即抽卡时间升序）
            group.sort(Comparator.comparing(SrGachaRecord::getGachaRecordId));

            SrGachaRecord first = group.get(0);
            String ownerKey = first.getCreateBy() + "|" + first.getUid();

            String maxExistingId = maxExistingIdCache.computeIfAbsent(ownerKey,
                    k -> srGachaRecordMapper.selectMaxRecordIdByOwner(first.getCreateBy(), first.getUid()));

            // 历史插入：已有记录中存在比本批最小流水号更大的记录
            boolean historical = maxExistingId != null
                    && first.getGachaRecordId().compareTo(maxExistingId) < 0;
            if (historical)
            {
                historicalUids.add(first.getUid());
            }

            // 追加/历史插入均先接续该卡池最后一条记录算出计数器；
            // 历史插入的临时值随后由异步重算任务按 gacha_record_id 全量纠正
            SrGachaRecord lastRecord = srGachaRecordMapper.selectLastByGroup(
                    first.getCreateBy(), first.getUid(), first.getGachaType());
            int totalPulls = (lastRecord != null && lastRecord.getTotalPulls() != null)
                    ? lastRecord.getTotalPulls() : 0;
            int pityCount = (lastRecord != null && lastRecord.getPityCount() != null)
                    ? lastRecord.getPityCount() : 0;

            for (SrGachaRecord record : group)
            {
                totalPulls++;
                // 上一条是五星则本条从 1 开始，否则 +1
                pityCount = isFiveStar(lastRecord) ? 1 : pityCount + 1;
                record.setTotalPulls(totalPulls);
                record.setPityCount(pityCount);
                lastRecord = record;
            }
        }

        return historicalUids;
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
