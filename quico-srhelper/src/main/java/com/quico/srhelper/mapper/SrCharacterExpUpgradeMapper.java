package com.quico.srhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quico.srhelper.domain.SrCharacterExpUpgrade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色经验升级Mapper接口
 */
@Mapper
public interface SrCharacterExpUpgradeMapper extends BaseMapper<SrCharacterExpUpgrade> {
    
    /**
     * 查询所有升级配置，按等级排序
     */
    public List<SrCharacterExpUpgrade> selectAllOrderByMinLevel();
    
    /**
     * 根据等级查询配置
     */
    public SrCharacterExpUpgrade selectByLevel(Integer level);
}