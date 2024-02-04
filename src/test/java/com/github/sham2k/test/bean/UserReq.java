package com.github.sham2k.test.bean;

import com.github.sham2k.validation.constraints.ValueMap;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Map;

@Data
public class UserReq
{
    @NotNull
    @Length(min = 7, max = 7, message = "cmdCode长度应是xxx.nnn格式！")
    private String cmdCode;

    @ValueMap(defineName = "cmd.001")
    private Map<String, Object> reqData;
}
