package com.quico.web.controller.system;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.system.domain.SysSocialClient;
import com.quico.system.service.ISysSocialClientService;

/**
 * JustAuth三方登录配置Controller
 * 
 * @author quico
 * @date 2026-11-03
 */
@Tag(name = "JustAuth三方登录配置Controller", description = "JustAuth三方登录配置相关接口")
@RestController
@RequestMapping("/system/socialClient")
public class SysSocialClientController extends BaseController
{
    @Autowired
    private ISysSocialClientService sysSocialClientService;

    /**
     * 查询JustAuth三方登录配置列表
     */
    @Operation(summary = "查询JustAuth三方登录配置列表", description = "分页查询JustAuth三方登录配置信息")
    @PreAuthorize("@ss.hasPermi('system:socialClient:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysSocialClient sysSocialClient)
    {
        startPage();
        List<SysSocialClient> list = sysSocialClientService.selectSysSocialClientList(sysSocialClient);
        return getDataTable(list);
    }

    /**
     * 导出JustAuth三方登录配置列表
     */
    @Operation(summary = "导出JustAuth三方登录配置列表", description = "导出JustAuth三方登录配置列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('system:socialClient:export')")
    @Log(title = "JustAuth三方登录配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysSocialClient sysSocialClient)
    {
        List<SysSocialClient> list = sysSocialClientService.selectSysSocialClientList(sysSocialClient);
        ExcelUtil<SysSocialClient> util = new ExcelUtil<SysSocialClient>(SysSocialClient.class);
        util.exportExcel(response, list, "JustAuth三方登录配置数据");
    }

    /**
     * 获取JustAuth三方登录配置详细信息
     */
    @Operation(summary = "获取JustAuth三方登录配置详细信息", description = "根据ID获取JustAuth三方登录配置详细信息")
    @PreAuthorize("@ss.hasPermi('system:socialClient:query')")
    @GetMapping(value = "/{clientId}")
    public AjaxResult getInfo(@PathVariable("clientId") Long clientId)
    {
        return success(sysSocialClientService.selectSysSocialClientByClientId(clientId));
    }

    /**
     * 新增JustAuth三方登录配置
     */
    @Operation(summary = "新增JustAuth三方登录配置", description = "新增JustAuth三方登录配置")
    @PreAuthorize("@ss.hasPermi('system:socialClient:add')")
    @Log(title = "JustAuth三方登录配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysSocialClient sysSocialClient)
    {
        if (!sysSocialClientService.checkSourceUnique(sysSocialClient))
        {
            return error("新增三方登录配置'" + sysSocialClient.getClientName() + "'失败，平台标识已存在");
        }
        sysSocialClient.setCreateBy(getUsername());
        return toAjax(sysSocialClientService.insertSysSocialClient(sysSocialClient));
    }

    /**
     * 修改JustAuth三方登录配置
     */
    @Operation(summary = "修改JustAuth三方登录配置", description = "更新JustAuth三方登录配置信息")
    @PreAuthorize("@ss.hasPermi('system:socialClient:edit')")
    @Log(title = "JustAuth三方登录配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysSocialClient sysSocialClient)
    {
        if (!sysSocialClientService.checkSourceUnique(sysSocialClient))
        {
            return error("修改三方登录配置'" + sysSocialClient.getClientName() + "'失败，平台标识已存在");
        }
        sysSocialClient.setUpdateBy(getUsername());
        return toAjax(sysSocialClientService.updateSysSocialClient(sysSocialClient));
    }

    /**
     * 删除JustAuth三方登录配置
     */
    @Operation(summary = "删除JustAuth三方登录配置", description = "批量删除JustAuth三方登录配置信息")
    @PreAuthorize("@ss.hasPermi('system:socialClient:remove')")
    @Log(title = "JustAuth三方登录配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{clientIds}")
    public AjaxResult remove(@PathVariable Long[] clientIds)
    {
        return toAjax(sysSocialClientService.deleteSysSocialClientByClientIds(clientIds));
    }
}
