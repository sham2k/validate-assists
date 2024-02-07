package io.github.sham2k.validation.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.sham2k.validation.config.bean.BeanDefine;
import io.github.sham2k.validation.config.bean.FileDefine;
import io.github.sham2k.validation.util.ResourceScanner;
import io.github.sham2k.validation.validator.ValidatorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理类，负责读取校验配置文件，存储校验相关配置。
 */
@Slf4j
public class ConfigManager
{
    private static final Map<String, BeanDefine> registry = new ConcurrentHashMap<>(50);

    private ConfigManager()
    {

    }

    public static void loadConfig(String homeName, String filePattern)
    {
        List<Resource> resources = ResourceScanner.scanResources(homeName, filePattern, ".xml");
        if (resources.isEmpty()) {
            log.warn("**** CAN NOT FOUND ANY VALIDATION FILE!");
        }

        XmlMapper mapper = getMapper();
        resources.forEach(resource -> {
            if (resource.exists() && resource.isFile() && resource.isReadable()) {
                try (InputStream is = resource.getInputStream()) {
                    readConfig(mapper, is);
                }
                catch (Exception e) {
                    log.error("Failed to read validation file [" + resource.getFilename() + "]", e);
                    throw new RuntimeException(e);
                }
            }
        });
        log.debug("**** [{}] registered", registry.size());
    }

    public static void loadConfig(String fileName)
    {
        XmlMapper mapper = getMapper();
        try (InputStream is = new FileInputStream(fileName)) {
            readConfig(mapper, is);
        }
        catch (Exception e) {
            log.error("Failed to read validation file " + fileName, e);
            throw new RuntimeException(e);
        }
    }

    private static void readConfig(XmlMapper mapper, InputStream is)
    {
        try {
            FileDefine define = mapper.readValue(is, FileDefine.class);
            define.getBeanDefines().forEach(beanDefine -> {
                beanDefine.getFieldDefines()
                    .forEach(fieldDefine -> fieldDefine.getConstraintDefiness().forEach(constraintDefine -> {
                        constraintDefine.setAnnotationDescriptor(ValidatorManager.build(constraintDefine, ""));
                    }));
                registry.put(beanDefine.getClassName(), beanDefine);
                log.debug("**** Register bean validation definition: [{}]", beanDefine.getClassName());
            });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象校验定义。
     *
     * @param beanName 校验类名
     * @return 校验定义
     */
    public static BeanDefine getConfig(String beanName)
    {
        return registry.get(beanName);
    }

    private static XmlMapper getMapper()
    {
        XmlMapper mapper = new XmlMapper();
        SimpleModule simpleModule = new SimpleModule().addDeserializer(String.class, new AutoTrimStringDeserializer());
        mapper.registerModule(simpleModule);
        return mapper;
    }

    /**
     * 本类解析数据时，自动剪裁数据项前后空格。
     */
    public static class AutoTrimStringDeserializer extends com.fasterxml.jackson.databind.deser.std.StringDeserializer
    {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String value = super.deserialize(p, ctxt);
            return value != null ? value.trim() : null;
        }
    }
}
