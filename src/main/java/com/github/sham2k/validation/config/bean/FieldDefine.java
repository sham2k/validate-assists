package com.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段校验配置类
 */
@Data
public class FieldDefine
{
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "valid")
    private ReferenceDefine valid;

    @JacksonXmlProperty(localName = "constraint")
    @JacksonXmlElementWrapper(localName = "constrains", useWrapping = false)
    private List<ConstraintDefine> constraintDefiness = new ArrayList<>();
}
