package com.quico.srhelper.domain.dto;

import lombok.Data;

// 材料项DTO
@Data
public class MaterialItemDTO {
    private Long itemId;
    private String itemName;
    private String image;
    private Integer quantity;
}
