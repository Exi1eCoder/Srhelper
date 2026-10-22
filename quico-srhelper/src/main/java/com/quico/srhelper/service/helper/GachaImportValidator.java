package com.quico.srhelper.service.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.HttpStatus;
import com.quico.common.core.domain.entity.SysDictData;
import com.quico.common.exception.BusinessException;
import com.quico.common.utils.DictUtils;
import com.quico.common.utils.StringUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 抽卡记录导入校验器（第一 ~ 第四层漏斗）
 *
 * 第一层：物理与前置拦截（后缀、大小、xlsx 魔数）
 * 第二层：EasyExcel 流式解析（空文件、行数上限、解析异常）
 * 第三层：sr_gacha_item 字典白名单
 * 第四层：纯文件级防伪（是否包含rawData表、uid/流水号尾号、流水号内嵌小时数）
 *
 * 第五层风控与审计见 {@link GachaUploadRiskService}
 *
 * @author quico
 */
@Slf4j
@Service
public class GachaImportValidator
{
    /** 文件大小上限：10MB */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    /** 允许的文件后缀 */
    private static final Set<String> ALLOWED_SUFFIXES = new HashSet<>(Arrays.asList("xls", "xlsx", "csv"));

    /** 卡池类型字典 */
    private static final String DICT_TYPE_GACHA = "sr_gacha_item";

    /** 抽卡记录数据所在工作表 */
    private static final String RAW_DATA_SHEET = "rawData";

    /** 防伪校验每批处理条数 */
    private static final int FORGERY_BATCH_SIZE = 500;

    /** 时间前缀格式：yyyyMMddHH */
    private static final DateTimeFormatter DATE_HOUR_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMddHH").withResolverStyle(ResolverStyle.STRICT);

    /** 合理时间范围：2000-01-01 ~ 2100-01-01 */
    private static final long MIN_EPOCH_MILLIS = 946684800000L;
    private static final long MAX_EPOCH_MILLIS = 4102444800000L;
    private static final long MIN_EPOCH_SECONDS = 946684800L;
    private static final long MAX_EPOCH_SECONDS = 4102444800L;

