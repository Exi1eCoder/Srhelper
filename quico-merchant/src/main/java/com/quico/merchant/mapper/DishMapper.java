package com.quico.merchant.mapper;

import java.util.List;
import com.quico.merchant.domain.Dish;
import com.quico.merchant.domain.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品管理Mapper接口
 * 
 * @author zhaowei
 * @date 2026-04-16
 */
public interface DishMapper
{
    /**
     * 查询商品管理
     * 
     * @param id 商品管理主键
     * @return 商品管理
     */
    public Dish selectDishById(Long id);

    /**
     * 查询商品管理列表
     * 
     * @param dish 商品管理
     * @return 商品管理集合
     */
    public List<Dish> selectDishList(Dish dish);

    /**
     * 新增商品管理
     * 
     * @param dish 商品管理
     * @return 结果
     */
    public int insertDish(Dish dish);

    /**
     * 修改商品管理
     * 
     * @param dish 商品管理
     * @return 结果
     */
    public int updateDish(Dish dish);

    /**
     * 删除商品管理
     * 
     * @param id 商品管理主键
     * @return 结果
     */
    public int deleteDishById(Long id);

    /**
     * 批量删除商品管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDishByIds(Long[] ids);

    /**
     * 批量删除商品口味关系
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDishFlavorByDishIds(Long[] ids);
    
    /**
     * 批量新增商品口味关系
     * 
     * @param dishFlavorList 商品口味关系列表
     * @return 结果
     */
    public int batchDishFlavor(List<DishFlavor> dishFlavorList);
    

    /**
     * 通过商品管理主键删除商品口味关系信息
     * 
     * @param id 商品管理ID
     * @return 结果
     */
    public int deleteDishFlavorByDishId(Long id);
}
