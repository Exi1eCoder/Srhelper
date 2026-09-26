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
import java.util.HashMap;
import java.util.Map;

import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.dto.LightConeAscensionBatchSaveDTO;
import com.quico.srhelper.domain.vo.SrLightconeExpUpgradeVO;
import com.quico.srhelper.service.ISrLightConeAscensionService;
import com.quico.srhelper.service.ISrLightConesService;
import com.quico.srhelper.service.ISrLightconeExpUpgradeService;

/**
 * 光锥晋升材料Controller
 *
 * @author quico
 * @date 2026-05-30
 */
@Tag(name = "光锥晋升材料Controller", description = "光锥晋升材料相关接口")
@RestController
@RequestMapping("/srhelper/lightconeAscension")
public class SrLightConeAscensionController extends BaseController
{
    @Autowired
    private ISrLightConeAscensionService srLightConeAscensionService;

    @Autowired
    private ISrLightConesService srLightConesService;

    @Autowired
    private ISrLightconeExpUpgradeService expUpgradeService;

    /**
     * 查询光锥晋升材料列表
     */
    @Operation(summary = "查询光锥晋升材料列表", description = "分页查询光锥晋升材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrLightConeAscension srLightConeAscension)
    {
        startPage();
        List<SrLightConeAscension> list = srLightConeAscensionService.selectSrLightConeAscensionList(srLightConeAscension);
        return getDataTable(list);
    }

    /**
     * 获取光锥晋升材料详细信息
     */
    @Operation(summary = "获取光锥晋升材料详细信息", description = "根据ID获取光锥晋升材料详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srLightConeAscensionService.selectSrLightConeAscensionById(id));
    }

    /**
     * 根据光锥ID查询所有晋升材料
     */
    @Operation(summary = "查询指定光锥的所有晋升材料", description = "根据光锥ID查询对应的所有晋升材料")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:query')")
    @GetMapping("/cone/{lightConeId}")
    public AjaxResult getByConeId(@PathVariable("lightConeId") Long lightConeId)
    {
        List<SrLightConeAscension> list = srLightConeAscensionService.selectSrLightConeAscensionByConeId(lightConeId);
        // 附带光锥升级经验
        SrLightCones lightCone = srLightConesService.selectSrLightConesById(lightConeId);
        List<SrLightconeExpUpgradeVO> expUpgrades = null;
        if (lightCone != null && lightCone.getStarLevel() != null) {
            expUpgrades = expUpgradeService.selectExpUpgradesByStarLevel(lightCone.getStarLevel().intValue());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ascensions", list);
        result.put("expUpgrades", expUpgrades);
        result.put("starLevel", lightCone != null ? lightCone.getStarLevel() : null);
        result.put("path", lightCone != null ? lightCone.getPath() : null);
        return success(result);
    }

    /**
     * 新增光锥晋升材料
     */
    @Operation(summary = "新增光锥晋升材料", description = "新增光锥晋升材料")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:add')")
    @Log(title = "光锥晋升材料", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrLightConeAscension srLightConeAscension)
    {
        return toAjax(srLightConeAscensionService.insertSrLightConeAscension(srLightConeAscension));
    }

    /**
     * 修改光锥晋升材料
     */
    @Operation(summary = "修改光锥晋升材料", description = "更新光锥晋升材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:edit')")
    @Log(title = "光锥晋升材料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrLightConeAscension srLightConeAscension)
    {
        return toAjax(srLightConeAscensionService.updateSrLightConeAscension(srLightConeAscension));
    }

    /**
     * 删除光锥晋升材料
     */
    @Operation(summary = "删除光锥晋升材料", description = "批量删除光锥晋升材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:remove')")
    @Log(title = "光锥晋升材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srLightConeAscensionService.deleteSrLightConeAscensionByIds(ids));
    }

    /**
     * 批量保存光锥晋升材料（先删后增）
     */
    @Operation(summary = "批量保存光锥晋升材料", description = "先清空指定光锥的所有晋升材料，再批量插入")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:add')")
    @Log(title = "光锥晋升材料", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchSave(@RequestBody LightConeAscensionBatchSaveDTO dto)
    {
        List<Long> insertedIds = srLightConeAscensionService.batchSaveLightConeAscension(dto);
        return success(insertedIds);
    }

    /**
     * 根据光锥ID删除所有晋升材料
     */
    @Operation(summary = "根据光锥ID删除所有晋升材料", description = "删除指定光锥的全部晋升材料")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:remove')")
    @Log(title = "光锥晋升材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/cone/{lightConeId}")
    public AjaxResult removeByConeId(@PathVariable Long lightConeId)
    {
        srLightConeAscensionService.deleteSrLightConeAscensionByConeId(lightConeId);
        return success();
    }
}
