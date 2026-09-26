package com.quico.srhelper.controller;

import java.util.List;
import java.util.Map;
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
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.service.ISrUserItemService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 用户持有材料Controller
 * 
 * @author quico
 * @date 2026-06-03
 */
@Tag(name = "用户持有材料Controller", description = "用户持有材料相关接口")
@RestController
@RequestMapping("/srhelper/userItem")
public class SrUserItemController extends BaseController
{
    @Autowired
    private ISrUserItemService srUserItemService;

    /**
     * 查询用户持有材料列表
     */
    @Operation(summary = "查询用户持有材料列表", description = "分页查询用户持有材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrUserItem srUserItem)
    {
        startPage();
        List<SrUserItem> list = srUserItemService.selectSrUserItemList(srUserItem);
        return getDataTable(list);
    }

    /**
     * 导出用户持有材料列表
     */
    @Operation(summary = "导出用户持有材料列表", description = "导出用户持有材料列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:export')")
    @Log(title = "用户持有材料", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrUserItem srUserItem)
    {
        List<SrUserItem> list = srUserItemService.selectSrUserItemList(srUserItem);
        ExcelUtil<SrUserItem> util = new ExcelUtil<SrUserItem>(SrUserItem.class);
        util.exportExcel(response, list, "用户持有材料数据");
    }

    /**
     * 获取用户持有材料数量映射
     */
    @Operation(summary = "获取用户持有材料数量映射", description = "根据userId获取用户持有的所有材料数量")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:query')")
    @GetMapping("/quantityMap")
    public AjaxResult getQuantityMap(@RequestParam("userId") Long userId)
    {
        Map<Long, Long> quantityMap = srUserItemService.getQuantityMapByUserId(userId);
        return success(quantityMap);
    }

    /**
     * 获取用户持有材料详细信息
     */
    @Operation(summary = "获取用户持有材料详细信息", description = "根据ID获取用户持有材料详细信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srUserItemService.selectSrUserItemById(id));
    }

    /**
     * 根据用户ID和材料ID查询记录
     */
    @Operation(summary = "根据用户ID和材料ID查询记录", description = "根据userId和itemId获取用户持有材料记录")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:query')")
    @GetMapping("/byUserIdAndItemId")
    public AjaxResult getByUserIdAndItemId(@RequestParam("userId") Long userId, @RequestParam("itemId") Long itemId)
    {
        return success(srUserItemService.selectByUserIdAndItemId(userId, itemId));
    }

    /**
     * 新增用户持有材料（如果已存在则更新）
     */
    @Operation(summary = "新增用户持有材料", description = "新增用户持有材料，如果已存在则更新")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:add')")
    @Log(title = "用户持有材料", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrUserItem srUserItem)
    {
        // 检查记录是否已存在
        SrUserItem existing = srUserItemService.selectByUserIdAndItemId(srUserItem.getUserId(), srUserItem.getItemId());
        if (existing != null) {
            // 已存在则更新
            srUserItem.setId(existing.getId());
            return toAjax(srUserItemService.updateSrUserItem(srUserItem));
        }
        return toAjax(srUserItemService.insertSrUserItem(srUserItem));
    }

    /**
     * 修改用户持有材料
     */
    @Operation(summary = "修改用户持有材料", description = "更新用户持有材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:edit')")
    @Log(title = "用户持有材料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrUserItem srUserItem)
    {
        return toAjax(srUserItemService.updateSrUserItem(srUserItem));
    }

    /**
     * 删除用户持有材料
     */
    @Operation(summary = "删除用户持有材料", description = "批量删除用户持有材料信息")
    @PreAuthorize("@ss.hasPermi('srhelper:userItem:remove')")
    @Log(title = "用户持有材料", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(srUserItemService.deleteSrUserItemByIds(ids));
    }
}