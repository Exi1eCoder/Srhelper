package com.quico.srhelper.mapper;

import com.quico.srhelper.domain.SrUserUid;

/**
 * 用户与游戏账号绑定Mapper接口
 *
 * @author quico
 */
public interface SrUserUidMapper
{
    /**
     * 按游戏账号UID查询绑定关系
     *
     * @param uid 游戏内账号UID
     * @return 绑定关系，未绑定返回 null
     */
    SrUserUid selectByUid(String uid);

    /**
     * 统计指定系统用户已绑定的游戏账号数量
     *
     * @param userId 系统用户ID
     * @return 已绑定数量
     */
    long countByUserId(Long userId);

    /**
     * 新增绑定关系
     *
     * uid 唯一索引冲突时抛出 DuplicateKeyException，由调用方处理并发抢注
     *
     * @param userUid 绑定关系
     * @return 结果
     */
    int insertSrUserUid(SrUserUid userUid);
}
