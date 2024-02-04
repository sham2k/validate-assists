package com.github.sham2k.test.bean;

import lombok.Data;

import java.util.Map;

@Data
public class WebReq
{
    private String cmdCode;
    private Map<String, Object> reqData;
}
