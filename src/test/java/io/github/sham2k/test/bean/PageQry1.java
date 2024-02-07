package io.github.sham2k.test.bean;

import lombok.Data;

import java.util.Map;

/**
 * 使用XML文件配置reqData的约束。
 */
@Data
public class PageQry1
{
    private String cmdCode;
    private Map<String, Object> qryData;
}
