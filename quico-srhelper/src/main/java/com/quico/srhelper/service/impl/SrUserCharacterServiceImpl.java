package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.exception.ServiceException;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrUserCharacterMapper;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.domain.enums.CharacterPathEnum;
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

    @Autowired
    private SrCharacterMapper srCharacterMapper;

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
        normalizePathLevels(srUserCharacter);
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
        normalizePathLevels(srUserCharacter);
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

    /**
     * 按命途处理命途特有等级字段（忆灵技能/忆灵天赋/欢愉技）
     * - 普通角色：三个字段强制重置为默认值1，防止前端传脏数据
     * - 记忆命途：校验忆灵技能(1-6)、忆灵天赋(1-6)，欢愉技重置为1
     * - 欢愉命途：校验欢愉技(1-10)，忆灵技能/忆灵天赋重置为1
     */
    private void normalizePathLevels(SrUserCharacter uc)
    {
        if (uc.getCharacterId() == null)
        {
            return;
        }
        SrCharacter character = srCharacterMapper.selectSrCharacterById(uc.getCharacterId());
        CharacterPathEnum path = CharacterPathEnum.fromPath(character != null ? character.getPath() : null);

        if (path.hasMemosprite())
        {
            uc.setMemospriteskillLevel(validateLevel(uc.getMemospriteskillLevel(), path.getMaxMemospriteskillLevel(), "忆灵技能"));
            uc.setMemospritetalentLevel(validateLevel(uc.getMemospritetalentLevel(), path.getMaxMemospritetalentLevel(), "忆灵天赋"));
        }
        else
        {
            uc.setMemospriteskillLevel((long) CharacterPathEnum.DEFAULT_LEVEL);
            uc.setMemospritetalentLevel((long) CharacterPathEnum.DEFAULT_LEVEL);
        }

        if (path.hasElationSkill())
        {
            uc.setElationskillLevel(validateLevel(uc.getElationskillLevel(), path.getMaxElationskillLevel(), "欢愉技"));
        }
        else
        {
            uc.setElationskillLevel((long) CharacterPathEnum.DEFAULT_LEVEL);
        }
    }

    /**
     * 校验命途特有维度等级，空值取默认值1，超出范围抛出业务异常
     */
    private Long validateLevel(Long value, int maxLevel, String dimensionName)
    {
        long level = value != null ? value : CharacterPathEnum.DEFAULT_LEVEL;
        if (level < 1 || level > maxLevel)
        {
            throw new ServiceException(dimensionName + "等级必须在1-" + maxLevel + "之间");
        }
        return level;
    }
}
