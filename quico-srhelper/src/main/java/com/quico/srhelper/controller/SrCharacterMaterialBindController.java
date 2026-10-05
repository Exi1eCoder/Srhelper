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
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.SecurityUtils;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.SrCharacterMaterialBindExcel;
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
        List<SrCharacterMaterialBind> list = srCharacterMaterialBindService.selectSrCharacterMaterialBindList(srCharacterMaterialBind);
        return getDataTable(list);
    }

    /**
     * 导入角色材料绑定（透视格式：一行一个角色，四列填材料名称）
     */
    @Operation(summary = "导入角色材料绑定", description = "通过Excel批量导入角色材料绑定，updateSupport=true 时已存在绑定的角色会被覆盖")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:add') or @ss.hasPermi('srhelper:materialBind:edit')")
    @Log(title = "角色材料绑定", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SrCharacterMaterialBindExcel> util = new ExcelUtil<SrCharacterMaterialBindExcel>(SrCharacterMaterialBindExcel.class);
        List<SrCharacterMaterialBindExcel> list = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUserId().toString();
        BindImportResult result = srCharacterMaterialBindService.importBindExcel(list, updateSupport, operName);
        AjaxResult ajax = AjaxResult.success(result.getMessage());
        ajax.put("data", result);
        return ajax;
    }

    /**
     * 导出角色材料绑定（透视格式：一行一个角色，世界掉落/拟造花萼/凝滞虚影/历战余响各一列）
     */
    @Operation(summary = "导出角色材料绑定", description = "按一行一个角色导出四类材料名称")
    @PreAuthorize("@ss.hasPermi('srhelper:materialBind:export')")
    @Log(title = "角色材料绑定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrCharacterMaterialBind srCharacterMaterialBind)
    {
        List<SrCharacterMaterialBindExcel> list = srCharacterMaterialBindService.selectBindExcelList(srCharacterMaterialBind);
        ExcelUtil<SrCharacterMaterialBindExcel> util = new ExcelUtil<SrCharacterMaterialBindExcel>(SrCharacterMaterialBindExcel.class);
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
