package com.quico.infra.controller.admin.signin.vo;

import lombok.Data;

import java.util.List;

/**
 * 签到结果 VO
 */
@Data
public class SignInResultVO {

    private Boolean success;
    private String message;
    private Integer points;
    private Integer continuousDays;
    private List<String> rewards;
}
