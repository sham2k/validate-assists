package io.github.sham2k.test.bean;

import lombok.Data;

import java.util.Map;

@Data
public class WebReq1
{
    private String cmdCode;
    private Map<String, Object> reqData;
}
