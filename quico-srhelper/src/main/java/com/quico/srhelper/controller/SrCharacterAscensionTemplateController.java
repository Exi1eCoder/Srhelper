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
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;
import com.quico.srhelper.service.ISrCharacterAscensionTemplateService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 角色晋升材料模板Controller
 * 
 * @author quico
 * @date 2026-06-15
 */
@Tag(name = "角色晋升材料模板Controller", description = "角色晋升材料模板相关接口")
@RestController
@RequestMapping("/srhelper/characterTemplate")
public class SrCharacterAscensionTemplateController extends BaseController
{
    @Autowired
    private ISrCharacterAscensionTemplateService srCharacterAscensionTemplateService;

    /**
     * 查询角色晋升材料模板列表
     */
    @Operation(summary = "查询角色晋升材料模板列表", description = "分页查询角色晋升材料模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        startPage();
        List<SrCharacterAscensionTemplate> list = srCharacterAscensionTemplateService.selectSrCharacterAscensionTemplateList(srCharacterAscensionTemplate);
        return getDataTable(list);
    }

    /**
     * 导出角色晋升材料模板列表
     */
    @Operation(summary = "导出角色晋升材料模板列表", description = "导出角色晋升材料模板列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:export')")
    @Log(title = "角色晋升材料模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        List<SrCharacterAscensionTemplate> list = srCharacterAscensionTemplateService.selectSrCharacterAscensionTemplateList(srCharacterAscensionTemplate);
        ExcelUtil<SrCharacterAscensionTemplate> util = new ExcelUtil<SrCharacterAscensionTemplate>(SrCharacterAscensionTemplate.class);
        util.exportExcel(response, list, "角色晋升材料模板数据");
    }

    /**
     * 获取角色晋升材料模板详细信息
     */
    @Operation(summary = "获取角色晋升材料模板详细信息", description = "根据ID获取角色晋升材料模板详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srCharacterAscensionTemplateService.selectSrCharacterAscensionTemplateById(id));
    }

    /**
     * 新增角色晋升材料模板
     */
    @Operation(summary = "新增角色晋升材料模板", description = "新增角色晋升材料模板")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:add')")
    @Log(title = "角色晋升材料模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        return toAjax(srCharacterAscensionTemplateService.insertSrCharacterAscensionTemplate(srCharacterAscensionTemplate));
    }

    /**
     * 修改角色晋升材料模板
     */
    @Operation(summary = "修改角色晋升材料模板", description = "更新角色晋升材料模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:edit')")
    @Log(title = "角色晋升材料模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        return toAjax(srCharacterAscensionTemplateService.updateSrCharacterAscensionTemplate(srCharacterAscensionTemplate));
    }

    /**
     * 删除角色晋升材料模板
     */
    @Operation(summary = "删除角色晋升材料模板", description = "批量删除角色晋升材料模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:remove')")
    @Log(title = "角色晋升材料模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srCharacterAscensionTemplateService.deleteSrCharacterAscensionTemplateByIds(ids));
    }

    /**
     * 根据模板类型获取完整模板数据（用于创建页回显到各页签）
     */
    @Operation(summary = "根据模板类型获取模板数据", description = "获取指定templateType下的完整模板，用于前端表单回显")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:query')")
    @GetMapping("/type/{templateType}")
    public AjaxResult getByTemplateType(@PathVariable String templateType)
    {
        List<SrCharacterAscensionTemplate> list = srCharacterAscensionTemplateService.selectByTemplateType(templateType);
        return success(list);
    }

    /**
     * 批量新增/覆盖角色晋升材料模板（创建晋升方案时使用）
     * 同一 templateType 视为同一模板：已有则先删后插，无有则直接插入
     */
    @Operation(summary = "批量新增角色晋升材料模板（覆盖式）", description = "接收JSON数组，同一templateType已有则覆盖")
    @PreAuthorize("@ss.hasPermi('srhelper:characterTemplate:add')")
    @Log(title = "角色晋升材料模板（批量覆盖）", businessType = BusinessType.INSERT)
    @PostMapping("/createWithAscension")
    public AjaxResult createWithAscension(@RequestBody List<SrCharacterAscensionTemplate> templates)
    {
        String templateType = templates.isEmpty() ? "" : templates.get(0).getTemplateType();
        int rows = srCharacterAscensionTemplateService.replaceByTemplateType(templates);
        return success("模板 [" + templateType + "] 更新完成，共 " + rows + " 条数据");
    }
}
