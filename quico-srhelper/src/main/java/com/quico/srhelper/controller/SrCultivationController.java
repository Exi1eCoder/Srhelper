package com.quico.srhelper.controller;

import com.quico.common.core.domain.AjaxResult;
import com.quico.srhelper.domain.vo.SrCharacterCultivationVO;
import com.quico.srhelper.domain.vo.SrCultivationCalcVO;
import com.quico.srhelper.service.ISrCultivationService;
import com.quico.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 养成计算控制器
 */
@RestController
@RequestMapping("/srhelper/cultivation")
@Tag(name = "养成计算", description = "角色养成材料计算")
@RequiredArgsConstructor
public class SrCultivationController {
    
    private final ISrCultivationService cultivationService;
    
    /**
     * 计算总体养成材料
     */
    @Operation(summary = "计算总体养成材料", description = "计算用户所有角色的总体养成材料需求")
    @GetMapping("/total")
    public AjaxResult calculateTotal(
            @Parameter(description = "目标等级，默认80") @RequestParam(required = false) Integer targetLevel) {
        Long userId = SecurityUtils.getUserId();
        SrCultivationCalcVO result = cultivationService.calculateTotalCultivation(userId, targetLevel);
        return AjaxResult.success(result);
    }
    
    /**
     * 计算单个角色养成材料
     */
    @Operation(summary = "计算单个角色养成材料", description = "计算指定角色的养成材料需求")
    @GetMapping("/character/{userCharacterId}")
    public AjaxResult calculateCharacter(
            @Parameter(description = "用户角色ID") @PathVariable Long userCharacterId,
            @Parameter(description = "目标等级，默认80") @RequestParam(required = false) Integer targetLevel) {
        SrCharacterCultivationVO result = cultivationService.calculateCharacterCultivation(userCharacterId, targetLevel);
        return result != null ? AjaxResult.success(result) : AjaxResult.error("角色不存在");
    }
}