package com.github.sham2k.validation.validator;

import com.github.sham2k.validation.config.ConfigManager;
import com.github.sham2k.validation.config.ValidatorManager;
import com.github.sham2k.validation.config.bean.BeanDefine;
import com.github.sham2k.validation.config.bean.ConstraintDefine;
import com.github.sham2k.validation.constraints.ValueMap;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ValueMapValidator implements ConstraintValidator<ValueMap, Object>
{
    @Override public void initialize(ValueMap constraintAnnotation)
    {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context)
    {
        if (!(context instanceof ConstraintValidatorContextImpl instance)) {
            return true;
        }

        // 获取校验对象
        final Object targetValue;
        String targetName = getRealDefineName(value,
            (String) instance.getConstraintDescriptor().getAttributes().get("targetName"));
        if (StringUtils.isNotBlank(targetName)) {
            targetValue = getProperty(value, targetName);
        }
        else {
            targetValue = value;
        }

        // 获取校验定义
        String defineName = getRealDefineName(value,
            (String) instance.getConstraintDescriptor().getAttributes().get("defineName"));
        if (StringUtils.isBlank(defineName)) {
            defineName = getRealDefineName(targetValue,
                (String) instance.getConstraintDescriptor().getAttributes().get("defineName"));
            if (StringUtils.isBlank(defineName)) {
                throw new RuntimeException("Validation definition name is not set");
            }
        }
        instance.addMessageParameter("defineName", defineName);

        // 获取校验规则
        BeanDefine beanDefine = ConfigManager.getConfig(defineName);
        if (beanDefine == null
            || beanDefine.getFieldDefines() == null
            || beanDefine.getFieldDefines().isEmpty()) {
            log.warn("WARN: No validation configuration [{}] defined, ignoring！", defineName);
            return true;
        }

        AtomicBoolean result = new AtomicBoolean(true);
        log.debug("Validate value map using define [{}]", defineName);
        beanDefine.getFieldDefines().forEach(fieldDefine -> {
            String fieldName = fieldDefine.getName();
            Object fieldValue = getProperty(targetValue, fieldName);
            if (fieldDefine.getConstraintDefiness() == null || fieldDefine.getConstraintDefiness().isEmpty()) {
                // 如没有定义约束，则检查是否定义了引用。
                // 只有复杂对象才可以定义引用。
                if (!BeanUtils.isSimpleProperty(fieldValue.getClass()) && fieldDefine.getValid() != null) {
                    Set<ConstraintViolation<Object>> errors = ValidatorManager.validate(fieldValue,
                        ValidatorManager.getGroups());
                    if (!errors.isEmpty()) {
                        result.set(false);
                        // 添加错误信息参数
                        errors.forEach(error -> {
                            if (StringUtils.isNotBlank(targetName)) {
                                context.buildConstraintViolationWithTemplate(error.getMessage())
                                    .addPropertyNode(targetName).addPropertyNode(fieldName)
                                    .addPropertyNode(error.getPropertyPath().toString())
                                    .addConstraintViolation();
                            }
                            else {
                                context.buildConstraintViolationWithTemplate(error.getMessage())
                                    .addPropertyNode(fieldName).addPropertyNode(error.getPropertyPath().toString())
                                    .addConstraintViolation();
                            }
                        });
                    }
                }
            }
            else {
                // 如定义了约束，则使用约束进行验证。
                fieldDefine.getConstraintDefiness().forEach(constraintDefine -> {
                    ConstraintAnnotationDescriptor<Annotation> annotationDescriptor = (ConstraintAnnotationDescriptor<Annotation>) constraintDefine.getAnnotationDescriptor();
                    if (enabledGroup(annotationDescriptor.getGroups())) {
                        List<ConstraintValidator<Annotation, Object>> validators = ValidatorManager.getValidator(
                            annotationDescriptor, fieldValue.getClass());
                        if (!validators.isEmpty()) {
                            validators.forEach(validator -> {
                                boolean checked = validator.isValid(fieldValue, context);
                                log.trace("Validator [{}] validate field [{}] value [{}] --> [{}]",
                                    validator.getClass().getSimpleName(),
                                    fieldName,
                                    fieldValue,
                                    checked);
                                if (!checked) {
                                    result.set(false);
                                    // 添加错误信息参数
                                    if (!(annotationDescriptor.getAttributes() == null || annotationDescriptor.getAttributes()
                                        .isEmpty())) {
                                        annotationDescriptor.getAttributes().forEach(instance::addMessageParameter);
                                    }
                                    // 添加错误信息
                                    if (StringUtils.isNotBlank(targetName)) {
                                        context.buildConstraintViolationWithTemplate(getMessage(constraintDefine))
                                            .addPropertyNode(targetName).addPropertyNode(fieldName)
                                            .addConstraintViolation();
                                    }
                                    else {
                                        context.buildConstraintViolationWithTemplate(getMessage(constraintDefine))
                                            .addPropertyNode(fieldName).addConstraintViolation();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        return result.get();
    }

    private String getMessage(ConstraintDefine constraintDefine)
    {
        String errorMessage = constraintDefine.getMessage();
        ConstraintAnnotationDescriptor<Annotation> annotationDescriptor = (ConstraintAnnotationDescriptor<Annotation>) constraintDefine.getAnnotationDescriptor();
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = annotationDescriptor.getMessage();
            if (StringUtils.isBlank(errorMessage)) {
                errorMessage = (String) invokeMethod(annotationDescriptor.getAnnotation(), "message", new Object[0]);
                if (StringUtils.isBlank(errorMessage)) {
                    errorMessage = annotationDescriptor.getType()
                        .getSimpleName() + " validate failed";
                }
            }
        }
        return errorMessage;
    }

    /**
     * 根据属性值，替换变量形式的定义名。
     *
     * @param defineName 原始定义名
     * @return 真实定义名
     */
    private String getRealDefineName(Object value, String defineName)
    {
        if (StringUtils.isBlank(defineName) || !defineName.startsWith("${") || !defineName.endsWith("}")) {
            return defineName;
        }
        defineName = defineName.substring(2, defineName.length() - 1);
        if (value instanceof Map<?, ?> valueMap) {
            return (String) valueMap.get(defineName);
        }
        else {
            return (String) getProperty(value, defineName);
        }
    }

    private Object getProperty(Object bean, String propertyName)
    {
        if (bean instanceof Map<?, ?> valueMap) {
            return valueMap.get(propertyName);
        }
        else {
            try {
                return PropertyUtils.getProperty(bean, propertyName);
            }
            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to obtain object property values", e);
            }
        }
    }

    private Object invokeMethod(Object bean, String methodName, Object... args)
    {
        try {
            return MethodUtils.invokeMethod(bean, methodName, args);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke object method " + methodName, e);
        }
    }

    private boolean enabledGroup(Class<?>[] groupClasses)
    {
        Class<?>[] groups = ValidatorManager.getGroups();
        if (groups.length == 0 && (groupClasses == null || groupClasses.length == 0)) {
            // Both using default
            return true;
        }
        else if (groups.length == 0) {
            // Validate using default
            for (Class<?> group2 : groupClasses) {
                if (Default.class.isAssignableFrom(group2)) {
                    return true;
                }
            }
        }
        else if (groupClasses == null || groupClasses.length == 0) {
            // Constraints using default
            for (Class<?> group1 : groups) {
                if (Default.class.isAssignableFrom(group1)) {
                    return true;
                }
            }
        }
        else {
            // Both not using default
            for (Class<?> group1 : groups) {
                for (Class<?> group2 : groupClasses) {
                    if (group1.isAssignableFrom(group2) || group2.isAssignableFrom(group1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
