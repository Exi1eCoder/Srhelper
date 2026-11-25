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
import com.quico.srhelper.domain.SrRelicSet;
import com.quico.srhelper.service.ISrRelicSetService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 遗器一览Controller
 * 
 * @author quico
 * @date 2026-11-24
 */
@Tag(name = "遗器一览Controller", description = "遗器一览相关接口")
@RestController
@RequestMapping("/srhelper/relicSet")
public class SrRelicSetController extends BaseController
{
    @Autowired
    private ISrRelicSetService srRelicSetService;

    /**
     * 查询遗器一览列表
     */
    @Operation(summary = "查询遗器一览列表", description = "分页查询遗器一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrRelicSet srRelicSet)
    {
        List<SrRelicSet> list = srRelicSetService.selectSrRelicSetList(srRelicSet);
        return getDataTable(list);
    }

    /**
     * 导出遗器一览列表
     */
    @Operation(summary = "导出遗器一览列表", description = "导出遗器一览列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:export')")
    @Log(title = "遗器一览", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrRelicSet srRelicSet)
    {
        List<SrRelicSet> list = srRelicSetService.selectSrRelicSetList(srRelicSet);
        ExcelUtil<SrRelicSet> util = new ExcelUtil<SrRelicSet>(SrRelicSet.class);
        util.exportExcel(response, list, "遗器一览数据");
    }

    /**
     * 获取遗器一览详细信息
     */
    @Operation(summary = "获取遗器一览详细信息", description = "根据ID获取遗器一览详细信息（含套装下的遗器部件）")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srRelicSetService.selectSrRelicSetDetail(id));
    }

    /**
     * 新增遗器一览
     */
    @Operation(summary = "新增遗器一览", description = "新增遗器一览")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:add')")
    @Log(title = "遗器一览", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrRelicSet srRelicSet)
    {
        return toAjax(srRelicSetService.insertSrRelicSet(srRelicSet));
    }

    /**
     * 修改遗器一览
     */
    @Operation(summary = "修改遗器一览", description = "更新遗器一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:edit')")
    @Log(title = "遗器一览", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrRelicSet srRelicSet)
    {
        return toAjax(srRelicSetService.updateSrRelicSet(srRelicSet));
    }

    /**
     * 删除遗器一览
     */
    @Operation(summary = "删除遗器一览", description = "批量删除遗器一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:relicSet:remove')")
    @Log(title = "遗器一览", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srRelicSetService.deleteSrRelicSetByIds(ids));
    }
}
