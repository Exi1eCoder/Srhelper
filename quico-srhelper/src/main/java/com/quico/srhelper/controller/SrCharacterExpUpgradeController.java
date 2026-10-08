package com.quico.srhelper.controller;

import com.quico.common.core.domain.AjaxResult;
import com.quico.srhelper.domain.dto.SrCharacterExpUpgradeDTO;
import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色经验升级控制器
 * Redis 缓存逻辑在 ServiceImpl 中统一处理，本层只做参数接收与结果包装
 */
@RestController
@RequestMapping("/srhelper/expUpgrade")
@Tag(name = "角色经验升级", description = "角色经验升级配置管理")
@RequiredArgsConstructor
public class SrCharacterExpUpgradeController {

    private final ISrCharacterExpUpgradeService expUpgradeService;

    /**
     * 查询所有升级配置
     */
    @Operation(summary = "查询所有升级配置", description = "获取所有角色升级所需的经验和信用点配置")
    @GetMapping("/list")
    public AjaxResult list() {
        List<SrCharacterExpUpgradeVO> list = expUpgradeService.selectAllExpUpgrades();
        return AjaxResult.success(list);
    }

    /**
     * 根据等级查询配置
     */
    @Operation(summary = "根据等级查询配置", description = "获取指定等级对应的升级配置")
    @GetMapping("/level/{level}")
    public AjaxResult getByLevel(@Parameter(description = "角色等级") @PathVariable Integer level) {
        SrCharacterExpUpgradeVO vo = expUpgradeService.selectExpUpgradeByLevel(level);
        return vo != null ? AjaxResult.success(vo) : AjaxResult.error("未找到对应等级的配置");
    }

    /**
     * 获取单个配置详情
     */
    @Operation(summary = "获取配置详情", description = "根据ID获取升级配置详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@Parameter(description = "配置ID") @PathVariable Long id) {
        SrCharacterExpUpgradeVO vo = expUpgradeService.selectExpUpgradeById(id);
        return vo != null ? AjaxResult.success(vo) : AjaxResult.error("配置不存在");
    }

    /**
     * 新增配置
     */
    @Operation(summary = "新增配置", description = "新增角色升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:expUpgrade:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacterExpUpgradeDTO dto) {
        int rows = expUpgradeService.insertExpUpgrade(dto);
        return rows > 0 ? AjaxResult.success("新增成功") : AjaxResult.error("新增失败");
    }

    /**
     * 修改配置
     */
    @Operation(summary = "修改配置", description = "修改角色升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:expUpgrade:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacterExpUpgradeDTO dto) {
        int rows = expUpgradeService.updateExpUpgrade(dto);
        return rows > 0 ? AjaxResult.success("修改成功") : AjaxResult.error("修改失败");
    }

    /**
     * 删除配置
     */
    @Operation(summary = "删除配置", description = "删除角色升级配置")
    @PreAuthorize("@ss.hasPermi('srhelper:expUpgrade:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@Parameter(description = "配置ID") @PathVariable Long id) {
        int rows = expUpgradeService.deleteExpUpgradeById(id);
        return rows > 0 ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }
}
