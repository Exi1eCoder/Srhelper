package com.quico.infra.service;

import com.quico.common.core.domain.AjaxResult;

/**
 * 签到核心服务接口（Redis Bitmap + MySQL）
 */
public interface ISignInService {

    /**
     * 用户签到
     */
    AjaxResult signIn(Long userId);

    /**
     * 获取签到状态（支持指定年月查历史）
     */
    AjaxResult getSignInStatus(Long userId, Integer year, Integer month);

    /**
     * 补签
     */
    AjaxResult repairSignIn(Long userId, String date);
}
