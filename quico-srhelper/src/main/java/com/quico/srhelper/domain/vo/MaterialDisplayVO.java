package com.quico.srhelper.domain.vo;

import lombok.Data;

// 材料展示VO
@Data
public class MaterialDisplayVO {
    private Long itemId;
    private String itemName;
    private String image;
    private Integer quantity;
}
