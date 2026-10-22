package com.quico.srhelper.service.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;

/**
 * 游戏原始抽卡记录（rawData）EasyExcel 流式读取监听器
 *
 * 表头：gacha_id, gacha_type, id, item_id, item_type, lang, name, rank_type, time, uid
 * 以 Map 方式读取，避免 19 位流水号在数值单元格中被转换成科学计数法后丢精度。
 *
 * @author quico
 */
public class GachaRawDataReadListener extends AnalysisEventListener<Map<Integer, String>>
{
    /** 单次导入最大数据行数 */
    public static final int MAX_DATA_ROWS = 50000;

    /** 必需列 */
    public static final List<String> REQUIRED_HEADERS = List.of(
            "gacha_id", "gacha_type", "id", "item_id", "item_type", "name", "rank_type", "time", "uid");

    private final Map<String, Integer> columnMap = new HashMap<>();

    private final List<SrGachaRecord> records = new ArrayList<>();

    private int rowCount;

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context)
    {
        columnMap.clear();
        if (headMap != null)
        {
            for (Map.Entry<Integer, String> entry : headMap.entrySet())
            {
                String name = normalizeHeader(entry.getValue());
                if (StringUtils.isNotEmpty(name))
                {
                    columnMap.put(name, entry.getKey());
                }
            }
        }
        checkRequiredHeaders(columnMap);
    }

    @Override
    public void invoke(Map<Integer, String> row, AnalysisContext context)
    {
        if (row == null || row.isEmpty())
        {
            return;
        }
        rowCount++;
        if (rowCount > MAX_DATA_ROWS)
        {
            throw new UploadValidateException(ViolationTypeEnum.EXCEED_LIMIT,
                    "单次导入不能超过 " + MAX_DATA_ROWS + " 条记录");
        }
        if (isBlankRow(row))
        {
            return;
        }
        records.add(buildRecord(columnMap, row::get));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context)
    {
        // 流式读取结束，无需额外处理
    }

    public List<SrGachaRecord> getRecords()
    {
        return records;
    }

    /**
     * 表头归一化：去 BOM、去首尾空白、转小写
     */
    public static String normalizeHeader(String header)
    {
        if (header == null)
        {
            return null;
        }
        String name = header.replace("\uFEFF", "").trim().toLowerCase();
        return name.isEmpty() ? null : name;
    }

    /**
     * 校验必需列是否齐全
     */
    public static void checkRequiredHeaders(Map<String, Integer> columnMap)
    {
        List<String> missing = new ArrayList<>();
        for (String header : REQUIRED_HEADERS)
        {
            if (!columnMap.containsKey(header))
            {
                missing.add(header);
            }
        }
        if (!missing.isEmpty())
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR,
                    "文件缺少必要的列：" + String.join(",", missing));
        }
    }

    /**
     * 按表头映射构建一条抽卡记录
     *
     * @param columnMap 列名 -> 列索引
     * @param cellReader 列索引 -> 原始单元格字符串
     */
    public static SrGachaRecord buildRecord(Map<String, Integer> columnMap, IntFunction<String> cellReader)
    {
        return SrGachaRecord.builder()
                .gachaId(normalizeNumeric(readCell(columnMap, cellReader, "gacha_id")))
                .gachaType(normalizeNumeric(readCell(columnMap, cellReader, "gacha_type")))
                .gachaRecordId(normalizeNumeric(readCell(columnMap, cellReader, "id")))
                .itemId(normalizeNumeric(readCell(columnMap, cellReader, "item_id")))
                .itemType(readCell(columnMap, cellReader, "item_type"))
                .name(readCell(columnMap, cellReader, "name"))
                .rankType(normalizeNumeric(readCell(columnMap, cellReader, "rank_type")))
                .time(parseTime(readCell(columnMap, cellReader, "time")))
                .uid(normalizeNumeric(readCell(columnMap, cellReader, "uid")))
                .build();
    }

    /**
     * 解析时间字符串，无法解析时返回 null，由第四层统一判定
     */
    public static java.util.Date parseTime(String timeStr)
    {
        if (StringUtils.isEmpty(timeStr))
        {
            return null;
        }
        return DateUtils.parseDate(timeStr);
    }

    private static String readCell(Map<String, Integer> columnMap, IntFunction<String> cellReader, String column)
    {
        Integer index = columnMap.get(column);
        if (index == null || index < 0)
        {
            return null;
        }
        String value = cellReader.apply(index);
        return value == null ? null : value.trim();
    }

    /**
     * 数值单元格归一化：展开科学计数法、去掉多余小数位，避免 19 位流水号丢精度
     */
    public static String normalizeNumeric(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }
        if (trimmed.indexOf('E') >= 0 || trimmed.indexOf('e') >= 0 || trimmed.endsWith(".0"))
        {
            try
            {
                return new BigDecimal(trimmed).stripTrailingZeros().toPlainString();
            }
            catch (NumberFormatException ignored)
            {
                // 非数值文本保持原样
            }
        }
        return trimmed;
    }

    private static boolean isBlankRow(Map<Integer, String> row)
    {
        for (String value : row.values())
        {
            if (StringUtils.isNotEmpty(value) && StringUtils.isNotEmpty(value.trim()))
            {
                return false;
            }
        }
        return true;
    }
}
