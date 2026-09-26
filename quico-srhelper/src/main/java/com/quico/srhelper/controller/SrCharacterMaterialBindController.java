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
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.service.ISrCharacterMaterialBindService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 角色材料绑定Controller
 * 
 * @author quico
 * @date 2026-06-14
 */
@Tag(name = "角色材料绑定Controller", description = "角色材料绑定相关接口")
@RestController
@RequestMapping("/srhelper/materialBind")
public class SrCharacterMaterialBindController extends BaseController
{
    @Autowired
    private ISrCharacterMaterialBindService srCharacterMaterialBindService;

    /**
     * 查询角色材料绑定列表
     */
    @Operation(summary = "查询角色材料绑定列表", description = "分页查询角色材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        startPage();
        List<SrCharacterMaterialBind> list = srCharacterMaterialBindService.selectSrCharacterMaterialBindList(srCharacterMaterialBind);
        return getDataTable(list);
    }

    /**
     * 导出角色材料绑定列表
     */
    @Operation(summary = "导出角色材料绑定列表", description = "导出角色材料绑定列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:export')")
    @Log(title = "角色材料绑定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrCharacterMaterialBind srCharacterMaterialBind)
    {
        List<SrCharacterMaterialBind> list = srCharacterMaterialBindService.selectSrCharacterMaterialBindList(srCharacterMaterialBind);
        ExcelUtil<SrCharacterMaterialBind> util = new ExcelUtil<SrCharacterMaterialBind>(SrCharacterMaterialBind.class);
        util.exportExcel(response, list, "角色材料绑定数据");
    }

    /**
     * 获取角色材料绑定详细信息
     */
    @Operation(summary = "获取角色材料绑定详细信息", description = "根据ID获取角色材料绑定详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srCharacterMaterialBindService.selectSrCharacterMaterialBindById(id));
    }

    /**
     * 新增角色材料绑定
     */
    @Operation(summary = "新增角色材料绑定", description = "新增角色材料绑定")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:add')")
    @Log(title = "角色材料绑定", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacterMaterialBind srCharacterMaterialBind)
    {
        return toAjax(srCharacterMaterialBindService.insertSrCharacterMaterialBind(srCharacterMaterialBind));
    }

    /**
     * 修改角色材料绑定
     */
    @Operation(summary = "修改角色材料绑定", description = "更新角色材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:edit')")
    @Log(title = "角色材料绑定", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacterMaterialBind srCharacterMaterialBind)
    {
        return toAjax(srCharacterMaterialBindService.updateSrCharacterMaterialBind(srCharacterMaterialBind));
    }

    /**
     * 删除角色材料绑定
     */
    @Operation(summary = "删除角色材料绑定", description = "批量删除角色材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:remove')")
    @Log(title = "角色材料绑定", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srCharacterMaterialBindService.deleteSrCharacterMaterialBindByIds(ids));
    }
}
