package com.quico.srhelper.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.constant.HttpStatus;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.exception.BusinessException;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.ip.IpUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.dto.GachaImportResult;
import com.quico.srhelper.exception.UploadValidateException;
import com.quico.srhelper.service.helper.GachaImportValidator;
import com.quico.srhelper.service.helper.GachaRecalcService;
import com.quico.srhelper.service.helper.GachaUploadRiskService;
import com.quico.srhelper.service.ISrGachaRecordService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 跃迁记录Controller
 * 
 * @author quico
 * @date 2026-08-19
 */
@Tag(name = "跃迁记录Controller", description = "跃迁记录相关接口")
@RestController
@RequestMapping("/srhelper/gachaRecord")
@Slf4j
public class SrGachaRecordController extends BaseController
{
    @Autowired
    private ISrGachaRecordService srGachaRecordService;

    @Autowired
    private GachaRecalcService gachaRecalcService;

    @Autowired
    private GachaImportValidator gachaImportValidator;

    @Autowired
    private GachaUploadRiskService gachaUploadRiskService;

    /**
     * 查询当前用户所有去重uid（游戏账号）
     */
    @Operation(summary = "查询账号列表", description = "查询当前用户导入过的所有游戏账号uid")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/uidList")
    public AjaxResult uidList()
    {
        String createBy = SecurityUtils.getUserId().toString();
        List<String> uidList = srGachaRecordService.selectDistinctUidList(createBy);
        log.debug("查询uid列表结果, createBy={}, size={}, uidList={}", createBy, uidList.size(), uidList);
        return success(uidList);
    }

    /**
     * 查询跃迁记录列表
     */
    @Operation(summary = "查询跃迁记录列表", description = "分页查询跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setCreateBy(SecurityUtils.getUserId().toString());
        startPage();
        List<SrGachaRecord> list = srGachaRecordService.selectSrGachaRecordList(srGachaRecord);
        return getDataTable(list);
    }

    /**
     * 抽卡分析：按卡池类型分组返回五星记录
     */
    @Operation(summary = "抽卡分析", description = "按卡池类型分组返回五星记录")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/analysis")
    public AjaxResult analysis(SrGachaRecord srGachaRecord)
    {
        srGachaRecord.setCreateBy(SecurityUtils.getUserId().toString());
        return success(srGachaRecordService.analysisGachaRecord(srGachaRecord));
    }

