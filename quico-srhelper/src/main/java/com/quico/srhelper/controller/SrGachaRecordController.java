package com.quico.srhelper.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.service.ISrGachaRecordService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 跃迁记录Controller
 * 
 * @author quico
 * @date 2026-08-19
 */
@Tag(name = "跃迁记录Controller", description = "跃迁记录相关接口")
@RestController
@RequestMapping("/srhelper/gachaRecord")
@Slf4j
public class SrGachaRecordController extends BaseController
{
    @Autowired
    private ISrGachaRecordService srGachaRecordService;

    /**
     * 查询当前用户所有去重uid（游戏账号）
     */
    @Operation(summary = "查询账号列表", description = "查询当前用户导入过的所有游戏账号uid")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/uidList")
    public AjaxResult uidList()
    {
        String createBy = SecurityUtils.getUserId().toString();
        List<String> uidList = srGachaRecordService.selectDistinctUidList(createBy);
        log.debug("查询uid列表结果, createBy={}, size={}, uidList={}", createBy, uidList.size(), uidList);
        return success(uidList);
    }

    /**
     * 查询跃迁记录列表
     */
    @Operation(summary = "查询跃迁记录列表", description = "分页查询跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setCreateBy(SecurityUtils.getUserId().toString());
        startPage();
        List<SrGachaRecord> list = srGachaRecordService.selectSrGachaRecordList(srGachaRecord);
        return getDataTable(list);
    }

    /**
     * 抽卡分析：按卡池类型分组返回五星记录
     */
    @Operation(summary = "抽卡分析", description = "按卡池类型分组返回五星记录")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/analysis")
    public AjaxResult analysis(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setCreateBy(SecurityUtils.getUserId().toString());
        return success(srGachaRecordService.analysisGachaRecord(srGachaRecord));
    }

    /**
     * 导出跃迁记录列表
     */
    @Operation(summary = "导出跃迁记录列表", description = "导出跃迁记录列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:export')")
    @Log(title = "跃迁记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrGachaRecord srGachaRecord)
    {
        List<SrGachaRecord> list = srGachaRecordService.selectSrGachaRecordList(srGachaRecord);
        ExcelUtil<SrGachaRecord> util = new ExcelUtil<SrGachaRecord>(SrGachaRecord.class);
        util.exportExcel(response, list, "跃迁记录数据");
    }

    /**
     * 获取跃迁记录详细信息
     */
    @Operation(summary = "获取跃迁记录详细信息", description = "根据ID获取跃迁记录详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:query')")
    @GetMapping(value = "/{gachaRecordId}")
    public AjaxResult getInfo(@PathVariable("gachaRecordId") String gachaRecordId)
    {
        return success(srGachaRecordService.selectSrGachaRecordByGachaRecordId(gachaRecordId));
    }

    /**
     * 新增跃迁记录
     */
    @Operation(summary = "新增跃迁记录", description = "新增跃迁记录")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:add')")
    @Log(title = "跃迁记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrGachaRecord srGachaRecord)
    {
        return toAjax(srGachaRecordService.insertSrGachaRecord(srGachaRecord));
    }

    /**
     * 修改跃迁记录
     */
    @Operation(summary = "修改跃迁记录", description = "更新跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:edit')")
    @Log(title = "跃迁记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrGachaRecord srGachaRecord)
    {
        return toAjax(srGachaRecordService.updateSrGachaRecord(srGachaRecord));
    }

    /**
     * 删除跃迁记录
     */
    @Operation(summary = "删除跃迁记录", description = "批量删除跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:remove')")
    @Log(title = "跃迁记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{gachaRecordIds}")
    public AjaxResult remove(@PathVariable String[] gachaRecordIds)
    {
        return toAjax(srGachaRecordService.deleteSrGachaRecordByGachaRecordIds(gachaRecordIds));
    }

    /**
     * 导入跃迁记录（解析Excel的rawData sheet）
     */
    @Operation(summary = "导入跃迁记录", description = "从星穹铁道跃迁记录Excel的rawData sheet导入抽卡数据")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:import')")
    @Log(title = "跃迁记录", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        List<SrGachaRecord> recordList = parseRawData(file);
        // create_by 存当前登录用户ID
        String operName = SecurityUtils.getUserId().toString();
        String message = srGachaRecordService.importGachaRecord(recordList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     */
    @Operation(summary = "下载导入模板", description = "下载跃迁记录导入模板")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SrGachaRecord> util = new ExcelUtil<SrGachaRecord>(SrGachaRecord.class);
        util.importTemplateExcel(response, "rawData");
    }

    /**
     * 解析Excel中的rawData sheet（游戏原始抽卡记录格式）
     * 表头：gacha_id, gacha_type, id, item_id, item_type, lang, name, rank_type, time, uid
     */
    private List<SrGachaRecord> parseRawData(MultipartFile file) throws Exception
    {
        List<SrGachaRecord> list = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream()))
        {
            Sheet sheet = wb.getSheet("rawData");
            if (sheet == null)
            {
                throw new RuntimeException("文件中不存在 rawData sheet");
            }

            // 读取表头，建立 列名 -> 列索引 映射
            Row header = sheet.getRow(0);
            if (header == null)
            {
                throw new RuntimeException("rawData sheet 表头为空");
            }
            Map<String, Integer> columnMap = new HashMap<>();
            for (Cell cell : header)
            {
                String name = formatter.formatCellValue(cell).trim();
                columnMap.put(name, cell.getColumnIndex());
            }

            // 数据从第2行开始
            for (int i = 1; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row == null)
                {
                    continue;
                }
                SrGachaRecord record = SrGachaRecord.builder()
                        .gachaId(getCellValue(row, columnMap.get("gacha_id"), formatter))
                        .gachaType(getCellValue(row, columnMap.get("gacha_type"), formatter))
                        .gachaRecordId(getCellValue(row, columnMap.get("id"), formatter))
                        .itemId(getCellValue(row, columnMap.get("item_id"), formatter))
                        .itemType(getCellValue(row, columnMap.get("item_type"), formatter))
                        .name(getCellValue(row, columnMap.get("name"), formatter))
                        .rankType(getCellValue(row, columnMap.get("rank_type"), formatter))
                        .time(parseTime(getCellValue(row, columnMap.get("time"), formatter)))
                        .uid(getCellValue(row, columnMap.get("uid"), formatter))
                        .build();
                list.add(record);
            }
        }
        return list;
    }

    /**
     * 读取单元格字符串值
     */
    private String getCellValue(Row row, Integer index, DataFormatter formatter)
    {
        if (index == null)
        {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null)
        {
            return null;
        }
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 解析时间字符串
     */
    private java.util.Date parseTime(String timeStr)
    {
        if (timeStr == null || timeStr.isEmpty())
        {
            return null;
        }
        return DateUtils.parseDate(timeStr);
    }
}