    /**
     * 防伪校验逃生舱：读取 RuoYi 参数设置缓存（sys_config:{configKey}），未配置时默认开启校验
     */
    private static final String FORGERY_CHECK_CONFIG_KEY = "srhelper.gacha.import.forgery-check-enabled";

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 第一层：物理与前置拦截
     */
    public void validatePhysical(MultipartFile file)
    {
        if (file == null || file.getOriginalFilename() == null)
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR, "上传文件不存在");
        }

        String suffix = resolveSuffix(file);
        if (!ALLOWED_SUFFIXES.contains(suffix))
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "不支持的文件类型：" + suffix);
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "文件大小超过 10MB");
        }
        if ("xlsx".equals(suffix) && !hasZipMagic(file))
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "文件不是有效的 xlsx 文件");
        }
    }

    /**
     * 第二层：解析文件（EasyExcel 流式解析 Excel，CSV 走轻量解析）
     */
    public List<SrGachaRecord> parse(MultipartFile file)
    {
        String suffix = resolveSuffix(file);
        List<SrGachaRecord> records = "csv".equals(suffix) ? parseCsv(file) : parseExcel(file);
        if (records == null || records.isEmpty())
        {
            throw new UploadValidateException(ViolationTypeEnum.FILE_EMPTY, "文件内容为空");
        }
        return records;
    }

    /**
     * 第三层：卡池类型字典白名单校验
     */
    public void validateGachaType(List<SrGachaRecord> records)
    {
        List<SysDictData> dictData = DictUtils.getDictCache(DICT_TYPE_GACHA);
        if (dictData == null || dictData.isEmpty())
        {
            // 字典未初始化属于系统问题，不计入用户信用
            throw new BusinessException(HttpStatus.ERROR, "卡池类型字典未配置，请联系管理员");
        }
        Set<String> allowedTypes = new HashSet<>();
        for (SysDictData dict : dictData)
        {
            allowedTypes.add(dict.getDictValue());
        }
        for (SrGachaRecord record : records)
        {
            if (!allowedTypes.contains(record.getGachaType()))
            {
                throw new UploadValidateException(ViolationTypeEnum.UNKNOWN_GACHA_TYPE,
                        "存在未知的卡池类型：" + record.getGachaType());
            }
        }
    }

    /**
     * 第四层：纯文件级防伪校验（逃生舱开启时跳过）
     */
    public void validateForgery(List<SrGachaRecord> records)
    {
        if (!isForgeryCheckEnabled())
        {
            log.warn("抽卡记录防伪校验已通过参数 {} 关闭，本次导入跳过第四层校验", FORGERY_CHECK_CONFIG_KEY);
            return;
        }
        if (records == null || records.isEmpty())
        {
            throw new UploadValidateException(ViolationTypeEnum.FILE_EMPTY, "文件内容为空");
        }

        SrGachaRecord base = records.get(0);
        String baseUid = base.getUid();
        String baseRecordId = base.getGachaRecordId();
        if (StringUtils.isEmpty(baseUid) || baseUid.length() < 2)
        {
            throw new UploadValidateException(ViolationTypeEnum.UID_MISMATCH_BASE, "首条记录 uid 缺失或格式异常");
        }
        if (StringUtils.isEmpty(baseRecordId) || baseRecordId.length() < 2)
        {
            throw new UploadValidateException(ViolationTypeEnum.UID_MISMATCH_BASE, "首条记录流水号缺失或格式异常");
        }
        if (base.getTime() == null)
        {
            throw new UploadValidateException(ViolationTypeEnum.UID_MISMATCH_BASE, "首条记录抽卡时间缺失或格式异常");
        }

        String baseUidTail = lastTwo(baseUid);
        String baseRecordTail = lastTwo(baseRecordId);
        if (!baseUidTail.equals(baseRecordTail))
        {
            throw new UploadValidateException(ViolationTypeEnum.UID_MISMATCH_BASE,
                    "首条记录 uid 尾号与流水号尾号不一致");
        }

        IdTimeStyle idTimeStyle = detectIdTimeStyle(baseRecordId);
        if (idTimeStyle == null)
        {
            throw new UploadValidateException(ViolationTypeEnum.UID_MISMATCH_BASE,
                    "首条记录流水号中无法识别抽卡时间");
        }

        int total = records.size();
        for (int from = 0; from < total; from += FORGERY_BATCH_SIZE)
        {
            int to = Math.min(from + FORGERY_BATCH_SIZE, total);
            for (int i = from; i < to; i++)
            {
                validateRecordForForgery(records.get(i), i, baseUid, baseRecordTail, idTimeStyle);
            }
        }
    }

    /**
     * 读取 RuoYi 参数设置缓存判断防伪校验开关
     * 未配置或读取异常时默认开启，避免逃生舱被误关
     */
    private boolean isForgeryCheckEnabled()
    {
        try
        {
            Object value = redisTemplate.opsForValue().get(CacheConstants.SYS_CONFIG_KEY + FORGERY_CHECK_CONFIG_KEY);
            if (value == null)
            {
                return true;
            }
            return !"false".equalsIgnoreCase(value.toString().trim());
        }
        catch (Exception e)
        {
            log.error("读取抽卡记录防伪校验开关失败，默认开启校验", e);
            return true;
        }
    }

    /**
     * 单条记录防伪校验
     */
    private void validateRecordForForgery(SrGachaRecord record, int index, String baseUid,
                                          String baseRecordTail, IdTimeStyle idTimeStyle)
    {
        int rowNo = index + 1;
        if (!baseUid.equals(record.getUid()))
        {
            throw new UploadValidateException(ViolationTypeEnum.MULTI_UID_ATTACK,
                    "第 " + rowNo + " 条记录 uid 与首条记录不一致");
        }

        String recordId = record.getGachaRecordId();
        if (StringUtils.isEmpty(recordId) || recordId.length() < 2)
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "第 " + rowNo + " 条记录流水号缺失");
        }
        if (!lastTwo(recordId).equals(baseRecordTail))
        {
            throw new UploadValidateException(ViolationTypeEnum.DATA_TAMPERED_TAIL,
                    "第 " + rowNo + " 条记录流水号尾号与基准不一致");
        }

        if (record.getTime() == null)
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "第 " + rowNo + " 条记录抽卡时间缺失或格式错误");
        }
        int hourFromId = extractHour(recordId, idTimeStyle);
        if (hourFromId < 0 || hourFromId != toLocalHour(record.getTime()))
        {
            throw new UploadValidateException(ViolationTypeEnum.DATA_TAMPERED_TIME,
                    "第 " + rowNo + " 条记录流水号内嵌小时数与抽卡时间不一致");
        }
    }

    /**
     * EasyExcel 流式解析 rawData sheet
     */
    private List<SrGachaRecord> parseExcel(MultipartFile file)
    {
        String sheetName = resolveRawDataSheet(file);
        try (InputStream inputStream = file.getInputStream())
        {
            GachaRawDataReadListener listener = new GachaRawDataReadListener();
            EasyExcel.read(inputStream, listener)
                    .headRowNumber(1)
                    .sheet(sheetName)
                    .doRead();
            return listener.getRecords();
        }
        catch (UploadValidateException e)
        {
            throw e;
        }
        catch (ExcelAnalysisException e)
        {
            UploadValidateException validateException = findUploadValidateException(e);
            if (validateException != null)
            {
                throw validateException;
            }
            throw new UploadValidateException(ViolationTypeEnum.PARSE_ERROR,
                    "文件解析失败，请确认上传的是游戏原始导出文件");
        }
        catch (Exception e)
        {
            UploadValidateException validateException = findUploadValidateException(e);
            if (validateException != null)
            {
                throw validateException;
            }
            log.warn("抽卡记录文件解析异常", e);
            throw new UploadValidateException(ViolationTypeEnum.PARSE_ERROR,
                    "文件解析失败，请确认上传的是游戏原始导出文件");
        }
    }

    /**
     * 解析前确认 rawData 工作表存在，并返回文件中的实际工作表名
     * EasyExcel 指定不存在的 sheet 时不会回调任何行，这里提前给出明确原因
     */
    private String resolveRawDataSheet(MultipartFile file)
    {
        try (InputStream inputStream = file.getInputStream();
             ExcelReader reader = EasyExcel.read(inputStream).build())
        {
            List<String> sheetNames = new ArrayList<>();
            String matchedName = null;
            for (ReadSheet sheet : reader.excelExecutor().sheetList())
            {
                String name = sheet.getSheetName();
                if (StringUtils.isEmpty(name))
                {
                    continue;
                }
                String trimmed = name.trim();
                sheetNames.add(trimmed);
                if (matchedName == null && RAW_DATA_SHEET.equalsIgnoreCase(trimmed))
                {
                    matchedName = trimmed;
                }
            }
            if (matchedName == null)
            {
                throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                        "文件中不存在 " + RAW_DATA_SHEET + " 工作表，当前工作表："
                                + (sheetNames.isEmpty() ? "无" : String.join("、", sheetNames)));
            }
            return matchedName;
        }
        catch (UploadValidateException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.warn("读取 Excel 工作表列表失败", e);
            throw new UploadValidateException(ViolationTypeEnum.PARSE_ERROR,
                    "文件解析失败，请确认上传的是游戏原始导出文件");
        }
    }

    /**
     * CSV 轻量解析（首行表头，UTF-8）
     */
    private List<SrGachaRecord> parseCsv(MultipartFile file)
    {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)))
        {
            String headerLine = nextNonBlankLine(reader);
            if (headerLine == null)
            {
                throw new UploadValidateException(ViolationTypeEnum.FILE_EMPTY, "文件内容为空");
            }

            List<String> headers = splitCsvLine(headerLine);
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++)
            {
                String name = GachaRawDataReadListener.normalizeHeader(headers.get(i));
                if (name != null)
                {
                    columnMap.put(name, i);
                }
            }
            GachaRawDataReadListener.checkRequiredHeaders(columnMap);

            List<SrGachaRecord> records = new ArrayList<>();
            int rowCount = 0;
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isEmpty())
                {
                    continue;
                }
                rowCount++;
                if (rowCount > GachaRawDataReadListener.MAX_DATA_ROWS)
                {
                    throw new UploadValidateException(ViolationTypeEnum.EXCEED_LIMIT,
                            "单次导入不能超过 " + GachaRawDataReadListener.MAX_DATA_ROWS + " 条记录");
                }
                List<String> cells = splitCsvLine(line);
                records.add(GachaRawDataReadListener.buildRecord(columnMap,
                        index -> index < cells.size() ? cells.get(index) : null));
            }
            return records;
        }
        catch (UploadValidateException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            UploadValidateException validateException = findUploadValidateException(e);
            if (validateException != null)
            {
                throw validateException;
            }
            log.warn("抽卡记录 CSV 解析异常", e);
            throw new UploadValidateException(ViolationTypeEnum.PARSE_ERROR,
                    "文件解析失败，请确认上传的是游戏原始导出文件");
        }
    }

    private String nextNonBlankLine(BufferedReader reader) throws java.io.IOException
    {
        String line;
        while ((line = reader.readLine()) != null)
        {
            if (!line.trim().isEmpty())
            {
                return line.replace("\uFEFF", "");
            }
        }
        return null;
    }

    /**
     * 简单 CSV 行拆分，支持双引号包裹与连续双引号转义
     */
    private List<String> splitCsvLine(String line)
    {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (c == '"')
            {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"')
                {
                    current.append('"');
                    i++;
                }
                else
                {
                    inQuotes = !inQuotes;
                }
            }
            else if (c == ',' && !inQuotes)
            {
                cells.add(current.toString());
                current.setLength(0);
            }
            else
            {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    private String resolveSuffix(MultipartFile file)
    {
        String originalFilename = file.getOriginalFilename();
        int dot = originalFilename == null ? -1 : originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1)
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR, "文件缺少扩展名");
        }
        return originalFilename.substring(dot + 1).toLowerCase();
    }

    /**
     * xlsx 魔数校验：必须以 PK 开头
     */
    private boolean hasZipMagic(MultipartFile file)
    {
        try (InputStream inputStream = file.getInputStream())
        {
            int first = inputStream.read();
            int second = inputStream.read();
            return first == 'P' && second == 'K';
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private UploadValidateException findUploadValidateException(Throwable throwable)
    {
        Throwable current = throwable;
        while (current != null)
        {
            if (current instanceof UploadValidateException)
            {
                return (UploadValidateException) current;
            }
            current = current.getCause();
        }
        return null;
    }

    private String lastTwo(String value)
    {
        return value.substring(value.length() - 2);
    }

    /**
     * 流水号时间编码方式
     */
    private enum IdTimeStyle
    {
        /** yyyyMMddHH 前缀 */
        DATE_PREFIX,
        /** 前 13 位为毫秒时间戳 */
        EPOCH_MILLIS,
        /** 前 10 位为秒时间戳 */
        EPOCH_SECONDS
    }

    private IdTimeStyle detectIdTimeStyle(String recordId)
    {
        if (isDateTimePrefix(recordId))
        {
            return IdTimeStyle.DATE_PREFIX;
        }
        Long millis = leadingLong(recordId, 13);
        if (millis != null && millis >= MIN_EPOCH_MILLIS && millis <= MAX_EPOCH_MILLIS)
        {
            return IdTimeStyle.EPOCH_MILLIS;
        }
        Long seconds = leadingLong(recordId, 10);
        if (seconds != null && seconds >= MIN_EPOCH_SECONDS && seconds <= MAX_EPOCH_SECONDS)
        {
            return IdTimeStyle.EPOCH_SECONDS;
        }
        return null;
    }

    private boolean isDateTimePrefix(String recordId)
    {
        if (recordId.length() < 10)
        {
            return false;
        }
        try
        {
            LocalDateTime.parse(recordId.substring(0, 10), DATE_HOUR_FORMATTER);
            return true;
        }
        catch (DateTimeParseException e)
        {
            return false;
        }
    }

    private Long leadingLong(String value, int length)
    {
        if (value.length() < length)
        {
            return null;
        }
        try
        {
            return Long.parseLong(value.substring(0, length));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private int extractHour(String recordId, IdTimeStyle idTimeStyle)
    {
        try
        {
            switch (idTimeStyle)
            {
                case DATE_PREFIX:
                    return recordId.length() < 10 ? -1 : Integer.parseInt(recordId.substring(8, 10));
                case EPOCH_MILLIS:
                    Long millis = leadingLong(recordId, 13);
                    return millis == null ? -1 : Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).getHour();
                case EPOCH_SECONDS:
                    Long seconds = leadingLong(recordId, 10);
                    return seconds == null ? -1 : Instant.ofEpochSecond(seconds)
                            .atZone(ZoneId.systemDefault()).getHour();
                default:
                    return -1;
            }
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    private int toLocalHour(java.util.Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).getHour();
    }
}
