package com.github.sham2k.test;

import com.github.sham2k.test.bean.User;
import com.github.sham2k.test.bean.WebReq;
import com.github.sham2k.test.group.SELECT;
import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.config.ValidatorManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestValidator
{
    @BeforeAll static void setUp()
    {
        ConfigManager.loadConfig("config", "validation-cfg");
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        ValidatorManager.setValidatorFactory(validatorFactory);
    }

    @Test
    void testWebReq()
    {
        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);

        WebReq req = new WebReq();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq>> result = ValidatorManager.validate(req, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        }
        else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }
}
