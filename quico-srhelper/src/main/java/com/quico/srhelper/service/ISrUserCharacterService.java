package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrUserCharacter;

/**
 * 我的角色Service接口
 * 
 * @author quico
 * @date 2026-05-31
 */
public interface ISrUserCharacterService 
{
    /**
     * 查询我的角色
     * 
     * @param id 我的角色主键
     * @return 我的角色
     */
    public SrUserCharacter selectSrUserCharacterById(Long id);

    /**
     * 查询我的角色列表
     * 
     * @param srUserCharacter 我的角色
     * @return 我的角色集合
     */
    public List<SrUserCharacter> selectSrUserCharacterList(SrUserCharacter srUserCharacter);

    /**
     * 新增我的角色
     * 
     * @param srUserCharacter 我的角色
     * @return 结果
     */
    public int insertSrUserCharacter(SrUserCharacter srUserCharacter);

    /**
     * 修改我的角色
     * 
     * @param srUserCharacter 我的角色
     * @return 结果
     */
    public int updateSrUserCharacter(SrUserCharacter srUserCharacter);

    /**
     * 批量删除我的角色
     * 
     * @param ids 需要删除的我的角色主键集合
     * @return 结果
     */
    public int deleteSrUserCharacterByIds(Long[] ids);

    /**
     * 删除我的角色信息
     * 
     * @param id 我的角色主键
     * @return 结果
     */
    public int deleteSrUserCharacterById(Long id);
}
