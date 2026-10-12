package com.quico.srhelper.service;

import com.quico.srhelper.domain.vo.SrCharacterCultivationVO;
import com.quico.srhelper.domain.vo.SrCharactersCultivationVO;
import com.quico.srhelper.domain.vo.SrCultivationCalcVO;

/**
 * 养成计算Service接口
 */
public interface ISrCultivationService {
    
    /**
     * 计算用户所有角色的总体养成材料
     * @param userId 用户ID
     * @param targetLevel 目标等级（默认80）
     * @return 养成计算结果
     */
    SrCultivationCalcVO calculateTotalCultivation(Long userId, Integer targetLevel);
    
    /**
     * 计算用户所有角色的养成材料（每个角色单独返回，不含总计）
     * @param userId 用户ID
     * @param targetLevel 目标等级（默认80）
     * @return 各角色养成材料计算结果
     */
    SrCharactersCultivationVO calculateUserCharactersCultivation(Long userId, Integer targetLevel);
    
    /**
     * 计算单个角色的养成材料
     * @param userCharacterId 用户角色ID
     * @param targetLevel 目标等级
     * @return 单个角色养成计算结果
     */
    SrCharacterCultivationVO calculateCharacterCultivation(Long userCharacterId, Integer targetLevel);
}