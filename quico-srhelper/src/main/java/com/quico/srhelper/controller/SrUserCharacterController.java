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
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.service.ISrUserCharacterService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 我的角色Controller
 * 
 * @author quico
 * @date 2026-05-31
 */
@Tag(name = "我的角色Controller", description = "我的角色相关接口")
@RestController
@RequestMapping("/srhelper/userCharacter")
public class SrUserCharacterController extends BaseController
{
    @Autowired
    private ISrUserCharacterService srUserCharacterService;

    /**
     * 查询我的角色列表
     */
    @Operation(summary = "查询我的角色列表", description = "分页查询我的角色信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrUserCharacter srUserCharacter)
    {
        // startPage();
        List<SrUserCharacter> list = srUserCharacterService.selectSrUserCharacterList(srUserCharacter);
        return getDataTable(list);
    }

    /**
     * 导出我的角色列表
     */
    @Operation(summary = "导出我的角色列表", description = "导出我的角色列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:export')")
    @Log(title = "我的角色", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrUserCharacter srUserCharacter)
    {
        List<SrUserCharacter> list = srUserCharacterService.selectSrUserCharacterList(srUserCharacter);
        ExcelUtil<SrUserCharacter> util = new ExcelUtil<SrUserCharacter>(SrUserCharacter.class);
        util.exportExcel(response, list, "我的角色数据");
    }

    /**
     * 获取我的角色详细信息
     */
    @Operation(summary = "获取我的角色详细信息", description = "根据ID获取我的角色详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srUserCharacterService.selectSrUserCharacterById(id));
    }

    /**
     * 新增我的角色
     */
    @Operation(summary = "新增我的角色", description = "新增我的角色")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:add')")
    @Log(title = "我的角色", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrUserCharacter srUserCharacter)
    {
        return toAjax(srUserCharacterService.insertSrUserCharacter(srUserCharacter));
    }

    /**
     * 修改我的角色
     */
    @Operation(summary = "修改我的角色", description = "更新我的角色信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:edit')")
    @Log(title = "我的角色", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrUserCharacter srUserCharacter)
    {
        return toAjax(srUserCharacterService.updateSrUserCharacter(srUserCharacter));
    }

    /**
     * 删除我的角色
     */
    @Operation(summary = "删除我的角色", description = "批量删除我的角色信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userCharacter:remove')")
    @Log(title = "我的角色", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srUserCharacterService.deleteSrUserCharacterByIds(ids));
    }
}
