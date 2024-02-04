package com.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

/**
 * 校验规则所属组配置
 */
@Data
public class GroupDefine
{
    @JacksonXmlProperty(localName = "value")
    @JacksonXmlElementWrapper(localName = "values", useWrapping = false)
    private List<String> values;
}
