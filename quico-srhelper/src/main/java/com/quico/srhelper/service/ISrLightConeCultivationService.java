package com.quico.srhelper.service;

import com.quico.srhelper.domain.vo.SrLightConeCultivationVO;

/**
 * 光锥养成计算Service接口
 * 
 * @author quico
 * @date 2026-06-07
 */
public interface ISrLightConeCultivationService 
{
    /**
     * 计算单个光锥的养成材料需求
     * 
     * @param userLightConeId 用户持有光锥ID
     * @param targetLevel 目标等级
     * @return 光锥养成计算结果
     */
    public SrLightConeCultivationVO calculateLightConeCultivation(Long userLightConeId, Integer targetLevel);

    /**
     * 计算用户所有光锥的总体养成材料需求
     * 
     * @param userId 用户ID
     * @param targetLevel 目标等级
     * @return 光锥总体养成计算结果
     */
    public SrLightConeCultivationVO calculateTotalLightConeCultivation(Long userId, Integer targetLevel);
}