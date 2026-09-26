package com.quico.infra.service;

import java.util.List;
import com.quico.infra.domain.QuicoUserSignIn;

/**
 * 用户签到记录Service接口
 * 
 * @author quico
 * @date 2026-08-03
 */
public interface IQuicoUserSignInService 
{
    /**
     * 查询用户签到记录
     * 
     * @param id 用户签到记录主键
     * @return 用户签到记录
     */
    public QuicoUserSignIn selectQuicoUserSignInById(Long id);

    /**
     * 查询用户签到记录列表
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 用户签到记录集合
     */
    public List<QuicoUserSignIn> selectQuicoUserSignInList(QuicoUserSignIn quicoUserSignIn);

    /**
     * 新增用户签到记录
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 结果
     */
    public int insertQuicoUserSignIn(QuicoUserSignIn quicoUserSignIn);

    /**
     * 修改用户签到记录
     * 
     * @param quicoUserSignIn 用户签到记录
     * @return 结果
     */
    public int updateQuicoUserSignIn(QuicoUserSignIn quicoUserSignIn);

    /**
     * 批量删除用户签到记录
     * 
     * @param ids 需要删除的用户签到记录主键集合
     * @return 结果
     */
    public int deleteQuicoUserSignInByIds(Long[] ids);

    /**
     * 删除用户签到记录信息
     * 
     * @param id 用户签到记录主键
     * @return 结果
     */
    public int deleteQuicoUserSignInById(Long id);
}
