package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 卡池项目对象 sr_gacha_item
 * 
 * @author quico
 * @date 2026-08-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrGachaItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关键字 */
    @Excel(name = "关键字")
    private String keyword;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String itemName;

    /** 项目类型 */
    @Excel(name = "项目类型")
    private String itemType;

    /** 稀有度 */
    @Excel(name = "稀有度")
    private String rankType;

    /** srID */
    @Excel(name = "SRID")
    private String srId;

}
