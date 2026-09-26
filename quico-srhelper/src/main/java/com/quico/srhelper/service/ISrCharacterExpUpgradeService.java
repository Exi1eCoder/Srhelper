package com.quico.srhelper.service;

import com.quico.srhelper.domain.dto.SrCharacterExpUpgradeDTO;
import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;

import java.util.List;

/**
 * 角色经验升级Service接口
 */
public interface ISrCharacterExpUpgradeService {
    
    List<SrCharacterExpUpgradeVO> selectAllExpUpgrades();
    
    SrCharacterExpUpgradeVO selectExpUpgradeById(Long id);
    
    SrCharacterExpUpgradeVO selectExpUpgradeByLevel(Integer level);
    
    int insertExpUpgrade(SrCharacterExpUpgradeDTO dto);
    
    int updateExpUpgrade(SrCharacterExpUpgradeDTO dto);
    
    int deleteExpUpgradeById(Long id);
}
