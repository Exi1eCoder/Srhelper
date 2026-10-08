package com.quico.srhelper.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.quico.srhelper.config.SrhelperCacheConstants;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.mapper.SrGachaRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 抽卡统计异步重算服务
 *
 * 计数器（total_pulls / pity_count）必须由抽卡时间决定（gacha_record_id 升序），
 * 与导入顺序无关。补录历史数据时先写入临时计数器保证可查询，再把 uid 放入 Redis 队列，
 * 由定时任务串行重算。
 *
 * Redis 结构：
 * - gacha:recalc:pending（Set）：待重算 uid
 * - gacha:recalc:running（ZSet）：重算中 uid，score=进入时间戳（卡死检测）
 * - gacha:recalc:done:{uid}（String）：完成标记，10 分钟过期
 * - gacha:recalc:lock:{uid}（String）：分布式锁
 * - gacha:recalc:retry:{uid}（String）：失败重试次数
 *
 * @author quico
 */
@Slf4j
@Service
public class GachaRecalcService
{
    /** 状态：重算完成 */
    public static final String STATUS_DONE = "DONE";
    /** 状态：重算中（排队中或执行中） */
    public static final String STATUS_PROCESSING = "PROCESSING";
    /** 状态：无重算任务 */
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    /** 每轮调度最多处理的 uid 数（凌晨集中处理，上限设大） */
    private static final int BATCH_PER_TICK = 500;

    /** running 中超过该时长（分钟）视为卡死 */
    private static final long STALE_MINUTES = 5;

    /** 锁超时（秒） */
    private static final long LOCK_SECONDS = 30;

    /** 最大重试次数 */
    private static final int MAX_RETRY = 3;

    /** BATCH 模式下每多少条 flush 一次 */
    private static final int FLUSH_SIZE = 500;

    /** 重试计数保留时间（分钟） */
    private static final long RETRY_TTL_MINUTES = 30;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private SrGachaRecordMapper srGachaRecordMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 提交重算任务（Set 天然去重，重复提交不会重复排队）
     */
    public void submit(String uid)
    {
        if (uid == null || uid.isEmpty())
        {
            return;
        }
        redisTemplate.opsForSet().add(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid);
        redisTemplate.delete(SrhelperCacheConstants.GACHA_RECALC_DONE_PREFIX + uid);
        log.info("抽卡统计重算已入队，uid={}", uid);
    }

