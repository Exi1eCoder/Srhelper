package com.quico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        com.fhs.trans.config.TransServiceConfig.class
})
public class QuicoApplication
{
    public static void main(String[] args)
    {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(QuicoApplication.class, args);
        System.out.println("""
                (♥◠‿◠)ﾉﾞ  榷桥商业管理系统启动成功   ლ(´ڡ`ლ)ﾞ
                '    ┏━┛┏━┃┃ ┃┏━┛┃  ┏━┃┏━┛┏━┃
                '    ━━┃┏┏┛┏━┃┏━┛┃  ┏━┛┏━┛┏┏┛
                '    ━━┛┛ ┛┛ ┛━━┛━━┛┛  ━━┛┛ ┛
                """);
    }
}
