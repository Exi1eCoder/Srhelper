package com.quico.infra.controller;

import java.util.List;
import com.quico.infra.domain.vo.SrMaterialVO;
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
import com.quico.infra.domain.SrMaterial;
import com.quico.infra.service.ISrMaterialService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 素材库Controller
 * 
 * @author quico
 * @date 2026-05-23
 */
@Tag(name = "素材库Controller", description = "素材库相关接口")
@RestController
@RequestMapping("/infra/material")
public class SrMaterialController extends BaseController
{
    @Autowired
    private ISrMaterialService srMaterialService;

    /**
     * 查询素材库列表
     */
    @Operation(summary = "查询素材库列表", description = "分页查询素材库信息")
    @PreAuthorize("@ss.hasPermi('infra:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrMaterial srMaterial)
    {
        startPage();
        List<SrMaterial> list = srMaterialService.selectSrMaterialList(srMaterial);
        List<SrMaterialVO> voList = srMaterialService.assembleSrMaterialVOList(list);
        TableDataInfo dataInfo = getDataTable(list);
        dataInfo.setRows(voList);
        return dataInfo;
    }

    /**
     * 导出素材库列表
     */
    @Operation(summary = "导出素材库列表", description = "导出素材库列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('infra:material:export')")
    @Log(title = "素材库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrMaterial srMaterial)
    {
        List<SrMaterial> list = srMaterialService.selectSrMaterialList(srMaterial);
        ExcelUtil<SrMaterial> util = new ExcelUtil<SrMaterial>(SrMaterial.class);
        util.exportExcel(response, list, "素材库数据");
    }

    /**
     * 获取素材库详细信息
     */
    @Operation(summary = "获取素材库详细信息", description = "根据ID获取素材库详细信息")
    @PreAuthorize("@ss.hasPermi('infra:material:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srMaterialService.selectSrMaterialById(id));
    }

    /**
     * 新增素材库
     */
    @Operation(summary = "新增素材库", description = "新增素材库")
    @PreAuthorize("@ss.hasPermi('infra:material:add')")
    @Log(title = "素材库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrMaterial srMaterial)
    {
        return toAjax(srMaterialService.insertSrMaterial(srMaterial));
    }

    /**
     * 修改素材库
     */
    @Operation(summary = "修改素材库", description = "更新素材库信息")
    @PreAuthorize("@ss.hasPermi('infra:material:edit')")
    @Log(title = "素材库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrMaterial srMaterial)
    {
        return toAjax(srMaterialService.updateSrMaterial(srMaterial));
    }

    /**
     * 删除素材库
     */
    @Operation(summary = "删除素材库", description = "批量删除素材库信息")
    @PreAuthorize("@ss.hasPermi('infra:material:remove')")
    @Log(title = "素材库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srMaterialService.deleteSrMaterialByIds(ids));
    }
}
