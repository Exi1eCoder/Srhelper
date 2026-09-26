package com.quico.srhelper.domain;

import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 光锥材料绑定对象 sr_lightcone_material_bind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrLightconeMaterialBind extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "光锥ID")
    private Long lightconeId;

    @Excel(name = "光锥名称")
    private String lightconeName;

    @Excel(name = "材料类型")
    private String itemAscensionType;

    @Excel(name = "材料ID")
    private Long itemId;

    private Long seriesId;

    @Excel(name = "材料名称")
    private String itemName;

    @Excel(name = "材料图片")
    private String itemImage;

    @Excel(name = "材料稀有度")
    private Integer rarityLevel;

    private Integer sortOrder;
}
