package com.quico.infra.framework.file.config;

import com.quico.infra.framework.file.core.client.FileClientFactory;
import com.quico.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author quico
 */
@Configuration(proxyBeanMethods = false)
public class QuicoFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
