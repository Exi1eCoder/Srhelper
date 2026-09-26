package com.quico.infra.controller.admin.signin.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 签到状态 VO
 */
@Data
public class SignInStatusVO {

    private Long userId;
    private String month;
    private List<LocalDate> signedDates;
    private Boolean todaySigned;
    private Integer continuousDays;
    private LocalDateTime todaySignTime;
    private Integer todayPoints;
}
