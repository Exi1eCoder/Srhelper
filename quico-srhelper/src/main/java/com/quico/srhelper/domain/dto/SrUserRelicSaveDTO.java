package com.quico.srhelper.domain.dto;

import java.util.List;

import lombok.Data;

/**
 * 用户遗器保存请求
 * 
 * @author quico
 */
@Data
public class SrUserRelicSaveDTO
{
    /** 主键ID，空表示新建 */
    private Long id;

    /** 遗器ID */
    private String relicId;

    /** 等级 */
    private Integer level;

    /** 主属性代码 */
    private String mainProperty;

    /** 初始副词条数 3 / 4 */
    private Integer initialSubCount;

    /** 来源 SIMULATED / IMPORTED / IMPORTED_MODIFIED */
    private String origin;

    /** 是否锁定 */
    private Boolean locked;

    /** 装备角色ID */
    private Long equippedBy;

    /** 规则数据版本 */
    private String ruleVersion;

    /** 副词条意图 */
    private List<SrUserRelicSubAffixDTO> subAffixes;
}
