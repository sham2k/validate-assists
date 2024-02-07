package io.github.sham2k.test.bean;

import lombok.Data;

import java.util.Map;

/**
 * 使用本工具配置整个类约束。
 */
@Data
public class PageQry5
{
    private String cmdCode;
    private Map<String, Object> qryData;
}
