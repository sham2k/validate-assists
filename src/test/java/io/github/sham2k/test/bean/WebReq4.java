package io.github.sham2k.test.bean;

import io.github.sham2k.validation.constraints.ValueMap;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Map;

@Data
@ValueMap(defineName = "${cmdCode}", targetName = "reqData")
public class WebReq4
{
    @NotNull
    @Length(min = 7, max = 7, message = "cmdCode长度应是xxx.nnn格式！")
    private String cmdCode;

    private Map<String, Object> reqData;
}
