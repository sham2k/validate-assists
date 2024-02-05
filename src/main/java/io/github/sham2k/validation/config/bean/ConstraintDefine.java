package io.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import java.util.List;

/**
 * 字段校验规则配置类
 */
@Data
public class ConstraintDefine
{
    @JacksonXmlProperty(localName = "annotation", isAttribute = true)
    private String annotation;

    @JacksonXmlProperty(localName = "message")
    private String message;

    @JacksonXmlProperty(localName = "groups")
    private GroupDefine groups;

    @JacksonXmlProperty(localName = "payload")
    private PayloadDefine payloads;

    @JacksonXmlProperty(localName = "element")
    @JacksonXmlElementWrapper(localName = "elements", useWrapping = false)
    private List<ParameterDefine> elements;

    /**
     * 校验规则元数据
     */
    @JsonIgnore
    private ConstraintAnnotationDescriptor<?> annotationDescriptor;
}
