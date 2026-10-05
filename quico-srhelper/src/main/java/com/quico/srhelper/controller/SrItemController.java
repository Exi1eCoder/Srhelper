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
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.dto.SrItemBindDTO;
import com.quico.srhelper.service.ISrItemService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 材料一览Controller
 * 
 * @author quico
 * @date 2026-05-25
 */
@Tag(name = "材料一览Controller", description = "材料一览相关接口")
@RestController
@RequestMapping("/srhelper/srItem")
public class SrItemController extends BaseController
{
    @Autowired
    private ISrItemService srItemService;

    /**
     * 查询材料一览列表
     */
    @Operation(summary = "查询材料一览列表", description = "分页查询材料一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrItem srItem)
    {
        // startPage();
        List<SrItem> list = srItemService.selectSrItemList(srItem);
        return getDataTable(list);
    }

    /**
     * 检查材料图鉴修改权限
     */
    @Operation(summary = "检查材料图鉴修改权限", description = "检查当前用户是否有材料图鉴修改权限")
    @GetMapping("/auth/check")
    public AjaxResult checkPermission()
    {
        // 检查用户是否有新增或编辑权限
        boolean hasPermission = SecurityUtils.hasPermi("srhelper:srItem:add") || SecurityUtils.hasPermi("srhelper:item:edit");
        return success(hasPermission);
    }

    /**
     * 导出材料一览列表
     */
    @Operation(summary = "导出材料一览列表", description = "导出材料一览列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:export')")
    @Log(title = "材料一览", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrItem srItem)
    {
        List<SrItem> list = srItemService.selectSrItemList(srItem);
        ExcelUtil<SrItem> util = new ExcelUtil<SrItem>(SrItem.class);
        util.exportExcel(response, list, "材料一览数据");
    }

    /**
     * 导入材料数据（与 export 配套，按 itemName 去重）
     */
    @Operation(summary = "导入材料数据", description = "通过Excel批量导入材料一览信息，updateSupport=true 时已存在记录会被更新")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:add') or @ss.hasPermi('srhelper:srItem:edit')")
    @Log(title = "材料一览", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SrItem> util = new ExcelUtil<SrItem>(SrItem.class);
        List<SrItem> itemList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUserId().toString();
        String message = srItemService.importItem(itemList, updateSupport, operName);
        return success(message);
    }

    /**
     * 获取材料一览详细信息
     */
    @Operation(summary = "获取材料一览详细信息", description = "根据ID获取材料一览详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srItemService.selectSrItemDetailById(id));
    }

    /**
     * 新增材料一览
     */
    @Operation(summary = "新增材料一览", description = "新增材料一览")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:add')")
    @Log(title = "材料一览", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrItemBindDTO bindDTO)
    {
        // 保存基本信息
        int result = srItemService.insertSrItem(bindDTO);
        
        // 绑定高阶素材
        if (result > 0 && (bindDTO.getAdvancedItem3Star() != null || bindDTO.getAdvancedItem4Star() != null)) {
            srItemService.bindAdvancedItems(bindDTO);
        }
        
        return toAjax(result);
    }

    /**
     * 修改材料一览
     */
    @Operation(summary = "修改材料一览", description = "更新材料一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:edit')")
    @Log(title = "材料一览", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrItemBindDTO bindDTO)
    {
        // 更新基本信息
        int result = srItemService.updateSrItem(bindDTO);
        
        // 绑定高阶素材
        if (result > 0 && (bindDTO.getAdvancedItem3Star() != null || bindDTO.getAdvancedItem4Star() != null)) {
            srItemService.bindAdvancedItems(bindDTO);
        }
        
        return toAjax(result);
    }

    /**
     * 删除材料一览
     */
    @Operation(summary = "删除材料一览", description = "批量删除材料一览信息")
    @PreAuthorize("@ss.hasPermi('srhelper:srItem:remove')")
    @Log(title = "材料一览", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srItemService.deleteSrItemByIds(ids));
    }
}
