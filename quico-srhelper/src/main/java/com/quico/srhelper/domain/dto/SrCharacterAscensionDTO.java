package com.quico.srhelper.domain.dto;

import lombok.Data;

// 晋升材料DTO - 适配前端扁平化结构
@Data
public class SrCharacterAscensionDTO {
    private Long id;
    private Long characterId;
    private String ascensionType;      // 前端字段 - 材料类型(BREAKTHROUGH/BASIC_ATK/SKILL/ULTIMATE/TALENT/TECHNIQUE/ELATION_SKILL/BONUS_ABILITY/STAT_BONUS)
    private Integer ascensionLevel;     // 前端字段 - 突破等级
    private Integer materialSlot;       // 前端字段 - 材料槽位
    private Long itemId;                // 前端字段 - 物品ID
    private String itemName;            // 前端字段 - 物品名称
    private String itemImage;           // 前端字段 - 物品图片
    private Integer quantity;           // 前端字段 - 数量
    private Integer rarityLevel;        // 前端字段 - 稀有度
    private Integer sortOrder;          // 排序
    private java.util.Date createTime;  // 创建时间
}