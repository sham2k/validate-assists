package io.github.sham2k.test;

import io.github.sham2k.test.bean.*;
import io.github.sham2k.test.group.SELECT;
import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.config.ValidatorManager;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestValidator
{
    @BeforeAll
    static void setUp()
    {
        ConfigManager.loadConfig("config", "validation-cfg");
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        ValidatorManager.setValidatorFactory(validatorFactory);
    }

    @Test
    void testWebReq1()
    {
        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);

        WebReq1 req = new WebReq1();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq1>> result = ValidatorManager.validate(req, Default.class, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    @Test
    void testWebReq2()
    {
        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);

        WebReq2 req = new WebReq2();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq2>> result = ValidatorManager.validate(req, Default.class, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    @Test
    void testWebReq3()
    {
        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);

        WebReq3 req = new WebReq3();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq3>> result = ValidatorManager.validate(req, Default.class, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    @Test
    void testWebReq4()
    {
        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);

        WebReq4 req = new WebReq4();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq4>> result = ValidatorManager.validate(req, Default.class, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    @Test
    void testWebReq5()
    {
        Map<String, String> subMap = new HashMap<>(3);
        subMap.put("k1", "value1");
        subMap.put("k2", "value2");

        User user = new User();
        user.setUserCode("u001");
        user.setUserName("tomcat");

        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user");
        reqData.put("rows", 5);
        reqData.put("user", user);
        reqData.put("subMap", subMap);

        WebReq4 req = new WebReq4();
        req.setCmdCode("cmd.005");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq4>> result = ValidatorManager.validate(req, Default.class, SELECT.class);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    @Test
    void testSubMap()
    {
        Map<String, String> subMap = new HashMap<>(3);
        subMap.put("k1", "value1");
        subMap.put("k2", "value2");
    }

}
