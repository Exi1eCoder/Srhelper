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
import com.quico.srhelper.domain.SrCharacterSkillMaterial;
import com.quico.srhelper.service.ISrCharacterSkillMaterialService;

/**
 * 角色技能材料配置Controller
 *
 * @author quico
 * @date 2026-05-30
 */
@Tag(name = "角色技能材料配置Controller")
@RestController
@RequestMapping("/srhelper/characterSkillMaterial")
public class SrCharacterSkillMaterialController extends BaseController
{
    @Autowired
    private ISrCharacterSkillMaterialService service;

    @Operation(summary = "查询技能材料配置列表")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrCharacterSkillMaterial material)
    {
        startPage();
        List<SrCharacterSkillMaterial> list = service.selectList(material);
        return getDataTable(list);
    }

    @Operation(summary = "查询指定角色的技能材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:query')")
    @GetMapping("/character/{characterId}")
    public AjaxResult getByCharacterId(@PathVariable Long characterId)
    {
        return success(service.selectByCharacterId(characterId));
    }

    @Operation(summary = "获取技能材料详情")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(service.selectById(id));
    }

    @Operation(summary = "新增技能材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:add')")
    @Log(title = "角色技能材料", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacterSkillMaterial material)
    {
        return toAjax(service.insert(material));
    }

    @Operation(summary = "修改技能材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:edit')")
    @Log(title = "角色技能材料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacterSkillMaterial material)
    {
        return toAjax(service.update(material));
    }

    @Operation(summary = "删除技能材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:remove')")
    @Log(title = "角色技能材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(service.deleteByIds(ids));
    }

    @Operation(summary = "根据角色ID删除所有技能材料")
    @PreAuthorize("@ss.hasPermi('srhelper:characterSkill:remove')")
    @Log(title = "角色技能材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/character/{characterId}")
    public AjaxResult removeByCharacterId(@PathVariable Long characterId)
    {
        service.deleteByCharacterId(characterId);
        return success();
    }
}
