package com.quico.srhelper.controller;

import java.util.Collections;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.dto.SrCharacterSaveDTO;
import com.quico.srhelper.service.ISrCharacterService;

/**
 * 角色晋升Controller
 * 
 * @author quico
 * @date 2026-06-02
 */
@Tag(name = "角色晋升Controller", description = "角色晋升相关接口")
@RestController
@RequestMapping("/srhelper/characterAscension")
public class SrCharacterAscensionController extends BaseController
{
    @Autowired
    private ISrCharacterService srCharacterService;

    /**
     * 获取角色详细信息（包含所有材料配置）
     */
    @Operation(summary = "获取角色详细信息", description = "根据ID获取角色详细信息，包含晋升材料、技能材料、额外能力、额外属性")
    @PreAuthorize("@ss.hasPermi('srhelper:srCharacter:query')")
    @GetMapping(value = "/character/{id}")
    public AjaxResult getCharacterDetail(@PathVariable("id") Long id)
    {
        return success(srCharacterService.getDetail(id));
    }

    /**
     * 保存角色（包含所有材料配置）
     */
    @Operation(summary = "保存角色", description = "新增或更新角色及其所有晋升材料配置（一次保存四张表）")
    @PreAuthorize("@ss.hasPermi('srhelper:srCharacter:add') or @ss.hasPermi('srhelper:srCharacter:edit')")
    @Log(title = "角色", businessType = BusinessType.INSERT)
    @PostMapping("/character")
    public AjaxResult saveCharacter(@RequestBody SrCharacterSaveDTO dto)
    {
        Long characterId = srCharacterService.saveAll(dto);
        return success(characterId);
    }

    /**
     * 批量保存角色（包含所有材料配置）
     * 支持单条和批量两种格式
     */
    @Operation(summary = "批量保存角色", description = "批量新增或更新角色及其所有晋升材料配置，支持单条和批量")
    @PreAuthorize("@ss.hasPermi('srhelper:srCharacter:add') or @ss.hasPermi('srhelper:srCharacter:edit')")
    @Log(title = "角色", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult saveBatch(@RequestBody Object body)
    {
        List<SrCharacterSaveDTO> dtoList;
        
        if (body instanceof List) {
            // 批量保存
            dtoList = (List<SrCharacterSaveDTO>) body;
        } else {
            // 单条保存（兼容前端发送单个对象的情况）
            SrCharacterSaveDTO dto = (SrCharacterSaveDTO) body;
            dtoList = Collections.singletonList(dto);
        }
        
        List<Long> characterIds = srCharacterService.saveBatch(dtoList);
        return success(characterIds);
    }

    /**
     * 修改角色（包含所有材料配置）
     */
    @Operation(summary = "修改角色", description = "更新角色及其所有晋升材料配置（一次更新四张表）")
    @PreAuthorize("@ss.hasPermi('srhelper:srCharacter:edit')")
    @Log(title = "角色", businessType = BusinessType.UPDATE)
    @PutMapping("/character/{id}")
    public AjaxResult updateCharacter(@PathVariable("id") Long id, @RequestBody SrCharacterSaveDTO dto)
    {
        dto.getCharacter().setId(id);
        Long characterId = srCharacterService.saveAll(dto);
        return success(characterId);
    }

    /**
     * 删除角色材料配置（不删除角色主表）
     */
    @Operation(summary = "删除角色材料配置", description = "删除角色的所有材料配置，但保留角色基本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:srCharacter:remove')")
    @Log(title = "角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/character/{id}")
    public AjaxResult deleteCharacter(@PathVariable("id") Long id)
    {
        srCharacterService.deleteCharacter(id);
        return success();
    }
}