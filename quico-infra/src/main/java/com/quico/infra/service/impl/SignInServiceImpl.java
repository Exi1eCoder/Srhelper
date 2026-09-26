package com.quico.infra.service.impl;

import com.quico.common.core.domain.AjaxResult;
import com.quico.infra.domain.QuicoUserSignIn;
import com.quico.infra.mapper.QuicoUserSignInMapper;
import com.quico.infra.service.ISignInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 签到核心服务实现
 * Redis Bitmap 存储签到状态，MySQL 存储签到记录
 *
 * Redis Key: sign:{userId}:{yyyyMM}
 * Bitmap offset = 当月第几天 - 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignInServiceImpl implements ISignInService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final QuicoUserSignInMapper signInMapper;

    private static final String SIGN_KEY_PREFIX = "sign:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult signIn(Long userId) {
        LocalDate today = LocalDate.now();

        if (isSignedToday(userId, today)) {
            return AjaxResult.error("今日已签到");
        }

        // Redis SETBIT
        setSignIn(userId, today);

        // MySQL 记录
        QuicoUserSignIn record = QuicoUserSignIn.builder()
                .userId(userId)
                .signDate(java.sql.Date.valueOf(today))
                .signTime(new java.util.Date())
                .isRepaired(0)
                .build();
        signInMapper.insertQuicoUserSignIn(record);

        int continuousDays = calcContinuousDays(userId, today);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("message", "签到成功");
        data.put("points", 5);
        data.put("continuousDays", continuousDays);

        log.info("用户[{}]签到成功，连续{}天", userId, continuousDays);
        return AjaxResult.success(data);
    }

    @Override
    public AjaxResult getSignInStatus(Long userId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        int y = (year != null) ? year : today.getYear();
        int m = (month != null) ? month : today.getMonthValue();
        LocalDate queryDate = LocalDate.of(y, m, 1);
        boolean isCurrentMonth = (y == today.getYear() && m == today.getMonthValue());

        String monthStr = String.format("%d-%02d", y, m);
        boolean todaySigned = isCurrentMonth && isSignedToday(userId, today);
        int continuousDays = isCurrentMonth ? calcContinuousDays(userId, today) : 0;
        int todayPoints = (isCurrentMonth && todaySigned) ? 5 : 0;
        List<String> signedDates = getSignedDatesOfMonth(userId, queryDate);

        // 今日签到时间（从 MySQL 查）
        String todaySignTime = null;
        if (isCurrentMonth && todaySigned) {
            QuicoUserSignIn query = new QuicoUserSignIn();
            query.setUserId(userId);
            query.setSignDate(java.sql.Date.valueOf(today));
            List<QuicoUserSignIn> records = signInMapper.selectQuicoUserSignInList(query);
            if (records != null && !records.isEmpty() && records.get(0).getSignTime() != null) {
                todaySignTime = records.get(0).getSignTime().toInstant().toString();
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("month", monthStr);
        data.put("todaySigned", todaySigned);
        data.put("continuousDays", continuousDays);
        data.put("todayPoints", todayPoints);
        data.put("todaySignTime", todaySignTime);
        data.put("signedDates", signedDates);

        return AjaxResult.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult repairSignIn(Long userId, String date) {
        LocalDate repairDate = LocalDate.parse(date);

        if (!repairDate.isBefore(LocalDate.now())) {
            return AjaxResult.error("只能补签过去的日期");
        }

        if (isSigned(userId, repairDate)) {
            return AjaxResult.error("该日期已签到");
        }

        setSignIn(userId, repairDate);

        QuicoUserSignIn record = QuicoUserSignIn.builder()
                .userId(userId)
                .signDate(java.sql.Date.valueOf(repairDate))
                .signTime(new java.util.Date())
                .isRepaired(1)
                .build();
        signInMapper.insertQuicoUserSignIn(record);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("message", "补签成功");

        return AjaxResult.success(data);
    }

    // ======================== Redis Bitmap ========================

    private String buildSignKey(Long userId, LocalDate date) {
        return SIGN_KEY_PREFIX + userId + ":" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private void setSignIn(Long userId, LocalDate date) {
        String key = buildSignKey(userId, date);
        int offset = date.getDayOfMonth() - 1;
        redisTemplate.opsForValue().setBit(key, offset, true);
    }

    private boolean isSigned(Long userId, LocalDate date) {
        String key = buildSignKey(userId, date);
        int offset = date.getDayOfMonth() - 1;
        Boolean bit = redisTemplate.opsForValue().getBit(key, offset);
        return Boolean.TRUE.equals(bit);
    }

    private boolean isSignedToday(Long userId, LocalDate today) {
        return isSigned(userId, today);
    }

    private List<String> getSignedDatesOfMonth(Long userId, LocalDate today) {
        List<String> dates = new ArrayList<>();
        String key = buildSignKey(userId, today);
        int daysInMonth = today.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            int offset = day - 1;
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, offset))) {
                dates.add(today.withDayOfMonth(day).toString());
            }
        }
        return dates;
    }

    private int calcContinuousDays(Long userId, LocalDate today) {
        int count = 0;
        for (int i = 0; i < 365; i++) {
            LocalDate d = today.minusDays(i);
            if (isSigned(userId, d)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