    /**
     * 导出跃迁记录列表
     */
    @Operation(summary = "导出跃迁记录列表", description = "导出跃迁记录列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:export')")
    @Log(title = "跃迁记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrGachaRecord srGachaRecord)
    {
        List<SrGachaRecord> list = srGachaRecordService.selectSrGachaRecordList(srGachaRecord);
        ExcelUtil<SrGachaRecord> util = new ExcelUtil<SrGachaRecord>(SrGachaRecord.class);
        util.exportExcel(response, list, "跃迁记录数据");
    }

    /**
     * 获取跃迁记录详细信息
     */
    @Operation(summary = "获取跃迁记录详细信息", description = "根据ID获取跃迁记录详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:query')")
    @GetMapping(value = "/{gachaRecordId}")
    public AjaxResult getInfo(@PathVariable("gachaRecordId") String gachaRecordId)
    {
        return success(srGachaRecordService.selectSrGachaRecordByGachaRecordId(gachaRecordId));
    }

    /**
     * 新增跃迁记录
     */
    @Operation(summary = "新增跃迁记录", description = "新增跃迁记录")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:add')")
    @Log(title = "跃迁记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrGachaRecord srGachaRecord)
    {
        return toAjax(srGachaRecordService.insertSrGachaRecord(srGachaRecord));
    }

    /**
     * 修改跃迁记录
     */
    @Operation(summary = "修改跃迁记录", description = "更新跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:edit')")
    @Log(title = "跃迁记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrGachaRecord srGachaRecord)
    {
        return toAjax(srGachaRecordService.updateSrGachaRecord(srGachaRecord));
    }

    /**
     * 删除跃迁记录
     */
    @Operation(summary = "删除跃迁记录", description = "批量删除跃迁记录信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:remove')")
    @Log(title = "跃迁记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{gachaRecordIds}")
    public AjaxResult remove(@PathVariable String[] gachaRecordIds)
    {
        return toAjax(srGachaRecordService.deleteSrGachaRecordByGachaRecordIds(gachaRecordIds));
    }

    /**
     * 导入跃迁记录（五层漏斗：物理拦截 -> 解析 -> 字典白名单 -> 防伪 -> 风控审计）
     * 返回结构化结果：needRecalc=true 表示补录了历史数据
     */
    @Operation(summary = "导入跃迁记录", description = "从星穹铁道跃迁记录Excel的rawData sheet导入抽卡数据")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:import')")
    @Log(title = "跃迁记录", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport, HttpServletRequest request)
    {
        String ip = IpUtils.getIpAddr(request);
        Long userId = SecurityUtils.getUserId();

        // 第一层前置拦截：封禁检查直接返回，不落审计、不计数
        if (gachaUploadRiskService.isIpBanned(ip))
        {
            return AjaxResult.error(HttpStatus.FORBIDDEN, GachaUploadRiskService.MSG_IP_BANNED);
        }
        if (gachaUploadRiskService.isUserBanned(userId.toString()))
        {
            return AjaxResult.error(GachaUploadRiskService.CODE_USER_BANNED, GachaUploadRiskService.MSG_USER_BANNED);
        }

        try
        {
            // 第一层：文件后缀 / 大小 / xlsx 魔数
            gachaImportValidator.validatePhysical(file);
            // 第二层：EasyExcel 流式解析 + 空文件/超限/解析异常
            List<SrGachaRecord> recordList = gachaImportValidator.parse(file);
            // 第三层：gacha_type 字典白名单
            gachaImportValidator.validateGachaType(recordList);
            // 第四层：纯文件级防伪校验（逃生舱可关闭）
            gachaImportValidator.validateForgery(recordList);

            GachaImportResult result = srGachaRecordService.importGachaRecord(recordList, updateSupport, userId.toString());
            // 成功即清零失败计数
            gachaUploadRiskService.clearFailCount(userId.toString(), ip);
            return AjaxResult.success(result.getMessage(), result);
        }
        catch (UploadValidateException e)
        {
            // 第五层：审计落库 + 信用分级（>=5 次封禁并抛出伪装成网络超时的 500）
            return gachaUploadRiskService.handleViolation(e, userId, ip, file);
        }
        catch (BusinessException e)
        {
            // 系统/业务异常（如字典未配置）转成 HTTP 200 + body code，确保前端能拿到具体提示；
            // 封禁伪装的 500 是在上面的 catch 块内抛出的，不会被这里捕获，仍返回真正的 HTTP 500。
            return AjaxResult.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 批量查询抽卡统计重算状态
     * 前端传入自己的 uid 集合，返回每个 uid 的状态：DONE / PROCESSING / UNKNOWN
     * 用于判断哪些账号需要重算、是否提示用户、重算按钮是否可点
     */
    @Operation(summary = "批量查询重算状态", description = "传入uid集合，返回每个uid的重算状态")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @GetMapping("/recalc/status")
    public AjaxResult recalcStatus(String[] uids)
    {
        if (uids == null || uids.length == 0)
        {
            return success(new HashMap<String, String>());
        }
        return success(gachaRecalcService.status(Arrays.asList(uids)));
    }

    /**
     * 手动触发抽卡统计重算（只能重算当前登录用户自己的账号）
     * 只插队到 pending，不立即执行（凌晨 4 点集中处理）
     */
    @Operation(summary = "手动重算", description = "抽卡统计异常时手动提交重新计算，每天限一次")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaRecord:list')")
    @Log(title = "跃迁记录", businessType = BusinessType.OTHER)
    @PostMapping("/recalc/{uid}")
    public AjaxResult recalc(@PathVariable("uid") String uid)
    {
        String status = gachaRecalcService.status(uid);
        if (GachaRecalcService.STATUS_PROCESSING.equals(status))
        {
            return success("已在重算队列中，将在凌晨 4:00 执行");
        }
        String createBy = SecurityUtils.getUserId().toString();
        String err = gachaRecalcService.triggerManual(uid, createBy);
        if (err != null)
        {
            return AjaxResult.error(err);
        }
        return success("已提交重算，将在凌晨 4:00 执行，请次日刷新查看");
    }

    /**
     * 下载导入模板
     */
    @Operation(summary = "下载导入模板", description = "下载跃迁记录导入模板")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SrGachaRecord> util = new ExcelUtil<SrGachaRecord>(SrGachaRecord.class);
        util.importTemplateExcel(response, "rawData");
    }
}
