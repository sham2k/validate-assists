package io.github.sham2k.test;

import io.github.sham2k.test.bean.*;
import io.github.sham2k.test.group.SELECT;
import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.validator.ValidatorManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry1 pageQry1 = new PageQry1();
        pageQry1.setCmdCode("cmd.001");
        pageQry1.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry1>> result = ValidatorManager.validate(pageQry1);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * XML模式： 类配置 + 动态约束集合名
     */
    @Test
    void testWebReq2()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry2 pageQry2 = new PageQry2();
        pageQry2.setCmdCode("cmd.001");
        pageQry2.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry2>> result = ValidatorManager.validate(pageQry2);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * 注解模式： 字段配置 + 固定约束集合名
     */
    @Test
    void testWebReq3()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry3 pageQry3 = new PageQry3();
        pageQry3.setCmdCode("cmd.001");
        pageQry3.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry3>> result = ValidatorManager.validate(pageQry3);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * 注解模式： 类配置 + 动态约束集合名
     */
    @Test
    void testWebReq4()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry4 pageQry4 = new PageQry4();
        pageQry4.setCmdCode("cmd.001");
        pageQry4.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry4>> result = ValidatorManager.validate(pageQry4);
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * 直接校验Bean
     */
    @Test
    void testWebReq5()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry5 pageQry5 = new PageQry5();
        pageQry5.setCmdCode("cmd.001");
        pageQry5.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry5>> result = ValidatorManager.validate(pageQry5, null, "pageQry.005");
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * 直接校验Map实例
     */
    @Test
    void testQryData1()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        Set<ConstraintViolation<Map<String, Object>>> result = ValidatorManager.validate(qryData, null, "cmd.001");
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }

    /**
     * 直接校验Bean：使用target获取值
     */
    @Test
    void testQryData2()
    {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(123);
        pageInfo.setPageSize(101); // error
        pageInfo.setOrderBy("abcdeabcde1234567"); // error

        Map<String, Object> whereBy = HashMap.newHashMap(2);
        whereBy.put("userCode", "user00001"); // error
        whereBy.put("userName", "tomcatAndJerry");

        Map<String, Object> qryData = HashMap.newHashMap(2);
        qryData.put("pageInfo", pageInfo);
        qryData.put("table", "t_user"); // error
        qryData.put("whereBy", whereBy);

        PageQry5 pageQry5 = new PageQry5();
        pageQry5.setCmdCode("cmd.001");
        pageQry5.setQryData(qryData);    // error

        Set<ConstraintViolation<PageQry5>> result = ValidatorManager.validate(pageQry5, "qryData", "${cmdCode}");
        if (result.isEmpty()) {
            System.out.println("**** PASS");
        } else {
            System.out.println("**** FAIL");
            result.forEach(error -> {
                System.out.println(error);
                assertTrue("qryData qryData.table qryData.pageInfo.pageSize qryData.pageInfo.orderBy qryData.whereBy.userCode".contains(error.getPropertyPath().toString()));
            });
        }
        assertEquals(5, result.size());
    }
}
