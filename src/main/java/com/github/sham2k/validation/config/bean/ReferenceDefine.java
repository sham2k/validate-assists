package com.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * 校验规则引用配置。
 * 对Map中包含的Map对象，使用此属性指示此Map对象引用的校验配置。
 */
@Data
public class ReferenceDefine
{
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
}
