package com.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验文件配置类
 */
@Data
@JacksonXmlRootElement(localName = "constraint-mappings")
public class FileDefine
{
    @JacksonXmlProperty(localName = "bean")
    @JacksonXmlElementWrapper(localName = "beans", useWrapping = false)
    private List<BeanDefine> beanDefines = new ArrayList<>();
}

