package com.quico.srhelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;

/**
 * 光锥经验升级实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sr_lightcone_exp_upgrade")
public class SrLightconeExpUpgrade {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 星级（3/4/5） */
    private Integer starLevel;

    private Integer minLevel;

    private Integer maxLevel;

    private Long expRequired;

    private Long creditsRequired;

    private Date createTime;

    private Date updateTime;
}
