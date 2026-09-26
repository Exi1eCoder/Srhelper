package com.quico.infra.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.SecurityUtils;
import com.quico.infra.domain.QuicoUserSignIn;
import com.quico.infra.service.IQuicoUserSignInService;
import com.quico.infra.service.ISignInService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 用户签到记录Controller
 * 
 * @author quico
 * @date 2026-08-03
 */
@Tag(name = "用户签到记录Controller", description = "用户签到记录相关接口")
@RestController
@RequestMapping("/infra/signInCenter")
public class QuicoUserSignInController extends BaseController
{
    @Autowired
    private IQuicoUserSignInService quicoUserSignInService;

    @Autowired
    private ISignInService signInService;

    /**
     * 查询用户签到记录列表
     */
    @Operation(summary = "查询用户签到记录列表", description = "分页查询用户签到记录信息")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:list')")
    @GetMapping("/list")
    public TableDataInfo list(QuicoUserSignIn quicoUserSignIn)
    {
        startPage();
        List<QuicoUserSignIn> list = quicoUserSignInService.selectQuicoUserSignInList(quicoUserSignIn);
        return getDataTable(list);
    }

    /**
     * 导出用户签到记录列表
     */
    @Operation(summary = "导出用户签到记录列表", description = "导出用户签到记录列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:export')")
    @Log(title = "用户签到记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QuicoUserSignIn quicoUserSignIn)
    {
        List<QuicoUserSignIn> list = quicoUserSignInService.selectQuicoUserSignInList(quicoUserSignIn);
        ExcelUtil<QuicoUserSignIn> util = new ExcelUtil<QuicoUserSignIn>(QuicoUserSignIn.class);
        util.exportExcel(response, list, "用户签到记录数据");
    }

    /**
     * 获取用户签到记录详细信息
     */
    @Operation(summary = "获取用户签到记录详细信息", description = "根据ID获取用户签到记录详细信息")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(quicoUserSignInService.selectQuicoUserSignInById(id));
    }

    /**
     * 新增用户签到记录
     */
    @Operation(summary = "新增用户签到记录", description = "新增用户签到记录")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:add')")
    @Log(title = "用户签到记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QuicoUserSignIn quicoUserSignIn)
    {
        return toAjax(quicoUserSignInService.insertQuicoUserSignIn(quicoUserSignIn));
    }

    /**
     * 修改用户签到记录
     */
    @Operation(summary = "修改用户签到记录", description = "更新用户签到记录信息")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:edit')")
    @Log(title = "用户签到记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QuicoUserSignIn quicoUserSignIn)
    {
        return toAjax(quicoUserSignInService.updateQuicoUserSignIn(quicoUserSignIn));
    }

    /**
     * 删除用户签到记录
     */
    @Operation(summary = "删除用户签到记录", description = "批量删除用户签到记录信息")
    @PreAuthorize("@ss.hasPermi('infra:signInCenter:remove')")
    @Log(title = "用户签到记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(quicoUserSignInService.deleteQuicoUserSignInByIds(ids));
    }

    // ======================== 签到核心接口（Redis Bitmap + MySQL） ========================

    /**
     * 用户签到
     */
    @Operation(summary = "用户签到", description = "执行每日签到，Redis Bitmap + MySQL 双写")
    @PreAuthorize("@ss.hasPermi('infra:signin:sign')")
    @PostMapping("/sign")
    public AjaxResult signIn() {
        Long userId = SecurityUtils.getUserId();
        return signInService.signIn(userId);
    }

    /**
     * 获取签到状态（当月日历 + 连续天数 + 今日是否已签）
     * 支持 year/month 可选参数查看历史月份
     */
    @Operation(summary = "获取签到状态", description = "获取签到日历、连续天数，支持按年月查询历史")
    @PreAuthorize("@ss.hasPermi('infra:signin:query')")
    @GetMapping("/status")
    public AjaxResult getStatus(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long userId = SecurityUtils.getUserId();
        return signInService.getSignInStatus(userId, year, month);
    }

    /**
     * 补签
     */
    @Operation(summary = "补签", description = "补签指定日期")
    @PreAuthorize("@ss.hasPermi('infra:signin:repair')")
    @PostMapping("/repair/{date}")
    public AjaxResult repairSignIn(@PathVariable String date) {
        Long userId = SecurityUtils.getUserId();
        return signInService.repairSignIn(userId, date);
    }
}
