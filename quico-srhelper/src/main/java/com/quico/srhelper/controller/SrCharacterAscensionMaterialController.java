package com.quico.srhelper.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.quico.common.core.page.TableDataInfo;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.SrCharacterAscensionMaterial;
import com.quico.srhelper.service.ISrCharacterAscensionMaterialService;

@Tag(name = "角色晋升材料配置Controller")
@RestController
@RequestMapping("/srhelper/characterAscensionMaterial")
public class SrCharacterAscensionMaterialController extends BaseController
{
    @Autowired
    private ISrCharacterAscensionMaterialService service;

    @Operation(summary = "查询晋升材料配置列表")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrCharacterAscensionMaterial material)
    {
        startPage();
        List<SrCharacterAscensionMaterial> list = service.selectList(material);
        return getDataTable(list);
    }

    @Operation(summary = "查询指定角色的晋升材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:query')")
    @GetMapping("/character/{characterId}")
    public AjaxResult getByCharacterId(@PathVariable Long characterId)
    {
        return success(service.selectByCharacterId(characterId));
    }

    @Operation(summary = "获取晋升材料详情")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(service.selectById(id));
    }

    @Operation(summary = "新增晋升材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:add')")
    @Log(title = "角色晋升材料", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacterAscensionMaterial material)
    {
        return toAjax(service.insert(material));
    }

    @Operation(summary = "修改晋升材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:edit')")
    @Log(title = "角色晋升材料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacterAscensionMaterial material)
    {
        return toAjax(service.update(material));
    }

    @Operation(summary = "删除晋升材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:remove')")
    @Log(title = "角色晋升材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(service.deleteByIds(ids));
    }

    @Operation(summary = "根据角色ID删除所有晋升材料")
    @PreAuthorize("@ss.hasPermi('srhelper:characterAscension:remove')")
    @Log(title = "角色晋升材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/character/{characterId}")
    public AjaxResult removeByCharacterId(@PathVariable Long characterId)
    {
        service.deleteByCharacterId(characterId);
        return success();
    }
}
