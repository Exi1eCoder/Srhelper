package com.quico.srhelper.service;

import com.quico.srhelper.domain.dto.SrLightconeExpUpgradeDTO;
import com.quico.srhelper.domain.vo.SrLightconeExpFullVO;
import com.quico.srhelper.domain.vo.SrLightconeExpUpgradeVO;

import java.util.List;

/**
 * 光锥经验升级Service接口
 */
public interface ISrLightconeExpUpgradeService {

    /**
     * 查询所有升级配置
     */
    List<SrLightconeExpUpgradeVO> selectAllExpUpgrades();

    /**
     * 根据星级查询配置
     */
    List<SrLightconeExpUpgradeVO> selectExpUpgradesByStarLevel(Integer starLevel);

    /**
     * 根据ID查询
     */
    SrLightconeExpUpgradeVO selectExpUpgradeById(Long id);

    /**
     * 根据星级和等级查询配置
     */
    SrLightconeExpUpgradeVO selectExpUpgradeByStarAndLevel(Integer starLevel, Integer level);

    /**
     * 根据星级查询升级配置 + 晋升模板（聚合）
     */
    SrLightconeExpFullVO selectExpFullByStarLevel(Integer starLevel);

    int insertExpUpgrade(SrLightconeExpUpgradeDTO dto);

    int updateExpUpgrade(SrLightconeExpUpgradeDTO dto);

    int deleteExpUpgradeById(Long id);
}
