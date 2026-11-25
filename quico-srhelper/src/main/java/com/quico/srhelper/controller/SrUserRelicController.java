package com.quico.srhelper.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.core.page.TableDataInfo;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.SrUserRelic;
import com.quico.srhelper.domain.dto.SrUserRelicSaveDTO;
import com.quico.srhelper.service.ISrUserRelicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 用户遗器Controller
 * 
 * @author quico
 * @date 2026-11-25
 */
@Tag(name = "用户遗器Controller", description = "我的遗器相关接口")
@RestController
@RequestMapping("/srhelper/userRelic")
public class SrUserRelicController extends BaseController
{
    @Autowired
    private ISrUserRelicService srUserRelicService;

    /**
     * 查询当前用户的遗器列表
     */
    @Operation(summary = "查询遗器列表", description = "分页查询当前用户的遗器")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrUserRelic srUserRelic)
    {
        startPage();
        List<SrUserRelic> list = srUserRelicService.selectSrUserRelicList(srUserRelic);
        return getDataTable(list);
    }

    /**
     * 获取遗器详情
     */
    @Operation(summary = "获取遗器详情", description = "根据ID获取遗器及副词条明细")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srUserRelicService.selectSrUserRelicDetail(id));
    }

    /**
     * 新建或更新遗器
     */
    @Operation(summary = "保存遗器", description = "校验意图并重算派生字段后落库，id 为空表示新建")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:add') or @ss.hasPermi('srhelper:userRelic:edit')")
    @Log(title = "用户遗器", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody SrUserRelicSaveDTO dto)
    {
        return success(srUserRelicService.saveSrUserRelic(dto));
    }

    /**
     * 批量删除遗器
     */
    @Operation(summary = "删除遗器", description = "批量删除遗器，已锁定的不可删除")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:remove')")
    @Log(title = "用户遗器", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srUserRelicService.deleteSrUserRelicByIds(ids));
    }

    /**
     * 切换锁定状态
     */
    @Operation(summary = "锁定/解锁遗器", description = "切换遗器锁定状态")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:edit')")
    @Log(title = "用户遗器", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/lock")
    public AjaxResult lock(@PathVariable("id") Long id, @RequestBody SrUserRelic body)
    {
        return toAjax(srUserRelicService.updateSrUserRelicLock(id, body.getLocked()));
    }

    /**
     * 装备到角色或卸下
     */
    @Operation(summary = "装备/卸下遗器", description = "equippedBy 为空表示卸下")
    @PreAuthorize("@ss.hasPermi('srhelper:userRelic:edit')")
    @Log(title = "用户遗器", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/equip")
    public AjaxResult equip(@PathVariable("id") Long id, @RequestBody SrUserRelic body)
    {
        return toAjax(srUserRelicService.updateSrUserRelicEquip(id, body.getEquippedBy()));
    }
}
