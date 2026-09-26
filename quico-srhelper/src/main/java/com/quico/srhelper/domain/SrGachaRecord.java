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
 * 跃迁记录对象 sr_gacha_record
 * 
 * @author quico
 * @date 2026-08-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrGachaRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卡池ID */
    @Excel(name = "卡池ID")
    private String gachaId;

    /** 卡池类型 */
    @Excel(name = "卡池类型")
    private String gachaType;

    /** 抽卡记录流水 */
    private String gachaRecordId;

    /** 物品ID */
    @Excel(name = "物品ID")
    private String itemId;

    /** 物品类型 */
    @Excel(name = "物品类型")
    private String itemType;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 稀有度 */
    @Excel(name = "稀有度")
    private String rankType;

    /** 抽卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "抽卡时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /** 用户ID */
    @Excel(name = "用户ID")
    private String uid;

    /** 总抽数 */
    @Excel(name = "总抽数")
    private Integer totalPulls;

    /** 保底内抽数 */
    @Excel(name = "保底内抽数")
    private Integer pityCount;

    /** 图片URL（非数据库字段，由缓存填充） */
    private String imageUrl;

}
