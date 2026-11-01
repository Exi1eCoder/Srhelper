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
import com.quico.srhelper.service.ISrCharacterService;

/**
 * 星缘相遇角色维护Controller
 *
 * 三个接口均把过滤条件下沉到 SQL：
 * - 已加入列表：standard_type = 2
 * - 可选角色：standard_type = 0
 *
 * @author quico
 */
@Tag(name = "星缘相遇管理", description = "星缘相遇角色维护相关接口")
@RestController
@RequestMapping("/srhelper/character")
public class SrCharacterCelestialController extends BaseController
{
    @Autowired
    private ISrCharacterService srCharacterService;

    /**
     * 查询已加入星缘相遇的角色列表
     * SQL 强制 standard_type = 2，仅返回 id、名字、头像、加入常驻池时间
     */
    @Operation(summary = "查询星缘相遇角色列表", description = "仅查询 standard_type=2 的角色，只返回id、名字、头像、加入常驻池时间")
    @PreAuthorize("@ss.hasPermi('srhelper:character:list')")
    @GetMapping("/celestial/list")
    public TableDataInfo celestialList(@RequestParam(required = false) String characterName)
    {
        startPage();
        return getDataTable(srCharacterService.selectCelestialCharacterList(characterName));
    }

    /**
     * 查询可选限定角色（“添加星缘相遇角色”弹窗下拉）
     * SQL 强制 standard_type = 0
     */
    @Operation(summary = "查询可选限定角色", description = "仅查询 standard_type=0 的角色，供添加星缘相遇角色下拉框使用")
    @PreAuthorize("@ss.hasPermi('srhelper:character:list')")
    @GetMapping("/limited/options")
    public AjaxResult limitedOptions(@RequestParam(required = false) String characterName)
    {
        return success(srCharacterService.selectLimitedCharacterOptions(characterName));
    }

    /**
     * 批量加入星缘相遇
     * 版本联动：standard_type=2，standard_start_time 取所选版本的实装时间
     */
    @Operation(summary = "批量加入星缘相遇", description = "按版本号取实装时间，将选中角色设为星缘相遇（standard_type=2）并刷新缓存")
    @PreAuthorize("@ss.hasPermi('srhelper:character:edit')")
    @Log(title = "角色", businessType = BusinessType.UPDATE)
    @PostMapping("/celestial/add")
    public AjaxResult addToCelestialEncounter(@RequestBody CelestialEncounterDTO dto)
    {
        return success(srCharacterService.addToCelestialEncounter(dto.getIds(), dto.getVersionCode()));
    }
}
