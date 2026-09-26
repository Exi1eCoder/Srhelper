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
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.srhelper.domain.SrGachaItem;
import com.quico.srhelper.service.ISrGachaItemService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 卡池项目Controller
 * 
 * @author quico
 * @date 2026-08-10
 */
@Tag(name = "卡池项目Controller", description = "卡池项目相关接口")
@RestController
@RequestMapping("/srhelper/gachaItem")
public class SrGachaItemController extends BaseController
{
    @Autowired
    private ISrGachaItemService srGachaItemService;

    /**
     * 查询卡池项目列表
     */
    @Operation(summary = "查询卡池项目列表", description = "分页查询卡池项目信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrGachaItem srGachaItem)
    {
        startPage();
        List<SrGachaItem> list = srGachaItemService.selectSrGachaItemList(srGachaItem);
        return getDataTable(list);
    }

    /**
     * 导出卡池项目列表
     */
    @Operation(summary = "导出卡池项目列表", description = "导出卡池项目列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:export')")
    @Log(title = "卡池项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrGachaItem srGachaItem)
    {
        List<SrGachaItem> list = srGachaItemService.selectSrGachaItemList(srGachaItem);
        ExcelUtil<SrGachaItem> util = new ExcelUtil<SrGachaItem>(SrGachaItem.class);
        util.exportExcel(response, list, "卡池项目数据");
    }

    /**
     * 获取卡池项目详细信息
     */
    @Operation(summary = "获取卡池项目详细信息", description = "根据ID获取卡池项目详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srGachaItemService.selectSrGachaItemById(id));
    }

    /**
     * 新增卡池项目
     */
    @Operation(summary = "新增卡池项目", description = "新增卡池项目")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:add')")
    @Log(title = "卡池项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrGachaItem srGachaItem)
    {
        return toAjax(srGachaItemService.insertSrGachaItem(srGachaItem));
    }

    /**
     * 修改卡池项目
     */
    @Operation(summary = "修改卡池项目", description = "更新卡池项目信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:edit')")
    @Log(title = "卡池项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrGachaItem srGachaItem)
    {
        return toAjax(srGachaItemService.updateSrGachaItem(srGachaItem));
    }

    /**
     * 删除卡池项目
     */
    @Operation(summary = "删除卡池项目", description = "批量删除卡池项目信息")
    @PreAuthorize("@ss.hasPermi('srhelper:gachaItem:remove')")
    @Log(title = "卡池项目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srGachaItemService.deleteSrGachaItemByIds(ids));
    }
}
