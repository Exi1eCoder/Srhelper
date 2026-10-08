package com.quico.srhelper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * srhelper 模块定时任务开关
 * 用于抽卡统计异步重算等 @Scheduled 任务
 *
 * @author quico
 */
@Configuration
@EnableScheduling
public class SrhelperSchedulingConfig
{
}
