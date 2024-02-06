package io.github.sham2k.validation.validator;

import io.github.sham2k.validation.config.ConfigManager;
import io.github.sham2k.validation.config.bean.BeanDefine;
import io.github.sham2k.validation.config.bean.ConstraintDefine;
import io.github.sham2k.validation.constraints.ValueMap;
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
    /**
     * 验证目标的字段名
     */
    public static final String TARGET_FIELD_NAME = "targetName";
    /**
     * 验证目标的集合名
     */
    public static final String DEFINE_FIELD_NAME = "defineName";

    @Override
    public void initialize(ValueMap constraintAnnotation)
    {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context)
    {
        return isValid(value, null, null, null, context);
    }

    public boolean isValid(Object value, String targetFieldName, String targetDefineName, String targetPathName,
        ConstraintValidatorContext context)
    {
        if (!(context instanceof ConstraintValidatorContextImpl instance)) {
            return true;
        }

        // 获取目标校验对象
        final Object targetValue;
        final String targetName = (targetFieldName == null) ? getRealDefineName(value,
            (String) instance.getConstraintDescriptor().getAttributes().get(TARGET_FIELD_NAME)) : targetFieldName;
        if (StringUtils.isNotBlank(targetName)) {
            targetValue = getProperty(value, targetName);
        }
        else {
            targetValue = value;
        }

        // 获取要使用的校验集合名称
        String defineName = targetDefineName;
        if (defineName == null) {
            defineName = getRealDefineName(value,
                (String) instance.getConstraintDescriptor().getAttributes().get(DEFINE_FIELD_NAME));
            if (StringUtils.isBlank(defineName)) {
                defineName = getRealDefineName(targetValue,
                    (String) instance.getConstraintDescriptor().getAttributes().get(DEFINE_FIELD_NAME));
                if (StringUtils.isBlank(defineName)) {
                    throw new RuntimeException("Validation definition name is not set");
                }
            }
        }
        instance.addMessageParameter(DEFINE_FIELD_NAME, defineName);

        // 获取要使用的校验集合规则定义
        BeanDefine beanDefine = ConfigManager.getConfig(defineName);
        if (beanDefine == null
            || beanDefine.getFieldDefines() == null
            || beanDefine.getFieldDefines().isEmpty()) {
            log.warn("WARN: No validation configuration [{}] defined, ignoring！", defineName);
            return true;
        }

        log.debug("Validate value map using define [{}]", defineName);
        AtomicBoolean result = new AtomicBoolean(true);
        String finalDefineName = defineName;
        beanDefine.getFieldDefines().forEach(fieldDefine -> {
            String fieldName = fieldDefine.getName();
            Object fieldValue = getProperty(targetValue, fieldName);
            if (fieldDefine.getConstraintDefiness() == null || fieldDefine.getConstraintDefiness()
                .isEmpty() || fieldDefine.getValid() != null) {
                // 如没有定义约束或定义引用，则进行嵌套检查。
                // 只有复杂对象才可以定义引用，忽略简单对象的检查引用。
                if (!BeanUtils.isSimpleProperty(fieldValue.getClass()) && fieldDefine.getValid() != null) {
                    if (fieldValue instanceof Map<?, ?> subMap) {
                        // 校验嵌套的Map
                        String subMapDefineName = fieldDefine.getValid().getName();
                        if (StringUtils.isNotBlank(subMapDefineName)) {
                            // 此处，targetFieldName必须是空白串
                            String pathName = getTargetPathName(targetName, targetPathName) + "." + fieldName;
                            boolean checked = isValid(subMap, "", subMapDefineName, pathName, context);
                            if (!checked) {
                                result.set(false);
                            }
                            // 恢复
                            instance.addMessageParameter(DEFINE_FIELD_NAME, finalDefineName);
                        }
                    }
                    else {
                        // 校验其他实例
                        Set<ConstraintViolation<Object>> errors = ValidatorManager.validate(fieldValue,
                            ValidatorManager.getGroups());
                        log.trace("Call HibernateValidator to validate field [{}] value [{}] --> [{}]",
                            fieldName,
                            fieldValue,
                            errors.isEmpty());
                        if (!errors.isEmpty()) {
                            result.set(false);
                            // 添加错误信息参数
                            errors.forEach(error -> {
                                String pathName = getTargetPathName(targetName, targetPathName);
                                if (StringUtils.isNotBlank(pathName)) {
                                    context.buildConstraintViolationWithTemplate(error.getMessage())
                                        .addPropertyNode(pathName).addPropertyNode(fieldName)
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
            }
            else {
                // 如定义了约束，则使用约束进行验证。
                fieldDefine.getConstraintDefiness().forEach(constraintDefine -> {
                    ConstraintAnnotationDescriptor<Annotation> annotationDescriptor = constraintDefine.getAnnotationDescriptor();
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
                                    String pathName = getTargetPathName(targetName, targetPathName);
                                    if (StringUtils.isNotBlank(pathName)) {
                                        context.buildConstraintViolationWithTemplate(getMessage(constraintDefine))
                                            .addPropertyNode(pathName).addPropertyNode(fieldName)
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
        ConstraintAnnotationDescriptor<? extends Annotation> annotationDescriptor = constraintDefine.getAnnotationDescriptor();
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = annotationDescriptor.getMessage();
            if (StringUtils.isBlank(errorMessage)) {
                errorMessage = (String) invokeMethod(annotationDescriptor.getAnnotation(), "message");
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

    /**
     * 返回当前字段的路径名，以便生成正确的错误信息。
     *
     * @param targetName     使用的目标字段名
     * @param targetPathName 传递的目标字段路径名
     * @return 字段路径
     */
    private String getTargetPathName(String targetName, String targetPathName)
    {
        if (StringUtils.isNotBlank(targetPathName)) {
            return targetPathName;
        }
        else {
            return StringUtils.isNotBlank(targetName) ? targetName : "";
        }
    }
}
