package com.quico.srhelper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.core.page.TableDataInfo;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.dto.CelestialEncounterDTO;
import com.quico.srhelper.service.ISrLightConesService;

/**
 * 常驻光锥（星缘相遇）维护Controller
 *
 * 三个接口均把过滤条件下沉到 SQL：
 * - 已加入列表：standard_type = 2
 * - 可选光锥：standard_type = 0
 *
 * @author quico
 */
@Tag(name = "常驻光锥管理", description = "常驻/星缘相遇光锥维护相关接口")
@RestController
@RequestMapping("/srhelper/lightcone")
public class SrLightconeCelestialController extends BaseController
{
    @Autowired
    private ISrLightConesService srLightConesService;

    /**
     * 查询已加入星缘相遇的光锥列表
     * SQL 强制 standard_type = 2，仅返回 id、名称、图片、加入常驻池时间
     */
    @Operation(summary = "查询星缘相遇光锥列表", description = "仅查询 standard_type=2 的光锥，只返回id、名称、图片、加入常驻池时间")
    @PreAuthorize("@ss.hasPermi('srhelper:lightcones:list')")
    @GetMapping("/celestial/list")
    public TableDataInfo celestialList(@RequestParam(required = false) String lightConeName)
    {
        startPage();
        return getDataTable(srLightConesService.selectCelestialLightconeList(lightConeName));
    }

    /**
     * 查询可选限定光锥（“添加常驻光锥”弹窗下拉）
     * SQL 强制 standard_type = 0
     */
    @Operation(summary = "查询可选限定光锥", description = "仅查询 standard_type=0 的光锥，供添加常驻光锥下拉框使用")
    @PreAuthorize("@ss.hasPermi('srhelper:lightcones:list')")
    @GetMapping("/limited/options")
    public AjaxResult limitedOptions(@RequestParam(required = false) String lightConeName)
    {
        return success(srLightConesService.selectLimitedLightconeOptions(lightConeName));
    }

    /**
     * 批量加入星缘相遇
     * 版本联动：standard_type=2，standard_start_time 取所选版本的实装时间
     */
    @Operation(summary = "批量加入星缘相遇（光锥）", description = "按版本号取实装时间，将选中光锥设为星缘相遇（standard_type=2）并刷新缓存")
    @PreAuthorize("@ss.hasPermi('srhelper:lightcones:edit')")
    @Log(title = "光锥", businessType = BusinessType.UPDATE)
    @PostMapping("/celestial/add")
    public AjaxResult addToCelestialEncounter(@RequestBody CelestialEncounterDTO dto)
    {
        return success(srLightConesService.addToCelestialEncounter(dto.getIds(), dto.getVersionCode()));
    }
}
