package com.quico.srhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quico.srhelper.domain.SrLightconeExpUpgrade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 光锥经验升级Mapper接口
 */
@Mapper
public interface SrLightconeExpUpgradeMapper extends BaseMapper<SrLightconeExpUpgrade> {

    /**
     * 查询所有升级配置，按星级和等级排序
     */
    List<SrLightconeExpUpgrade> selectAllOrderByStarAndLevel();

    /**
     * 根据星级和等级查询配置
     */
    SrLightconeExpUpgrade selectByStarAndLevel(Integer starLevel, Integer level);

    /**
     * 根据星级查询所有配置
     */
    List<SrLightconeExpUpgrade> selectByStarLevel(Integer starLevel);
}
