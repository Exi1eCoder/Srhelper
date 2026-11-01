package com.quico.srhelper.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 角色对象 sr_character
 * 
 * @author quico
 * @date 2026-05-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrCharacter extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 角色名字 */
    @Excel(name = "角色名字")
    private String characterName;

    /** 角色头像URL */
    @Excel(name = "角色头像URL")
    private String avatar;

    /** 角色性别（0男 1女 2未知） */
    @Excel(name = "角色性别", dictType = "sys_user_sex")
    private Long gender;

    /** 星级（1-5星） */
    @Excel(name = "星级", dictType = "star_level")
    private Long starLevel;

    /** 属性 */
    @Excel(name = "属性", dictType = "sr_combat_types")
    private String combatTypes;

    /** 命途 */
    @Excel(name = "命途", dictType = "sr_path")
    private String path;

    /** 阵营 */
    @Excel(name = "阵营")
    private String faction;
    
    /** 实装版本（字典 sr_release_version 的 dictValue 与 dictLabel 相同，导入导出均按文本处理，字典校验在业务层完成） */
    @Excel(name = "实装版本")
    private String releaseVersion;

    /** 简介 */
    @Excel(name = "简介")
    private String description;

    /** 常驻类型：0-限定 1-基础常驻 2-星缘相遇（见 StandardType） */
    @Excel(name = "常驻类型", readConverterExp = "0=限定,1=基础常驻,2=星缘相遇")
    private Integer standardType;

    /** 加入常驻池的时间（歪判定用：抽卡时间不早于该时间才算歪） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "加入常驻池时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date standardStartTime;

    /** 排序值（新增时自动设时间戳，值越大越靠前） */
    private Long sortOrder;

    /** 材料绑定是否完整（可用性标记），由图鉴缓存/列表接口动态推导，不持久化到 sr_character */
    private Boolean usable;
}