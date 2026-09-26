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
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;
import com.quico.srhelper.service.ISrLightconeAscensionTemplateService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 光锥晋升素材模板Controller
 * 
 * @author quico
 * @date 2026-06-20
 */
@Tag(name = "光锥晋升素材模板Controller", description = "光锥晋升素材模板相关接口")
@RestController
@RequestMapping("/srhelper/lightconeMaterialTemplate")
public class SrLightconeAscensionTemplateController extends BaseController
{
    @Autowired
    private ISrLightconeAscensionTemplateService srLightconeAscensionTemplateService;

    /**
     * 查询光锥晋升素材模板列表
     */
    @Operation(summary = "查询光锥晋升素材模板列表", description = "分页查询光锥晋升素材模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        startPage();
        List<SrLightconeAscensionTemplate> list = srLightconeAscensionTemplateService.selectSrLightconeAscensionTemplateList(srLightconeAscensionTemplate);
        return getDataTable(list);
    }

    /**
     * 导出光锥晋升素材模板列表
     */
    @Operation(summary = "导出光锥晋升素材模板列表", description = "导出光锥晋升素材模板列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:export')")
    @Log(title = "光锥晋升素材模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        List<SrLightconeAscensionTemplate> list = srLightconeAscensionTemplateService.selectSrLightconeAscensionTemplateList(srLightconeAscensionTemplate);
        ExcelUtil<SrLightconeAscensionTemplate> util = new ExcelUtil<SrLightconeAscensionTemplate>(SrLightconeAscensionTemplate.class);
        util.exportExcel(response, list, "光锥晋升素材模板数据");
    }

    /**
     * 获取光锥晋升素材模板详细信息
     */
    @Operation(summary = "获取光锥晋升素材模板详细信息", description = "根据ID获取光锥晋升素材模板详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srLightconeAscensionTemplateService.selectSrLightconeAscensionTemplateById(id));
    }

    /**
     * 新增光锥晋升素材模板
     */
    @Operation(summary = "新增光锥晋升素材模板", description = "新增光锥晋升素材模板")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:add')")
    @Log(title = "光锥晋升素材模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        return toAjax(srLightconeAscensionTemplateService.insertSrLightconeAscensionTemplate(srLightconeAscensionTemplate));
    }

    /**
     * 修改光锥晋升素材模板
     */
    @Operation(summary = "修改光锥晋升素材模板", description = "更新光锥晋升素材模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:edit')")
    @Log(title = "光锥晋升素材模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        return toAjax(srLightconeAscensionTemplateService.updateSrLightconeAscensionTemplate(srLightconeAscensionTemplate));
    }

    /**
     * 删除光锥晋升素材模板
     */
    @Operation(summary = "删除光锥晋升素材模板", description = "批量删除光锥晋升素材模板信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:remove')")
    @Log(title = "光锥晋升素材模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srLightconeAscensionTemplateService.deleteSrLightconeAscensionTemplateByIds(ids));
    }

    /**
     * 批量新增/覆盖光锥晋升素材模板
     * 同一 templateType 视为同一模板：已有则先删后插
     */
    @Operation(summary = "批量新增光锥晋升素材模板（覆盖式）", description = "接收JSON数组，同一templateType已有则覆盖")
    @PreAuthorize("@ss.hasPermi('srhelper:lightconeMaterialTemplate:add')")
    @Log(title = "光锥晋升素材模板（批量覆盖）", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchSave(@RequestBody List<SrLightconeAscensionTemplate> templates)
    {
        String templateType = templates.isEmpty() ? "" : templates.get(0).getTemplateType();
        int rows = srLightconeAscensionTemplateService.replaceByTemplateType(templates);
        return success("模板 [" + templateType + "] 更新完成，共 " + rows + " 条数据");
    }
}
