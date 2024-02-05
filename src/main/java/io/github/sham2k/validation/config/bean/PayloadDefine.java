package io.github.sham2k.validation.config.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

/**
 * 校验规则所属附加数据
 */
@Data
public class PayloadDefine
{
    @JacksonXmlProperty(localName = "value")
    @JacksonXmlElementWrapper(localName = "values", useWrapping = false)
    private List<String> values;
}
