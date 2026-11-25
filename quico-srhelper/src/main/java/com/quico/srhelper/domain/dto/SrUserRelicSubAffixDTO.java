package com.quico.srhelper.domain.dto;

import lombok.Data;

/**
 * 用户遗器副词条保存意图
 * 
 * @author quico
 */
@Data
public class SrUserRelicSubAffixDTO
{
    /** 属性代码 */
    private String property;

    /** 初始档位序号 */
    private Integer baseTier;

    /** 强化档位序列，逗号分隔 */
    private String upgradeTiers;
}
