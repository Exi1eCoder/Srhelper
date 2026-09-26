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
 * 角色经验升级实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sr_character_exp_upgrade")
public class SrCharacterExpUpgrade {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Integer minLevel;
    
    private Integer maxLevel;
    
    private Long expRequired;
    
    private Long creditsRequired;
    
    private Date createTime;
    
    private Date updateTime;
}