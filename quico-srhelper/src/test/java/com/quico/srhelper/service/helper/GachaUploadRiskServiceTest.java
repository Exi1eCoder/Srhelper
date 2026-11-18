package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.multipart.MultipartFile;

import com.quico.common.constant.HttpStatus;
import com.quico.common.exception.ServiceException;
import com.quico.srhelper.constants.SrhelperCacheConstants;
import com.quico.srhelper.domain.SysUploadViolationLog;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;
import com.quico.srhelper.mapper.SysUploadViolationLogMapper;

/**
 * 抽卡记录上传风控服务（第五层）单元测试
 *
 * 锁定：
 * - 死因写入审计日志，重度违规 is_malicious = 1
 * - 用户失败 1~3 次 → 40010；第 4 次 → 40013；第 5 次起 → 封禁并伪装 500
 * - IP 失败 20 次 → 封禁 IP
 * - 导入成功 → 只清除用户失败计数，IP 计数自然过期
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("抽卡上传风控与审计")
class GachaUploadRiskServiceTest {

    private static final long DEFAULT_TTL_MINUTES = 24 * 60;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private ValueOperations<Object, Object> valueOperations;

    @Mock
    private SysUploadViolationLogMapper uploadViolationLogMapper;

    @InjectMocks
    private GachaUploadRiskService service;

    @Test
    @DisplayName("重度违规落库 is_malicious=1，并按用户失败次数返回 40010")
    void shouldSaveMaliciousViolationAndReturnFormatError() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("gacha.xlsx");
        when(file.getSize()).thenReturn(1024L);
        stubCounts(1, 1);

        UploadValidateException violation = new UploadValidateException(
                ViolationTypeEnum.DATA_TAMPERED_FUTURE_TIME, "第 1 条记录抽卡时间晚于系统当前时间");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleViolationAsException(violation, 1L, "1.1.1.1", file));

        assertEquals(GachaUploadRiskService.CODE_DATA_FORMAT_ERROR, ex.getCode().intValue());

        SysUploadViolationLog log = captureViolationLog();
        assertEquals("DATA_TAMPERED_FUTURE_TIME", log.getViolationType());
        assertEquals(1, log.getIsMalicious().intValue());
        assertEquals(1L, log.getUserId().longValue());
        assertEquals("1.1.1.1", log.getIpAddress());
        assertEquals("gacha.xlsx", log.getFileName());
        assertEquals(1024L, log.getFileSize().longValue());
    }

    @Test
    @DisplayName("用户第 4 次失败 → 40013 触发须知弹窗")
    void shouldReturnNoticeOnFourthFailure() {
        stubCounts(4, 4);
        UploadValidateException violation = new UploadValidateException(
                ViolationTypeEnum.FORMAT_ERROR, "文件格式错误");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleViolationAsException(violation, 1L, "1.1.1.1", null));

        assertEquals(GachaUploadRiskService.CODE_USER_NOTICE, ex.getCode().intValue());
        assertEquals(GachaUploadRiskService.MSG_USER_NOTICE, ex.getMessage());
    }

    @Test
    @DisplayName("用户第 5 次失败 → 封禁用户并伪装网络超时")
    void shouldBanUserOnFifthFailure() {
        stubCounts(5, 5);
        UploadValidateException violation = new UploadValidateException(
                ViolationTypeEnum.FORMAT_ERROR, "文件格式错误");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleViolationAsException(violation, 1L, "1.1.1.1", null));

        assertEquals(HttpStatus.ERROR, ex.getCode().intValue());
        assertEquals(GachaUploadRiskService.MSG_NETWORK_TIMEOUT, ex.getMessage());
        verify(valueOperations).set(eq(SrhelperCacheConstants.GACHA_UPLOAD_BAN_USER_PREFIX + 1L),
                eq("1"), eq(DEFAULT_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("IP 第 20 次失败 → 封禁 IP")
    void shouldBanIpOnTwentiethFailure() {
        stubCounts(1, 20);
        UploadValidateException violation = new UploadValidateException(
                ViolationTypeEnum.FORMAT_ERROR, "文件格式错误");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleViolationAsException(violation, 1L, "1.1.1.1", null));

        assertEquals(GachaUploadRiskService.CODE_DATA_FORMAT_ERROR, ex.getCode().intValue());
        verify(valueOperations).set(eq(SrhelperCacheConstants.GACHA_UPLOAD_BAN_IP_PREFIX + "1.1.1.1"),
                eq("1"), eq(DEFAULT_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("导入成功 → 只清除用户失败计数，不清 IP 计数")
    void shouldClearOnlyUserFailCount() {
        service.clearFailCount("1");

        verify(redisTemplate).delete(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_USER_PREFIX + "1");
        verify(redisTemplate, never()).delete(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_IP_PREFIX + "1.1.1.1");
    }

    @Test
    @DisplayName("IP 封禁标记存在 → isIpBanned 返回 true")
    void shouldDetectBannedIp() {
        when(redisTemplate.hasKey(SrhelperCacheConstants.GACHA_UPLOAD_BAN_IP_PREFIX + "1.1.1.1"))
                .thenReturn(true);

        assertTrue(service.isIpBanned("1.1.1.1"));
    }

    @Test
    @DisplayName("用户封禁标记不存在 → isUserBanned 返回 false")
    void shouldDetectNotBannedUser() {
        when(redisTemplate.hasKey(SrhelperCacheConstants.GACHA_UPLOAD_BAN_USER_PREFIX + "1"))
                .thenReturn(false);

        assertFalse(service.isUserBanned("1"));
    }

    @Test
    @DisplayName("错误信息超过 500 字符 → 审计日志截断")
    void shouldTruncateLongErrorMessage() {
        stubCounts(1, 1);
        UploadValidateException violation = new UploadValidateException(
                ViolationTypeEnum.FORMAT_ERROR, "x".repeat(600));

        assertThrows(ServiceException.class,
                () -> service.handleViolationAsException(violation, 1L, "1.1.1.1", null));

        assertEquals(500, captureViolationLog().getErrorMsg().length());
    }

    /**
     * 按 key 前缀区分用户/IP 返回各自失败次数
     */
    private void stubCounts(long userFails, long ipFails) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any())).thenAnswer(invocation -> {
            Object key = invocation.getArgument(0);
            return key.toString().contains(":user:") ? userFails : ipFails;
        });
    }

    private SysUploadViolationLog captureViolationLog() {
        ArgumentCaptor<SysUploadViolationLog> captor = ArgumentCaptor.forClass(SysUploadViolationLog.class);
        verify(uploadViolationLogMapper).insertSysUploadViolationLog(captor.capture());
        return captor.getValue();
    }
}
