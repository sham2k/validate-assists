package io.github.sham2k.validation.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceScanner
{
    public static List<Resource> scanResources(String packageName, String resourceType)
    {
        packageName = packageName.trim();
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        String path = packageName.replace(".", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String locationPattern = "classpath*:" + path + "/**/" + resourceType;
        return scanResources(locationPattern);
    }

    public static List<Resource> scanResources(String locationPattern)
    {
        Resource[] resources = null;
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            resources = resourcePatternResolver.getResources(locationPattern);
        } catch (Exception e) {
            // NOTHING
        }
        if (resources != null) {
            return Arrays.asList(resources);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<Resource> scanResources(String configFileHome, String resourcePattern, String resourceType)
    {
        List<Resource> resources = new ArrayList<>();
        String locationPattern1 = "**/" + resourcePattern + "*" + resourceType; // **/xxx*.xml
        String locationPattern2 = resourcePattern + "/**/*" + resourceType;     // xx/**/*.xml
        // 获取类路径资源
        List<Resource> classResources1 = scanResources("", locationPattern1);
        List<Resource> classResources2 = scanResources("", locationPattern2);
        resources.addAll(classResources1);
        resources.addAll(classResources2);
        // 获取外部配置文件
        if (StringUtils.isBlank(configFileHome)) {
            configFileHome = System.getenv("APP_CFG_HOME");
            if (StringUtils.isBlank(configFileHome)) {
                configFileHome = System.getenv("CONFIG_HOME");
                if (StringUtils.isBlank(configFileHome)) {
                    configFileHome = ".";
                }
            }
        }
        if (!configFileHome.endsWith("/")) {
            configFileHome = configFileHome + "/";
        }
        List<Resource> fileResources1 = scanResources(configFileHome + locationPattern1);
        List<Resource> fileResources2 = scanResources(configFileHome + locationPattern2);
        resources.addAll(fileResources1);
        resources.addAll(fileResources2);
        return resources;
    }
}
