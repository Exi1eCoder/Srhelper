package com.quico.merchant.service;

import java.util.List;
import com.quico.merchant.domain.Dish;

/**
 * 商品管理Service接口
 * 
 * @author zhaowei
 * @date 2026-04-16
 */
public interface IDishService 
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
     * 批量删除商品管理
     * 
     * @param ids 需要删除的商品管理主键集合
     * @return 结果
     */
    public int deleteDishByIds(Long[] ids);

    /**
     * 删除商品管理信息
     * 
     * @param id 商品管理主键
     * @return 结果
     */
    public int deleteDishById(Long id);
}
