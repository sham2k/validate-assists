package io.github.sham2k.test;

import io.github.sham2k.test.bean.*;
import io.github.sham2k.test.group.SELECT;
import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.validator.ValidatorManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestValidator
{
    /**
     * 初始化
     */
    @BeforeAll
    static void setUp()
    {
        ConfigManager.loadConfig("config", "validation-cfg");
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        ValidatorManager.setValidatorFactory(validatorFactory);
    }

    /**
     * XML模式： 字段配置 + 固定约束集合名
     */
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

    /**
     * XML模式： 类配置 + 动态约束集合名
     */
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

    /**
     * 注解模式： 字段配置 + 固定约束集合名
     */
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

    /**
     * 注解模式： 类配置 + 动态约束集合名
     */
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

    /**
     * 直接校验普通对象
     */
    @Test
    void testWebReq5()
    {
        Map<String, Object> reqData = HashMap.newHashMap(4);
        reqData.put("table", "t_user1");
        reqData.put("rows", 5);

        WebReq5 req = new WebReq5();
        req.setCmdCode("cmd.001");
        req.setReqData(reqData);

        Set<ConstraintViolation<WebReq5>> result = ValidatorManager.validate(req, "reqData", "${cmdCode}");
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }

    /**
     * 注解模式：类注解 + 动态约束集合名 + 校验嵌套 Map
     */
    @Test
    void testSubMap1()
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

    /**
     * 直接校验Map实例
     */
    @Test
    void testSubMap2()
    {
        Map<String, String> subMap = new HashMap<>(3);
        subMap.put("k1", "value1");
        subMap.put("k2", "value2");

        Set<ConstraintViolation<Map<String, String>>> result = ValidatorManager.validate(subMap, null, "sub.001");
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(System.out::println);
        }
    }
}
