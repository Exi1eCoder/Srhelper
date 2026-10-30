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
import com.quico.srhelper.domain.SrReleaseVersion;
import com.quico.srhelper.service.ISrReleaseVersionService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 游戏版本信息Controller
 * 
 * @author quico
 * @date 2026-10-29
 */
@Tag(name = "游戏版本信息Controller", description = "游戏版本信息相关接口")
@RestController
@RequestMapping("/srhelper/releaseVersion")
public class SrReleaseVersionController extends BaseController
{
    @Autowired
    private ISrReleaseVersionService srReleaseVersionService;

    /**
     * 查询游戏版本信息列表
     */
    @Operation(summary = "查询游戏版本信息列表", description = "分页查询游戏版本信息信息")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrReleaseVersion srReleaseVersion)
    {
        List<SrReleaseVersion> list = srReleaseVersionService.selectSrReleaseVersionList(srReleaseVersion);
        return getDataTable(list);
    }

    /**
     * 导出游戏版本信息列表
     */
    @Operation(summary = "导出游戏版本信息列表", description = "导出游戏版本信息列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:export')")
    @Log(title = "游戏版本信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrReleaseVersion srReleaseVersion)
    {
        List<SrReleaseVersion> list = srReleaseVersionService.selectSrReleaseVersionList(srReleaseVersion);
        ExcelUtil<SrReleaseVersion> util = new ExcelUtil<SrReleaseVersion>(SrReleaseVersion.class);
        util.exportExcel(response, list, "游戏版本信息数据");
    }

    /**
     * 获取游戏版本信息详细信息
     */
    @Operation(summary = "获取游戏版本信息详细信息", description = "根据ID获取游戏版本信息详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srReleaseVersionService.selectSrReleaseVersionById(id));
    }

    /**
     * 新增游戏版本信息
     */
    @Operation(summary = "新增游戏版本信息", description = "新增游戏版本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:add')")
    @Log(title = "游戏版本信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrReleaseVersion srReleaseVersion)
    {
        return toAjax(srReleaseVersionService.insertSrReleaseVersion(srReleaseVersion));
    }

    /**
     * 修改游戏版本信息
     */
    @Operation(summary = "修改游戏版本信息", description = "更新游戏版本信息信息")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:edit')")
    @Log(title = "游戏版本信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrReleaseVersion srReleaseVersion)
    {
        return toAjax(srReleaseVersionService.updateSrReleaseVersion(srReleaseVersion));
    }

    /**
     * 删除游戏版本信息
     */
    @Operation(summary = "删除游戏版本信息", description = "批量删除游戏版本信息信息")
    @PreAuthorize("@ss.hasPermi('srhelper:releaseVersion:remove')")
    @Log(title = "游戏版本信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srReleaseVersionService.deleteSrReleaseVersionByIds(ids));
    }
}
