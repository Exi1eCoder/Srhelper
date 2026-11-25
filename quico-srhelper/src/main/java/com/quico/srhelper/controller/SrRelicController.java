package com.quico.srhelper.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.service.ISrRelicService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 遗器图鉴Controller
 * 
 * @author quico
 * @date 2026-11-24
 */
@Tag(name = "遗器图鉴Controller", description = "遗器图鉴相关接口")
@RestController
@RequestMapping("/srhelper/relic")
public class SrRelicController extends BaseController
{
    @Autowired
    private ISrRelicService srRelicService;

    /**
     * 查询遗器图鉴列表
     */
    @Operation(summary = "查询遗器图鉴列表", description = "分页查询遗器图鉴信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrRelic srRelic)
    {
        startPage();
        List<SrRelic> list = srRelicService.selectSrRelicList(srRelic);
        return getDataTable(list);
    }

    /**
     * 导出遗器图鉴列表
     */
    @Operation(summary = "导出遗器图鉴列表", description = "导出遗器图鉴列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:export')")
    @Log(title = "遗器图鉴", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrRelic srRelic)
    {
        List<SrRelic> list = srRelicService.selectSrRelicList(srRelic);
        ExcelUtil<SrRelic> util = new ExcelUtil<SrRelic>(SrRelic.class);
        util.exportExcel(response, list, "遗器图鉴数据");
    }

    /**
     * 查询前端所需的遗器规则数据（仅 5 星）
     */
    @Operation(summary = "查询遗器规则数据", description = "一次性下发 5 星遗器、词条池、套装、档位系数与规则版本")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:list') or @ss.hasPermi('srhelper:relic:list')")
    @GetMapping("/rules")
    public AjaxResult rules()
    {
        return success(srRelicService.selectRelicRuleData());
    }

    /**
     * 获取遗器图鉴详细信息
     */
    @Operation(summary = "获取遗器图鉴详细信息", description = "根据ID获取遗器图鉴详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srRelicService.selectSrRelicById(id));
    }

    /**
     * 新增遗器图鉴
     */
    @Operation(summary = "新增遗器图鉴", description = "新增遗器图鉴")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:add')")
    @Log(title = "遗器图鉴", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrRelic srRelic)
    {
        return toAjax(srRelicService.insertSrRelic(srRelic));
    }

    /**
     * 修改遗器图鉴
     */
    @Operation(summary = "修改遗器图鉴", description = "更新遗器图鉴信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:edit')")
    @Log(title = "遗器图鉴", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrRelic srRelic)
    {
        return toAjax(srRelicService.updateSrRelic(srRelic));
    }

    /**
     * 删除遗器图鉴
     */
    @Operation(summary = "删除遗器图鉴", description = "批量删除遗器图鉴信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relic:remove')")
    @Log(title = "遗器图鉴", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srRelicService.deleteSrRelicByIds(ids));
    }
}
