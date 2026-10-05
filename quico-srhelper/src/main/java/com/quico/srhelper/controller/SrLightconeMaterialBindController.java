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
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.SrLightconeMaterialBindExcel;
import com.quico.srhelper.service.ISrLightconeMaterialBindService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 光锥材料绑定Controller
 * 
 * @author quico
 * @date 2026-08-14
 */
@Tag(name = "光锥材料绑定Controller", description = "光锥材料绑定相关接口")
@RestController
@RequestMapping("/srhelper/lightconeMaterialBind")
public class SrLightconeMaterialBindController extends BaseController
{
    @Autowired
    private ISrLightconeMaterialBindService srLightconeMaterialBindService;

    /**
     * 查询光锥材料绑定列表
     */
    @Operation(summary = "查询光锥材料绑定列表", description = "分页查询光锥材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        List<SrLightconeMaterialBind> list = srLightconeMaterialBindService.selectSrLightconeMaterialBindList(srLightconeMaterialBind);
        return getDataTable(list);
    }

    /**
     * 导入光锥材料绑定（透视格式：一行一个光锥，两列填材料名称）
     */
    @Operation(summary = "导入光锥材料绑定", description = "通过Excel批量导入光锥材料绑定，updateSupport=true 时已存在绑定的光锥会被覆盖")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:add') or @ss.hasPermi('srhelper:lightconeMaterialBind:edit')")
    @Log(title = "光锥材料绑定", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SrLightconeMaterialBindExcel> util = new ExcelUtil<SrLightconeMaterialBindExcel>(SrLightconeMaterialBindExcel.class);
        List<SrLightconeMaterialBindExcel> list = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUserId().toString();
        BindImportResult result = srLightconeMaterialBindService.importBindExcel(list, updateSupport, operName);
        AjaxResult ajax = AjaxResult.success(result.getMessage());
        ajax.put("data", result);
        return ajax;
    }

    /**
     * 导出光锥材料绑定（透视格式：一行一个光锥，世界掉落/拟造花萼各一列）
     */
    @Operation(summary = "导出光锥材料绑定", description = "按一行一个光锥导出两类材料名称")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:export')")
    @Log(title = "光锥材料绑定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrLightconeMaterialBind srLightconeMaterialBind)
    {
        List<SrLightconeMaterialBindExcel> list = srLightconeMaterialBindService.selectBindExcelList(srLightconeMaterialBind);
        ExcelUtil<SrLightconeMaterialBindExcel> util = new ExcelUtil<SrLightconeMaterialBindExcel>(SrLightconeMaterialBindExcel.class);
        util.exportExcel(response, list, "光锥材料绑定数据");
    }

    /**
     * 获取光锥材料绑定详细信息
     */
    @Operation(summary = "获取光锥材料绑定详细信息", description = "根据ID获取光锥材料绑定详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srLightconeMaterialBindService.selectSrLightconeMaterialBindById(id));
    }

    /**
     * 新增光锥材料绑定
     */
    @Operation(summary = "新增光锥材料绑定", description = "新增光锥材料绑定")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:add')")
    @Log(title = "光锥材料绑定", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrLightconeMaterialBind srLightconeMaterialBind)
    {
        return toAjax(srLightconeMaterialBindService.insertSrLightconeMaterialBind(srLightconeMaterialBind));
    }

    /**
     * 修改光锥材料绑定
     */
    @Operation(summary = "修改光锥材料绑定", description = "更新光锥材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:edit')")
    @Log(title = "光锥材料绑定", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrLightconeMaterialBind srLightconeMaterialBind)
    {
        return toAjax(srLightconeMaterialBindService.updateSrLightconeMaterialBind(srLightconeMaterialBind));
    }

    /**
     * 删除光锥材料绑定
     */
    @Operation(summary = "删除光锥材料绑定", description = "批量删除光锥材料绑定信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialBind:remove')")
    @Log(title = "光锥材料绑定", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srLightconeMaterialBindService.deleteSrLightconeMaterialBindByIds(ids));
    }
}
