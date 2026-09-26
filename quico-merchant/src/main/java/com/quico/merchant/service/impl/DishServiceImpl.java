package com.quico.merchant.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.quico.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.quico.merchant.domain.DishFlavor;
import com.quico.merchant.mapper.DishMapper;
import com.quico.merchant.domain.Dish;
import com.quico.merchant.service.IDishService;

/**
 * 商品管理Service业务层处理
 * 
 * @author zhaowei
 * @date 2026-04-16
 */
@Service
public class DishServiceImpl implements IDishService 
{
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询商品管理
     * 
     * @param id 商品管理主键
     * @return 商品管理
     */
    @Override
    public Dish selectDishById(Long id)
    {
        return dishMapper.selectDishById(id);
    }

    /**
     * 查询商品管理列表
     * 
     * @param dish 商品管理
     * @return 商品管理
     */
    @Override
    public List<Dish> selectDishList(Dish dish)
    {
        return dishMapper.selectDishList(dish);
    }

    /**
     * 新增商品管理
     * 
     * @param dish 商品管理
     * @return 结果
     */
    @Transactional
    @Override
    public int insertDish(Dish dish)
    {
        dish.setCreateTime(DateUtils.getNowDate());
        int rows = dishMapper.insertDish(dish);
        insertDishFlavor(dish);
        return rows;
    }

    /**
     * 修改商品管理
     * 
     * @param dish 商品管理
     * @return 结果
     */
    @Transactional
    @Override
    public int updateDish(Dish dish)
    {
        dish.setUpdateTime(DateUtils.getNowDate());
        dishMapper.deleteDishFlavorByDishId(dish.getId());
        insertDishFlavor(dish);
        return dishMapper.updateDish(dish);
    }

    /**
     * 批量删除商品管理
     * 
     * @param ids 需要删除的商品管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteDishByIds(Long[] ids)
    {
        dishMapper.deleteDishFlavorByDishIds(ids);
        return dishMapper.deleteDishByIds(ids);
    }

    /**
     * 删除商品管理信息
     * 
     * @param id 商品管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteDishById(Long id)
    {
        dishMapper.deleteDishFlavorByDishId(id);
        return dishMapper.deleteDishById(id);
    }

    /**
     * 新增商品口味关系信息
     * 
     * @param dish 商品管理对象
     */
    public void insertDishFlavor(Dish dish)
    {
        List<DishFlavor> dishFlavorList = dish.getDishFlavorList();
        Long id = dish.getId();
        if (StringUtils.isNotNull(dishFlavorList))
        {
            List<DishFlavor> list = new ArrayList<DishFlavor>();
            for (DishFlavor dishFlavor : dishFlavorList)
            {
                dishFlavor.setDishId(id);
                list.add(dishFlavor);
            }
            if (list.size() > 0)
            {
                dishMapper.batchDishFlavor(list);
            }
        }
    }
}
