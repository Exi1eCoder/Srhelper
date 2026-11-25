package com.quico.srhelper.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.domain.vo.SrUserCharacterOptionVO;

/**
 * 我的角色Mapper接口
 * 
 * @author quico
 * @date 2026-05-31
 */
public interface SrUserCharacterMapper 
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
     * 查询我的角色下拉选项
     * 
     * @param userId 用户ID
     * @return 下拉选项集合
     */
    public List<SrUserCharacterOptionVO> selectSrUserCharacterOptions(@Param("userId") Long userId);

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
     * 删除我的角色
     * 
     * @param id 我的角色主键
     * @return 结果
     */
    public int deleteSrUserCharacterById(Long id);

    /**
     * 批量删除我的角色
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrUserCharacterByIds(Long[] ids);
}
