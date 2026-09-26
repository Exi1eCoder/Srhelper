package com.quico.srhelper.controller;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.SecurityUtils;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.dto.LightConeSaveDTO;
import com.quico.srhelper.service.ISrLightConesService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 光锥一览Controller
 * 
 * @author quico
 * @date 2026-05-25
 */
@Tag(name = "光锥一览Controller", description = "光锥一览相关接口")
@RestController
@RequestMapping("/srhelper/lightcones")
public class SrLightConesController extends BaseController
{
    @Autowired
    private ISrLightConesService srLightConesService;

    /**
     * 查询光锥一览列表
     */
    @Operation(summary = "查询光锥一览列表", description = "分页查询光锥一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrLightCones srLightCones)
    {
        startPage();
        List<SrLightCones> list = srLightConesService.selectSrLightConesList(srLightCones);
        return getDataTable(list);
    }

    
    /**
     * 导入光锥数据（与 export 配套，按光锥名字 + 实装版本去重）
     */
    @Operation(summary = "导入光锥数据", description = "通过Excel批量导入光锥基本信息，updateSupport=true 时已存在记录会被更新")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:add') or @ss.hasPermi('srhelper:lightCones:edit')")
    @Log(title = "光锥一览", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SrLightCones> util = new ExcelUtil<SrLightCones>(SrLightCones.class);
        List<SrLightCones> lightConesList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUserId().toString();
        String message = srLightConesService.importLightCones(lightConesList, updateSupport, operName);
        return success(message);
    }


    /**
     * 导出光锥一览列表
     */
    @Operation(summary = "导出光锥一览列表", description = "导出光锥一览列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:export')")
    @Log(title = "光锥一览", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrLightCones srLightCones)
    {
        List<SrLightCones> list = srLightConesService.selectSrLightConesList(srLightCones);
        ExcelUtil<SrLightCones> util = new ExcelUtil<SrLightCones>(SrLightCones.class);
        util.exportExcel(response, list, "光锥一览数据");
    }

    /**
     * 获取光锥一览详细信息
     */
    @Operation(summary = "获取光锥一览详细信息", description = "根据ID获取光锥一览详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srLightConesService.selectSrLightConesById(id));
    }

    /**
     * 获取光锥详情（含材料绑定）
     */
    @Operation(summary = "获取光锥详情", description = "根据ID获取光锥信息及其材料绑定")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getDetail(@PathVariable("id") Long id) {
        return success(srLightConesService.getDetail(id));
    }

    /**
     * 新增光锥一览
     */
    @Operation(summary = "新增光锥一览", description = "新增光锥一览")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:add')")
    @Log(title = "光锥一览", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrLightCones srLightCones)
    {
        srLightConesService.insertSrLightCones(srLightCones);
        return success(srLightCones);
    }

    /**
     * 修改光锥一览
     */
    @Operation(summary = "修改光锥一览", description = "更新光锥一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:edit')")
    @Log(title = "光锥一览", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrLightCones srLightCones)
    {
        return toAjax(srLightConesService.updateSrLightCones(srLightCones));
    }

    /**
     * 删除光锥一览
     */
    @Operation(summary = "删除光锥一览", description = "批量删除光锥一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:remove')")
    @Log(title = "光锥一览", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srLightConesService.deleteSrLightConesByIds(ids));
    }

    /**
     * 统一保存/更新光锥及材料绑定
     */
    @Operation(summary = "保存光锥及材料绑定", description = "保存或更新光锥信息及其材料绑定")
    @PreAuthorize("@ss.hasPermi('srhelper:lightCones:add') or @ss.hasPermi('srhelper:lightCones:edit')")
    @Log(title = "光锥", businessType = BusinessType.INSERT)
    @PostMapping("/saveAll")
    public AjaxResult saveAll(@RequestBody LightConeSaveDTO dto)
    {
        Long lightConeId = srLightConesService.saveAll(dto);
        return success(lightConeId);
    }
}
