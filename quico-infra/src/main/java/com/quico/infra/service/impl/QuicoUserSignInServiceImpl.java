package com.quico.infra.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.infra.mapper.QuicoUserSignInMapper;
import com.quico.infra.domain.QuicoUserSignIn;
import com.quico.infra.service.IQuicoUserSignInService;

/**
 * 用户签到记录Service业务层处理
 * 
 * @author quico
 * @date 2026-08-03
 */
@Service
public class QuicoUserSignInServiceImpl implements IQuicoUserSignInService 
{
    @Autowired
    private QuicoUserSignInMapper quicoUserSignInMapper;

    /**
     * 查询用户签到记录
     * 
     * @param id 用户签到记录主键
     * @return 用户签到记录
     */
    @Override
    public QuicoUserSignIn selectQuicoUserSignInById(Long id)
    {
        return quicoUserSignInMapper.selectQuicoUserSignInById(id);
    }

    /**
     * 查询用户签到记录列表
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 用户签到记录
     */
    @Override
    public List<QuicoUserSignIn> selectQuicoUserSignInList(QuicoUserSignIn quicoUserSignIn)
    {
        return quicoUserSignInMapper.selectQuicoUserSignInList(quicoUserSignIn);
    }

    /**
     * 新增用户签到记录
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 结果
     */
    @Override
    public int insertQuicoUserSignIn(QuicoUserSignIn quicoUserSignIn)
    {
        return quicoUserSignInMapper.insertQuicoUserSignIn(quicoUserSignIn);
    }

    /**
     * 修改用户签到记录
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 结果
     */
    @Override
    public int updateQuicoUserSignIn(QuicoUserSignIn quicoUserSignIn)
    {
        return quicoUserSignInMapper.updateQuicoUserSignIn(quicoUserSignIn);
    }

    /**
     * 批量删除用户签到记录
     * 
     * @param ids 需要删除的用户签到记录主键
     * @return 结果
     */
    @Override
    public int deleteQuicoUserSignInByIds(Long[] ids)
    {
        return quicoUserSignInMapper.deleteQuicoUserSignInByIds(ids);
    }

    /**
     * 删除用户签到记录信息
     * 
     * @param id 用户签到记录主键
     * @return 结果
     */
    @Override
    public int deleteQuicoUserSignInById(Long id)
    {
        return quicoUserSignInMapper.deleteQuicoUserSignInById(id);
    }
}
