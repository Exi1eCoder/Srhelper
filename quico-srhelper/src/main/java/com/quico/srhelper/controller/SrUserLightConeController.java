package com.quico.srhelper.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.domain.vo.SrLightConeCultivationVO;
import com.quico.srhelper.service.ISrUserLightConeService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.quico.srhelper.service.ISrLightConeCultivationService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 用户持有光锥Controller
 * 
 * @author quico
 * @date 2026-06-07
 */
@Tag(name = "用户持有光锥Controller", description = "用户持有光锥相关接口")
@RestController
@RequestMapping("/srhelper/userLightCone")
public class SrUserLightConeController extends BaseController
{
    @Autowired
    private ISrUserLightConeService srUserLightConeService;

    @Autowired
    private ISrLightConeCultivationService lightConeCultivationService;

    /**
     * 查询用户持有光锥列表
     */
    @PreAuthorize("@ss.hasPermi('srhelper:userLightCone:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrUserLightCone srUserLightCone)
    {
        // startPage();
        List<SrUserLightCone> list = srUserLightConeService.selectSrUserLightConeList(srUserLightCone);
        return getDataTable(list);
    }

    /**
     * 获取当前登录用户的持有光锥列表（包含光锥详情）
     */
    @GetMapping("/user")
    public AjaxResult getCurrentUserLightCones(
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer starLevel,
            @RequestParam(required = false) String lightConeName)
    {
        Long userId = getUserId();
        List<SrUserLightCone> list = srUserLightConeService.selectByUserIdWithDetails(userId, path, starLevel, lightConeName);
        return success(list);
    }

    /**
     * 根据用户ID查询持有光锥列表（包含光锥详情）
     */
    @GetMapping("/user/{userId}")
    public AjaxResult getByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer starLevel,
            @RequestParam(required = false) String lightConeName)
    {
        if (userId == null || userId <= 0)
        {
            return AjaxResult.error("用户ID参数无效");
        }
        List<SrUserLightCone> list = srUserLightConeService.selectByUserIdWithDetails(userId, path, starLevel, lightConeName);
        return success(list);
    }

    /**
     * 导出用户持有光锥列表
     */
    @PreAuthorize("@ss.hasPermi('srhelper:userLightCone:export')")
    @Log(title = "用户持有光锥", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrUserLightCone srUserLightCone)
    {
        List<SrUserLightCone> list = srUserLightConeService.selectSrUserLightConeList(srUserLightCone);
        ExcelUtil<SrUserLightCone> util = new ExcelUtil<SrUserLightCone>(SrUserLightCone.class);
        util.exportExcel(response, list, "用户持有光锥数据");
    }

    /**
     * 获取用户持有光锥详细信息（包含光锥名称、图片、星级、命途）
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        SrUserLightCone result = srUserLightConeService.selectByIdWithDetails(id);
        return result != null ? success(result) : error("光锥不存在");
    }

    /**
     * 新增用户持有光锥
     */
    @PreAuthorize("@ss.hasPermi('srhelper:userLightCone:add')")
    @Log(title = "用户持有光锥", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrUserLightCone srUserLightCone)
    {
        return toAjax(srUserLightConeService.insertSrUserLightCone(srUserLightCone));
    }

    /**
     * 修改用户持有光锥
     */
    @PreAuthorize("@ss.hasPermi('srhelper:userLightCone:edit')")
    @Log(title = "用户持有光锥", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrUserLightCone srUserLightCone)
    {
        return toAjax(srUserLightConeService.updateSrUserLightCone(srUserLightCone));
    }

    /**
     * 删除用户持有光锥
     */
    @PreAuthorize("@ss.hasPermi('srhelper:userLightCone:remove')")
    @Log(title = "用户持有光锥", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srUserLightConeService.deleteSrUserLightConeByIds(ids));
    }

    /**
     * 计算单个光锥的养成材料需求
     */
    @GetMapping("/cultivation/{userLightConeId}")
    public AjaxResult calculateCultivation(@PathVariable("userLightConeId") Long userLightConeId)
    {
        SrLightConeCultivationVO result = lightConeCultivationService.calculateLightConeCultivation(userLightConeId, MAX_LEVEL);
        return success(result);
    }

    /**
     * 计算用户所有光锥的总体养成材料需求
     */
    @GetMapping("/cultivation/total")
    public AjaxResult calculateTotalCultivation()
    {
        Long userId = getUserId();
        SrLightConeCultivationVO result = lightConeCultivationService.calculateTotalLightConeCultivation(userId, MAX_LEVEL);
        return success(result);
    }

    private static final Integer MAX_LEVEL = 80;
}