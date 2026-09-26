package com.quico.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.quico.framework.mybatis.core.handler.AutoFillMetaObjectHandler;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.quico.common.utils.StringUtils;

/**
 * Mybatis支持*匹配扫描包
 *
 * @author ruoyi
 */
@Configuration
public class MyBatisConfig
{
    @Autowired
    private Environment env;

    @Autowired
    private AutoFillMetaObjectHandler autoFillMetaObjectHandler;

    @Autowired(required = false)
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    public static String setTypeAliasesPackage(String typeAliasesPackage)
    {
        ResourcePatternResolver resolver = (ResourcePatternResolver) new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        List<String> allResult = new ArrayList<String>();
        try
        {
            for (String aliasesPackage : typeAliasesPackage.split(","))
            {
                List<String> result = new ArrayList<String>();
                aliasesPackage = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + ClassUtils.convertClassNameToResourcePath(aliasesPackage.trim()) + "/" + DEFAULT_RESOURCE_PATTERN;
                Resource[] resources = resolver.getResources(aliasesPackage);
                if (resources != null && resources.length > 0)
                {
                    MetadataReader metadataReader = null;
                    for (Resource resource : resources)
                    {
                        if (resource.isReadable())
                        {
                            metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            try
                            {
                                result.add(Class.forName(metadataReader.getClassMetadata().getClassName()).getPackage().getName());
                            }
                            catch (ClassNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (result.size() > 0)
                {
                    HashSet<String> hashResult = new HashSet<String>(result);
                    allResult.addAll(hashResult);
                }
            }
            if (allResult.size() > 0)
            {
                typeAliasesPackage = String.join(",", (String[]) allResult.toArray(new String[0]));
            }
            else
            {
                throw new RuntimeException("mybatis typeAliasesPackage 路径扫描错误,参数typeAliasesPackage:" + typeAliasesPackage + "未找到任何包");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return typeAliasesPackage;
    }

    public Resource[] resolveMapperLocations(String[] mapperLocations)
    {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<Resource>();
        if (mapperLocations != null)
        {
            for (String mapperLocation : mapperLocations)
            {
                try
                {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception
    {
        // 优先读取 mybatis-plus 配置，如果不存在则读取 mybatis 配置
        String typeAliasesPackage = env.getProperty("mybatis-plus.type-aliases-package");
        if (StringUtils.isEmpty(typeAliasesPackage)) {
            typeAliasesPackage = env.getProperty("mybatis.typeAliasesPackage");
        }

        String mapperLocations = env.getProperty("mybatis-plus.mapper-locations");
        if (StringUtils.isEmpty(mapperLocations)) {
            mapperLocations = env.getProperty("mybatis.mapperLocations");
        }

        String configLocation = env.getProperty("mybatis-plus.configuration-location");
        if (StringUtils.isEmpty(configLocation)) {
            configLocation = env.getProperty("mybatis.configLocation");
        }

        typeAliasesPackage = setTypeAliasesPackage(typeAliasesPackage);
        VFS.addImplClass(SpringBootVFS.class);

        // 使用 MyBatis-Plus 的 SqlSessionFactory
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
        sessionFactory.setMapperLocations(resolveMapperLocations(StringUtils.split(mapperLocations, ",")));

        // 配置全局策略，包括自动填充处理器
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(autoFillMetaObjectHandler);
        sessionFactory.setGlobalConfig(globalConfig);
        // 注入 MyBatis-Plus 插件（分页插件等）
        if (mybatisPlusInterceptor != null) {
            sessionFactory.setPlugins(mybatisPlusInterceptor);
        }
        if (!StringUtils.isEmpty(configLocation)) {
            sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
        }
        return sessionFactory.getObject();
    }
}
