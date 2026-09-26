package com.quico.infra.dal.dataobject.signin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuicoUserSignInDO {

    private Long id;
    /** 用户ID */
    private Long userId;
    /** 签到日期 */
    private LocalDate signDate;
    /** 签到时间 */
    private LocalDateTime signTime;
    /** 是否补签 0否 1是 */
    private Integer isRepaired;
}
