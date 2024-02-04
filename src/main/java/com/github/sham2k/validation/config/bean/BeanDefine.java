package com.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象校验配置类
 */
@Data
public class BeanDefine
{
    @JacksonXmlProperty(localName = "class", isAttribute = true)
    private String className;

    @JacksonXmlProperty(localName = "ignore-annotations", isAttribute = true)
    private Boolean ignoreAnnotations = true;

    @JacksonXmlProperty(localName = "field")
    @JacksonXmlElementWrapper(localName = "fields", useWrapping = false)
    private List<FieldDefine> fieldDefines = new ArrayList<>();
}
