package io.github.sham2k.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.config.bean.*;
import io.github.sham2k.validation.util.ResourceScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestConfigManager
{
    @Test
    void testRead() throws IOException
    {
        ConfigManager.loadConfig("", "validation-cfg");
        BeanDefine beanDefine = ConfigManager.getConfig("cmd.001");
        System.out.println(beanDefine);
    }

    @Test
    void testWrite() throws JsonProcessingException
    {
        List<String> groups = new ArrayList<>(2);
        groups.add("group1");
        groups.add("group2");
        GroupDefine groupDefine = new GroupDefine();
        groupDefine.setValues(groups);

        List<String> payloads = new ArrayList<>(2);
        payloads.add("payload1");
        payloads.add("payload2");
        PayloadDefine payloadDefine = new PayloadDefine();
        payloadDefine.setValues(payloads);

        List<String> minValues = new ArrayList<>(1);
        minValues.add("3");
        List<String> maxValues = new ArrayList<>(1);
        maxValues.add("6");

        List<ParameterDefine> elements = new ArrayList<>(2);
        ParameterDefine parameter1 = new ParameterDefine();
        parameter1.setName("min");
        parameter1.setValues(minValues);
        ParameterDefine parameter2 = new ParameterDefine();
        parameter2.setName("max");
        parameter2.setValues(maxValues);
        elements.add(parameter1);
        elements.add(parameter2);

        List<ConstraintDefine> constraints = new ArrayList<>(2);
        ConstraintDefine constraintDefine = new ConstraintDefine();
        constraintDefine.setAnnotation("annotation");
        constraintDefine.setMessage("message");
        constraintDefine.setGroups(groupDefine);
        constraintDefine.setPayloads(payloadDefine);
        constraintDefine.setElements(elements);
        constraints.add(constraintDefine);

        List<FieldDefine> fieldDefines = new ArrayList<>(2);
        FieldDefine fieldDefine = new FieldDefine();
        fieldDefine.setName("userCode");
        fieldDefine.setConstraintDefiness(constraints);
        fieldDefines.add(fieldDefine);

        List<BeanDefine> beans = new ArrayList<>(10);
        BeanDefine beanDefine = new BeanDefine();
        beanDefine.setClassName("cmd.001");
        beanDefine.setFieldDefines(fieldDefines);
        beans.add(beanDefine);

        FileDefine define = new FileDefine();
        define.setBeanDefines(beans);

        XmlMapper.builder().defaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
        String str = mapper.writeValueAsString(define);
        System.out.println(str);
    }

    @Test
    void testScanner()
    {
        List<Resource> resources = ResourceScanner.scanResources("config", "validation-cfg", "*.xml");
        resources.forEach(resource -> {
            try {
                System.out.println(resource.getFile().getName());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
