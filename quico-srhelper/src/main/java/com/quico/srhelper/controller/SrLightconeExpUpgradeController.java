package com.quico.srhelper.controller;

import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.srhelper.domain.dto.SrLightconeExpUpgradeDTO;
import com.quico.srhelper.service.ISrLightconeExpUpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/srhelper/lcExpUpgrade")
@Tag(name = "光锥经验升级", description = "光锥经验升级配置管理")
@RequiredArgsConstructor
public class SrLightconeExpUpgradeController extends BaseController {

    private final ISrLightconeExpUpgradeService expUpgradeService;

    /**
     * 查询升级配置（核心：传starLevel按星级过滤，不传则查全量）
     */
    @Operation(summary = "查询升级配置", description = "传starLevel则按星级过滤，同时附赠该星级晋升模板数据")
    @GetMapping("/list")
    public AjaxResult list(@Parameter(description = "星级（3/4/5），可选") @RequestParam(required = false) Integer starLevel) {
        if (starLevel != null) {
            return success(expUpgradeService.selectExpFullByStarLevel(starLevel));
        }
        return success(expUpgradeService.selectAllExpUpgrades());
    }

    /**
     * 根据星级和等级查询配置
     */
    @Operation(summary = "根据星级和等级查询配置", description = "获取指定星级和等级对应的升级配置")
    @GetMapping("/level/{starLevel}/{level}")
    public AjaxResult getByStarAndLevel(
            @Parameter(description = "星级（3/4/5）") @PathVariable Integer starLevel,
            @Parameter(description = "光锥等级") @PathVariable Integer level) {
        return success(expUpgradeService.selectExpUpgradeByStarAndLevel(starLevel, level));
    }

    @Operation(summary = "获取配置详情", description = "根据ID获取升级配置详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@Parameter(description = "配置ID") @PathVariable Long id) {
        return success(expUpgradeService.selectExpUpgradeById(id));
    }

    /**
     * 新增配置（并清除缓存）
     */
    @Operation(summary = "新增配置", description = "新增光锥升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:lcExpUpgrade:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SrLightconeExpUpgradeDTO dto) {
        return toAjax(expUpgradeService.insertExpUpgrade(dto));
    }

    /**
     * 修改配置（并清除缓存）
     */
    @Operation(summary = "修改配置", description = "修改光锥升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:lcExpUpgrade:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SrLightconeExpUpgradeDTO dto) {
        return toAjax(expUpgradeService.updateExpUpgrade(dto));
    }

    /**
     * 删除配置（并清除缓存）
     */
    @Operation(summary = "删除配置", description = "删除光锥升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:lcExpUpgrade:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@Parameter(description = "配置ID") @PathVariable Long id) {
        return toAjax(expUpgradeService.deleteExpUpgradeById(id));
    }
}
