package com.quico.merchant.controller;

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
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.merchant.domain.Dish;
import com.quico.merchant.service.IDishService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 商品管理Controller
 * 
 * @author zhaowei
 * @date 2026-04-16
 */
@RestController
@RequestMapping("/merchant/dish")
public class DishController extends BaseController
{
    @Autowired
    private IDishService dishService;

    /**
     * 查询商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dish dish)
    {
        startPage();
        List<Dish> list = dishService.selectDishList(dish);
        return getDataTable(list);
    }

    /**
     * 导出商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:export')")
    @Log(title = "商品管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Dish dish)
    {
        List<Dish> list = dishService.selectDishList(dish);
        ExcelUtil<Dish> util = new ExcelUtil<Dish>(Dish.class);
        util.exportExcel(response, list, "商品管理数据");
    }

    /**
     * 获取商品管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(dishService.selectDishById(id));
    }

    /**
     * 新增商品管理
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Dish dish)
    {
        return toAjax(dishService.insertDish(dish));
    }

    /**
     * 修改商品管理
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Dish dish)
    {
        return toAjax(dishService.updateDish(dish));
    }

    /**
     * 删除商品管理
     */
    @PreAuthorize("@ss.hasPermi('merchant:dish:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dishService.deleteDishByIds(ids));
    }
}
