package com.quico.infra.dal.dataobject.signin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签到奖励规则
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuicoSignInRewardDO {

    private Integer id;
    /** 连续签到天数 */
    private Integer continuousDays;
    /** 奖励积分 */
    private Integer rewardPoints;
    /** 奖励描述 */
    private String rewardDesc;
}
