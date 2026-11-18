package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.multipart.MultipartFile;

import com.quico.common.core.domain.entity.SysDictData;
import com.quico.common.exception.ServiceException;
import com.quico.common.exception.BusinessException;
import com.quico.common.utils.DictUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;

/**
 * 抽卡记录导入校验器（第一 ~ 第四层）单元测试
 *
 * 第一层：文件后缀 / 大小 / xlsx 魔数
 * 第二层：CSV 解析 / 空文件 / 行数上限
 * 前置：卡池类型字典是否配置（系统问题，尽早拒绝）
 * 第三层：卡池类型字典内容白名单（在第四层之后执行）
 * 第四层：抽卡时间不得晚于系统当前时间，以及 uid/流水号尾号/内嵌小时数防伪
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("抽卡记录导入校验器")
class GachaImportValidatorTest {

    /** 合法的 uid 尾号（与流水号尾号一致） */
    private static final String UID = "12345612";

    /** 流水号尾号 */
    private static final String TAIL = "12";

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private ValueOperations<Object, Object> valueOperations;

    @InjectMocks
    private GachaImportValidator validator;

    // ---------------------------------------------------------------- 第一层

    @Nested
    @DisplayName("第一层：物理与前置拦截")
    class PhysicalLayer {

        @Test
        @DisplayName("文件为空 → FORMAT_ERROR")
        void shouldRejectNullFile() {
            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(null));
        }

        @Test
        @DisplayName("原始文件名为空 → FORMAT_ERROR")
        void shouldRejectMissingFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn(null);

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(file));
        }

        @Test
        @DisplayName("后缀不在白名单 → FORMAT_ERROR")
        void shouldRejectUnsupportedSuffix() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("records.txt");

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(file));
        }

        @Test
        @DisplayName("缺少扩展名 → FORMAT_ERROR")
        void shouldRejectMissingExtension() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("records");

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(file));
        }

        @Test
        @DisplayName("文件超过 10MB → FORMAT_ERROR")
        void shouldRejectOversizedFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("records.csv");
            when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1);

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(file));
        }

        @Test
        @DisplayName("xlsx 魔数不是 PK → FORMAT_ERROR")
        void shouldRejectFakeXlsx() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("records.xlsx");
            when(file.getSize()).thenReturn(100L);
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("XX".getBytes(StandardCharsets.UTF_8)));

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validatePhysical(file));
        }

        @Test
        @DisplayName("xlsx 魔数为 PK → 通过")
        void shouldAcceptValidXlsx() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("records.xlsx");
            when(file.getSize()).thenReturn(100L);
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("PK".getBytes(StandardCharsets.UTF_8)));

            assertDoesNotThrow(() -> validator.validatePhysical(file));
        }
    }

    // ---------------------------------------------------------------- 第二层

    @Nested
    @DisplayName("第二层：文件解析")
    class ParseLayer {

        @Test
        @DisplayName("合法 CSV → 解析成功")
        void shouldParseValidCsv() throws IOException {
            MultipartFile file = csvFile(CSV_HEADER + "\n" + CSV_ROW);

            List<SrGachaRecord> records = validator.parse(file);

            assertEquals(1, records.size());
            SrGachaRecord record = records.get(0);
            assertEquals("100", record.getGachaId());
            assertEquals("11", record.getGachaType());
            assertEquals("202401011012345612", record.getGachaRecordId());
            assertEquals("1001", record.getItemId());
            assertEquals("Character", record.getItemType());
            assertEquals("某角色", record.getName());
            assertEquals("5", record.getRankType());
            assertEquals("12345612", record.getUid());
            assertNotNull(record.getTime());
        }

        @Test
        @DisplayName("CSV 缺少必需列 → FORMAT_ERROR")
        void shouldRejectCsvMissingRequiredHeader() throws IOException {
            MultipartFile file = csvFile("gacha_id,gacha_type,id,item_id,item_type,name,rank_type,time\n1,11,2,3,x,y,5,2024-01-01 10:00:00");

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.parse(file));
        }

        @Test
        @DisplayName("空 CSV → FILE_EMPTY")
        void shouldRejectEmptyCsv() throws IOException {
            MultipartFile file = csvFile("");

            assertViolation(ViolationTypeEnum.FILE_EMPTY, () -> validator.parse(file));
        }

        @Test
        @DisplayName("数据行数超过 50000 → EXCEED_LIMIT")
        void shouldRejectCsvExceedingRowLimit() throws IOException {
            StringBuilder content = new StringBuilder(CSV_HEADER).append('\n');
            for (int i = 0; i <= GachaRawDataReadListener.MAX_DATA_ROWS; i++) {
                content.append("x\n");
            }
            MultipartFile file = csvFile(content.toString());

            assertViolation(ViolationTypeEnum.EXCEED_LIMIT, () -> validator.parse(file));
        }
    }

    // ---------------------------------------------------------------- 第三层

    @Nested
    @DisplayName("字典配置与卡池类型白名单")
    class GachaTypeLayer {

        @Test
        @DisplayName("字典未配置 → assertDictConfigured 抛 BusinessException（系统问题）")
        void shouldRejectUnconfiguredDictEarly() {
            try (MockedStatic<DictUtils> mocked = mockStatic(DictUtils.class)) {
                mocked.when(() -> DictUtils.getDictCache("sr_gacha_item")).thenReturn(Collections.emptyList());

                assertThrows(BusinessException.class, () -> validator.assertDictConfigured());
            }
        }

        @Test
        @DisplayName("字典已配置 → assertDictConfigured 返回字典数据")
        void shouldReturnDictWhenConfigured() {
            try (MockedStatic<DictUtils> mocked = mockStatic(DictUtils.class)) {
                mocked.when(() -> DictUtils.getDictCache("sr_gacha_item"))
                        .thenReturn(dict("11", "12"));

                assertEquals(2, validator.assertDictConfigured().size());
            }
        }

        @Test
        @DisplayName("字典未配置 → BusinessException（不计用户信用）")
        void shouldRejectUnconfiguredDict() {
            try (MockedStatic<DictUtils> mocked = mockStatic(DictUtils.class)) {
                mocked.when(() -> DictUtils.getDictCache("sr_gacha_item")).thenReturn(Collections.emptyList());

                assertThrows(BusinessException.class, () -> validator.validateGachaType(records("11")));
            }
        }

        @Test
        @DisplayName("卡池类型不在字典 → UNKNOWN_GACHA_TYPE")
        void shouldRejectUnknownGachaType() {
            try (MockedStatic<DictUtils> mocked = mockStatic(DictUtils.class)) {
                mocked.when(() -> DictUtils.getDictCache("sr_gacha_item"))
                        .thenReturn(dict("11", "12"));

                assertViolation(ViolationTypeEnum.UNKNOWN_GACHA_TYPE,
                        () -> validator.validateGachaType(records("99")));
            }
        }

        @Test
        @DisplayName("卡池类型在字典 → 通过")
        void shouldAcceptKnownGachaType() {
            try (MockedStatic<DictUtils> mocked = mockStatic(DictUtils.class)) {
                mocked.when(() -> DictUtils.getDictCache("sr_gacha_item"))
                        .thenReturn(dict("11", "12"));

                assertDoesNotThrow(() -> validator.validateGachaType(records("11", "12")));
            }
        }
    }

    // ---------------------------------------------------------------- 第四层

    @Nested
    @DisplayName("第四层：防伪校验")
    class ForgeryLayer {

        @Test
        @DisplayName("抽卡时间晚于系统当前时间 → DATA_TAMPERED_FUTURE_TIME（重度）")
        void shouldRejectFuturePullTime() {
            enableForgeryCheck();
            // uid 尾号与流水号尾号本就不一致，用于证明时间校验先于其余防伪校验执行
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), "99999999", futureTime(), "11"));

            UploadValidateException ex = assertThrows(UploadValidateException.class,
                    () -> validator.validateForgery(records));

            assertEquals(ViolationTypeEnum.DATA_TAMPERED_FUTURE_TIME, ex.getViolationType());
            assertEquals(1, ex.getViolationType().getIsMalicious().intValue());
        }

        @Test
        @DisplayName("全部记录时间在过去且 uid/尾号/内嵌小时一致 → 通过")
        void shouldAcceptConsistentPastRecords() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(recordId(11, TAIL), UID, pastTime(11, 15), "11"));

            assertDoesNotThrow(() -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("首条 uid 尾号与流水号尾号不一致 → UID_MISMATCH_BASE")
        void shouldRejectBaseUidTailMismatch() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), "12345699", pastTime(10, 30), "11"));

            assertViolation(ViolationTypeEnum.UID_MISMATCH_BASE, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("同一文件出现多个 uid → MULTI_UID_ATTACK")
        void shouldRejectMultiUid() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(recordId(11, TAIL), "99999912", pastTime(11, 15), "11"));

            assertViolation(ViolationTypeEnum.MULTI_UID_ATTACK, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("非首条流水号尾号被篡改 → DATA_TAMPERED_TAIL")
        void shouldRejectTamperedTail() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(recordId(11, "99"), UID, pastTime(11, 15), "11"));

            assertViolation(ViolationTypeEnum.DATA_TAMPERED_TAIL, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("流水号内嵌小时数与抽卡时间不一致 → DATA_TAMPERED_TIME")
        void shouldRejectEmbeddedHourMismatch() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(recordId(11, TAIL), UID, pastTime(12, 15), "11"));

            assertViolation(ViolationTypeEnum.DATA_TAMPERED_TIME, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("非首条流水号缺失 → FORMAT_ERROR")
        void shouldRejectMissingRecordId() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(null, UID, pastTime(11, 15), "11"));

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("非首条抽卡时间缺失 → FORMAT_ERROR")
        void shouldRejectMissingTime() {
            enableForgeryCheck();
            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), UID, pastTime(10, 30), "11"),
                    record(recordId(11, TAIL), UID, null, "11"));

            assertViolation(ViolationTypeEnum.FORMAT_ERROR, () -> validator.validateForgery(records));
        }

        @Test
        @DisplayName("逃生舱关闭防伪校验 → 直接跳过")
        void shouldSkipWhenForgeryCheckDisabled() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(any())).thenReturn("false");

            List<SrGachaRecord> records = List.of(
                    record(recordId(10, TAIL), "99999999", futureTime(), "11"));

            assertDoesNotThrow(() -> validator.validateForgery(records));
        }
    }

    @Nested
    @DisplayName("解析阶段逐行守卫")
    class GuardDuringParse {

        @Test
        @DisplayName("守卫抛出的用户违规原样传播，不被包装成 PARSE_ERROR")
        void shouldPropagateGuardViolation() {
            int[] seen = new int[1];
            assertViolation(ViolationTypeEnum.MULTI_UID_ATTACK, () ->
                    validator.parse(csvFile(CSV_HEADER + "\n" + CSV_ROW + "\n" + CSV_ROW), record -> {
                        if (++seen[0] > 1) {
                            throw new UploadValidateException(ViolationTypeEnum.MULTI_UID_ATTACK, "多账号");
                        }
                    }));
            // 第二行命中即中断，不会继续读完
            assertEquals(2, seen[0]);
        }

        @Test
        @DisplayName("守卫抛出的账号归属业务异常原样传播")
        void shouldPropagateServiceException() {
            assertThrows(ServiceException.class, () ->
                    validator.parse(csvFile(CSV_HEADER + "\n" + CSV_ROW), record -> {
                        throw new ServiceException("账号已被绑定", 40301);
                    }));
        }
    }

    // ---------------------------------------------------------------- 辅助方法

    private static final String CSV_HEADER =
            "gacha_id,gacha_type,id,item_id,item_type,name,rank_type,time,uid";

    private static final String CSV_ROW =
            "100,11,202401011012345612,1001,Character,某角色,5,2024-01-01 10:30:00,12345612";

    private void enableForgeryCheck() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("true");
    }

    private MultipartFile csvFile(String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("records.csv");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        return file;
    }

    private static SrGachaRecord record(String recordId, String uid, Date time, String gachaType) {
        SrGachaRecord record = new SrGachaRecord();
        record.setGachaRecordId(recordId);
        record.setUid(uid);
        record.setTime(time);
        record.setGachaType(gachaType);
        return record;
    }

    private static List<SrGachaRecord> records(String... gachaTypes) {
        List<SrGachaRecord> list = new ArrayList<>();
        for (String type : gachaTypes) {
            SrGachaRecord record = new SrGachaRecord();
            record.setGachaType(type);
            list.add(record);
        }
        return list;
    }

    private static List<SysDictData> dict(String... values) {
        List<SysDictData> list = new ArrayList<>();
        for (String value : values) {
            SysDictData data = new SysDictData();
            data.setDictValue(value);
            list.add(data);
        }
        return list;
    }

    /** 构造 yyyyMMddHH 前缀 + 固定中段 + 尾号的流水号 */
    private static String recordId(int hour, String tail) {
        return String.format("20240101%02d123456%s", hour, tail);
    }

    private static Date pastTime(int hour, int minute) {
        return Date.from(LocalDateTime.of(2024, 1, 1, hour, minute)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private static Date futureTime() {
        return new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
    }

    private static void assertViolation(ViolationTypeEnum expected, Executable executable) {
        UploadValidateException ex = assertThrows(UploadValidateException.class, executable);
        assertEquals(expected, ex.getViolationType());
    }
}
