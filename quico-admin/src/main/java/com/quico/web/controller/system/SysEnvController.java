package com.quico.web.controller.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.core.domain.AjaxResult;

/**
 * 环境标识控制器
 */
@RestController
@RequestMapping("/system/env")
public class SysEnvController {

    // 获取当前激活的配置文件（dev, test, prod）
    @Value("${spring.profiles.active:dev}")
    private String activeEnv;

    @GetMapping("/tag")
    public AjaxResult getEnvTag() {
        // 若依框架统一返回 AjaxResult
        return AjaxResult.success("active", activeEnv);
    }
}
