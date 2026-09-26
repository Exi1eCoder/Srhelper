package com.quico.merchant.domain;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;

/**
 * 商品管理对象 tb_dish
 * 
 * @author zhaowei
 * @date 2026-04-16
 */
@Data
public class Dish extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String name;

    /** 售价 */
    @Excel(name = "售价")
    private BigDecimal price;

    /** 图片 */
    @Excel(name = "图片")
    private String image;

    /** 描述信息 */
    private String description;

    /** 售卖状态 */
    @Excel(name = "售卖状态")
    private Long status;

    /** 商品口味关系信息 */
    private List<DishFlavor> dishFlavorList;

}
