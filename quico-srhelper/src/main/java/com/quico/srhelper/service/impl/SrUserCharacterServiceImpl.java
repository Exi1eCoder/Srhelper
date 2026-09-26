package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrUserCharacterMapper;
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.service.ISrUserCharacterService;

/**
 * 我的角色Service业务层处理
 * 
 * @author quico
 * @date 2026-05-31
 */
@Service
public class SrUserCharacterServiceImpl implements ISrUserCharacterService 
{
    @Autowired
    private SrUserCharacterMapper srUserCharacterMapper;

    /**
     * 查询我的角色
     * 
     * @param id 我的角色主键
     * @return 我的角色
     */
    @Override
    public SrUserCharacter selectSrUserCharacterById(Long id)
    {
        return srUserCharacterMapper.selectSrUserCharacterById(id);
    }

    /**
     * 查询我的角色列表
     * 
     * @param srUserCharacter 我的角色
     * @return 我的角色
     */
    @Override
    public List<SrUserCharacter> selectSrUserCharacterList(SrUserCharacter srUserCharacter)
    {
        return srUserCharacterMapper.selectSrUserCharacterList(srUserCharacter);
    }

    /**
     * 新增我的角色
     * 
     * @param srUserCharacter 我的角色
     * @return 结果
     */
    @Override
    public int insertSrUserCharacter(SrUserCharacter srUserCharacter)
    {
        srUserCharacter.setCreateTime(DateUtils.getNowDate());
        return srUserCharacterMapper.insertSrUserCharacter(srUserCharacter);
    }

    /**
     * 修改我的角色
     * 
     * @param srUserCharacter 我的角色
     * @return 结果
     */
    @Override
    public int updateSrUserCharacter(SrUserCharacter srUserCharacter)
    {
        srUserCharacter.setUpdateTime(DateUtils.getNowDate());
        return srUserCharacterMapper.updateSrUserCharacter(srUserCharacter);
    }

    /**
     * 批量删除我的角色
     * 
     * @param ids 需要删除的我的角色主键
     * @return 结果
     */
    @Override
    public int deleteSrUserCharacterByIds(Long[] ids)
    {
        return srUserCharacterMapper.deleteSrUserCharacterByIds(ids);
    }

    /**
     * 删除我的角色信息
     * 
     * @param id 我的角色主键
     * @return 结果
     */
    @Override
    public int deleteSrUserCharacterById(Long id)
    {
        return srUserCharacterMapper.deleteSrUserCharacterById(id);
    }
}