    /**
     * 查询重算状态
     */
    public String status(String uid)
    {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(SrhelperCacheConstants.GACHA_RECALC_DONE_PREFIX + uid)))
        {
            return STATUS_DONE;
        }
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
                SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid)))
        {
            return STATUS_PROCESSING;
        }
        if (redisTemplate.opsForZSet().score(SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, uid) != null)
        {
            return STATUS_PROCESSING;
        }
        return STATUS_UNKNOWN;
    }

    /**
     * 批量查询重算状态
     * 前端传入自己的 uid 集合，一次拿到每个 uid 的状态，用于决定是否需要提示/启用重算按钮
     *
     * @param uids 游戏账号集合
     * @return uid -> 状态（DONE / PROCESSING / UNKNOWN），保持入参顺序
     */
    public Map<String, String> status(List<String> uids)
    {
        Map<String, String> result = new LinkedHashMap<>();
        if (uids == null || uids.isEmpty())
        {
            return result;
        }
        for (String uid : uids)
        {
            if (uid == null || uid.isEmpty())
            {
                continue;
            }
            result.put(uid, status(uid));
        }
        return result;
    }

    private static final DateTimeFormatter DATE_KEY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 手动触发重算（抽卡分析页"重新计算"按钮）
     * 只负责把 uid 插队到 pending，不立即执行（凌晨 4 点集中处理）
     * 每天限一次
     *
     * @param uid 游戏账号
     * @param createBy 当前登录平台用户ID（只能重算自己的数据）
     * @return null=已入队；非空=错误原因（鉴权失败/今日已提交）
     */
    public String triggerManual(String uid, String createBy)
    {
        if (uid == null || uid.isEmpty()
                || srGachaRecordMapper.countByUidAndCreateBy(uid, createBy) <= 0)
        {
            return "该游戏账号不属于当前用户，无法重算";
        }
        String dailyKey = SrhelperCacheConstants.GACHA_RECALC_MANUAL_PREFIX
                + uid + ":" + LocalDate.now().format(DATE_KEY_FMT);
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(dailyKey, "1", 1, TimeUnit.DAYS);
        if (!Boolean.TRUE.equals(absent))
        {
            return "今日已提交过重算，请明天再试";
        }
        redisTemplate.delete(SrhelperCacheConstants.GACHA_RECALC_DONE_PREFIX + uid);
        redisTemplate.opsForSet().add(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid);
        log.info("用户{}手动提交抽卡统计重算，uid={}，将在凌晨 4:00 执行", createBy, uid);
        return null;
    }

    /**
     * 定时消费待重算队列
     *
     * cron 由启动时的 active profile 决定：
     * - 含 prod：每天凌晨 4 点集中处理（避免白天全表重算占用数据库资源）
     * - 非 prod：每分钟一次，方便开发阶段快速验证
     *
     * 注意用 contains 而非 equals，兼容 prod,druid 这类复合 profile
     */
    @Scheduled(cron = "#{environment.getProperty('spring.profiles.active', 'dev').contains('prod') ? '0 0 4 * * ?' : '0 */5 * * * ?'}")
    // @Scheduled(cron = "0 0 4 * * ?")
    public void consumePending()
    {
        try
        {
            recoverStale();

            for (int i = 0; i < BATCH_PER_TICK; i++)
            {
                Object popped = redisTemplate.opsForSet().pop(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY);
                if (popped == null)
                {
                    break;
                }
                String uid = popped.toString();

                // 按 uid 加锁，同一账号的重算在多实例下也串行
                String lockKey = SrhelperCacheConstants.GACHA_RECALC_LOCK_PREFIX + uid;
                Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, uid, LOCK_SECONDS, TimeUnit.SECONDS);
                if (!Boolean.TRUE.equals(locked))
                {
                    // 其他实例正在处理，放回队列等待下一轮
                    redisTemplate.opsForSet().add(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid);
                    continue;
                }

                try
                {
                    redisTemplate.opsForZSet().add(SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, uid,
                            System.currentTimeMillis());
                    recalculate(uid);

                    // 成功：清理运行标记/重试计数，写完成标记
                    redisTemplate.opsForZSet().remove(SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, uid);
                    redisTemplate.delete(SrhelperCacheConstants.GACHA_RECALC_RETRY_PREFIX + uid);
                    redisTemplate.opsForValue().set(
                            SrhelperCacheConstants.GACHA_RECALC_DONE_PREFIX + uid, "1",
                            SrhelperCacheConstants.GACHA_RECALC_DONE_TTL_MINUTES, TimeUnit.MINUTES);
                    log.info("抽卡统计重算完成，uid={}", uid);
                }
                catch (Exception e)
                {
                    redisTemplate.opsForZSet().remove(SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, uid);
                    handleFailure(uid, e);
                }
                finally
                {
                    redisTemplate.delete(lockKey);
                }
            }
        }
        catch (Exception e)
        {
            log.error("抽卡统计重算调度异常", e);
        }
    }

    /**
     * 卡死恢复：running 中停留超过 5 分钟的 uid 移回 pending
     */
    private void recoverStale()
    {
        long cutoff = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(STALE_MINUTES);
        Set<Object> stale = redisTemplate.opsForZSet()
                .rangeByScore(SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, 0, cutoff);
        if (stale == null || stale.isEmpty())
        {
            return;
        }
        for (Object uid : stale)
        {
            Long removed = redisTemplate.opsForZSet().remove(
                    SrhelperCacheConstants.GACHA_RECALC_RUNNING_KEY, uid);
            if (removed != null && removed > 0)
            {
                redisTemplate.opsForSet().add(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid);
                log.warn("检测到卡死的重算任务，已移回待处理队列，uid={}", uid);
            }
        }
    }

    /**
     * 失败处理：未超过重试次数放回 pending，超过则记录日志人工介入
     */
    private void handleFailure(String uid, Exception e)
    {
        String retryKey = SrhelperCacheConstants.GACHA_RECALC_RETRY_PREFIX + uid;
        Long times = redisTemplate.opsForValue().increment(retryKey);
        if (times != null && times == 1L)
        {
            redisTemplate.expire(retryKey, RETRY_TTL_MINUTES, TimeUnit.MINUTES);
        }
        if (times != null && times >= MAX_RETRY)
        {
            redisTemplate.delete(retryKey);
            log.error("抽卡统计重算连续失败 {} 次，放弃自动重试，需人工介入，uid={}", MAX_RETRY, uid, e);
        }
        else
        {
            log.warn("抽卡统计重算失败（第 {} 次），将重试，uid={}", times, uid, e);
            redisTemplate.opsForSet().add(SrhelperCacheConstants.GACHA_RECALC_PENDING_KEY, uid);
        }
    }

    /**
     * 重算指定 uid 下全部平台用户的抽卡计数器
     * 每个 createBy 独立按 gachaType 分组，组内按 gacha_record_id 升序重算
     */
    private void recalculate(String uid)
    {
        List<String> createBys = srGachaRecordMapper.selectCreateByByUid(uid);
        if (createBys == null || createBys.isEmpty())
        {
            log.warn("抽卡统计重算找不到 uid 归属的用户，uid={}", uid);
            return;
        }

        try (SqlSession batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false))
        {
            SrGachaRecordMapper batchMapper = batchSession.getMapper(SrGachaRecordMapper.class);
            int pending = 0;

            for (String createBy : createBys)
            {
                List<SrGachaRecord> all = srGachaRecordMapper.selectByOwnerUidOrderByRecordId(createBy, uid);
                if (all == null || all.isEmpty())
                {
                    continue;
                }

                // 按卡池类型分组，保持首次出现顺序
                java.util.Map<String, List<SrGachaRecord>> groupMap = new java.util.LinkedHashMap<>();
                for (SrGachaRecord record : all)
                {
                    groupMap.computeIfAbsent(record.getGachaType(), k -> new ArrayList<>()).add(record);
                }

                for (List<SrGachaRecord> group : groupMap.values())
                {
                    int totalPulls = 0;
                    int pityCount = 0;
                    SrGachaRecord previous = null;

                    for (SrGachaRecord record : group)
                    {
                        totalPulls++;
                        // 上一条是五星则本条从 1 开始，否则 +1
                        pityCount = isFiveStar(previous) ? 1 : pityCount + 1;

                        // 仅当计数器确实变化时才更新
                        if (!Integer.valueOf(totalPulls).equals(record.getTotalPulls())
                                || !Integer.valueOf(pityCount).equals(record.getPityCount()))
                        {
                            batchMapper.updateCounters(record.getGachaRecordId(), totalPulls, pityCount);
                            pending++;
                            if (pending >= FLUSH_SIZE)
                            {
                                batchSession.flushStatements();
                                pending = 0;
                            }
                        }
                        previous = record;
                    }
                }
            }

            batchSession.flushStatements();
            batchSession.commit();
        }
    }

    private boolean isFiveStar(SrGachaRecord record)
    {
        return record != null && "5".equals(record.getRankType());
    }
}
