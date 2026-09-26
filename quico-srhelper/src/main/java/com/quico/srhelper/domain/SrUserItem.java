package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 用户持有材料对象 sr_user_item
 * 
 * @author quico
 * @date 2026-06-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 材料ID */
    @Excel(name = "材料ID")
    private Long itemId;

    /** 拥有数量 */
    @Excel(name = "拥有数量")
    private Long quantity;


}
