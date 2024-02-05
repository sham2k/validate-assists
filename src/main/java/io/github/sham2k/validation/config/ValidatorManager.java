package io.github.sham2k.validation.config;

import io.github.sham2k.validation.config.bean.ConstraintDefine;
import io.github.sham2k.validation.util.ClassLoadingHelper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 校验规则及校验器管理器
 * 本类根据校验配置，创建相应的校验注解实例和校验器实例。
 */
public class ValidatorManager
{
    @Setter
    @Getter
    private static ValidatorFactory validatorFactory;

    private static final ThreadLocal<Class<?>[]> validationGroups = new ThreadLocal<>();

    private ValidatorManager()
    {

    }

    /**
     * 执行校验操作。
     * 由于Hibernate validator底层没有暴露当前要校验的组，因此增加此方法替换底层方法，以便获取到要校验的组。
     *
     * @param object 要校验的对象
     * @param groups 要校验的组
     * @param <T>    要校验的对象类型
     * @return 校验结果
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups)
    {
        try {
            setGroups(groups);
            return validatorFactory.getValidator().validate(object, groups);
        }
        finally {
            validationGroups.remove();
        }
    }

    public static Class<?>[] getGroups()
    {
        Class<?>[] groups = validationGroups.get();
        return groups == null ? new Class[0] : groups;
    }

    public static void setGroups(Class<?>... groups)
    {
        if (groups == null || groups.length == 0) {
            validationGroups.remove();
        }
        else {
            validationGroups.set(groups);
        }
    }

    public static <A extends Annotation> ConstraintAnnotationDescriptor<A> build(ConstraintDefine define,
        String defaultPackage)
    {
        ClassLoadingHelper helper = new ClassLoadingHelper(ValidatorManager.class.getClassLoader(),
            Thread.currentThread()
                .getContextClassLoader());
        Class<A> annotationClass = (Class<A>) helper.loadClass(define.getAnnotation(), defaultPackage);
        ConstraintAnnotationDescriptor.Builder<A> builder = new ConstraintAnnotationDescriptor.Builder<>(
            annotationClass);
        if (StringUtils.isNotBlank(define.getMessage())) {
            builder.setMessage(define.getMessage().trim());
        }
        if (define.getGroups() != null && !define.getGroups().getValues().isEmpty()) {
            builder.setGroups(
                define.getGroups().getValues().stream().map(value -> helper.loadClass(value.trim(), defaultPackage)
                ).toList().toArray(new Class<?>[0]));
        }
        if (!(define.getPayloads() == null || define.getPayloads().getValues().isEmpty())) {
            builder.setPayload(
                define.getPayloads().getValues().stream().map(value -> helper.loadClass(value.trim(), defaultPackage)
                ).toList().toArray(new Class<?>[0]));
        }
        if (define.getElements() != null && !define.getElements().isEmpty()) {
            define.getElements().forEach(element -> builder.setAttribute(element.getName(),
                getParameterValue(helper, element.getValues(), annotationClass,
                    element.getName(), "")));
        }
        return builder.build();
    }

    public static <A extends Annotation, T> List<ConstraintValidator<A, T>> getValidator(
        ConstraintAnnotationDescriptor<A> annotationDescriptor, Class<?> valueType)
    {
        Class<A> annotationType = annotationDescriptor.getType();
        List<ConstraintValidatorDescriptor<A>> validatorDescriptors = ConstraintHelper.forAllBuiltinConstraints()
            .findValidatorDescriptors(annotationType, ValidationTarget.ANNOTATED_ELEMENT);
        if (validatorDescriptors == null || validatorDescriptors.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConstraintValidator<A, T>> validators = new ArrayList<>(validatorDescriptors.size());
        validatorDescriptors.forEach(validatorDescriptor -> {
            if (((Class<?>) validatorDescriptor.getValidatedType()).isAssignableFrom(valueType)) {
                ConstraintValidator<A, T> instance = (ConstraintValidator<A, T>) validatorFactory.getConstraintValidatorFactory()
                    .getInstance(
                        validatorDescriptor.getValidatorClass());
                instance.initialize(annotationDescriptor.getAnnotation());
                validators.add(instance);
            }
        });
        return validators;
    }

    private static List<String> removeEmptyContentElements(List<String> params)
    {
        return params.stream().filter(StringUtils::isNotBlank).toList();
    }

    private static Class<?> getAnnotationParameterType(Class<?> annotationClass, String name)
    {
        Method m = run(GetMethod.action(annotationClass, name));
        if (m == null) {
            throw new RuntimeException("No such attribute");
        }
        return m.getReturnType();
    }

    private static Object getParameterValue(ClassLoadingHelper helper, List<String> configParameters,
        Class<?> annotationClass, String name, String defaultPackage)
    {
        List<String> parameters = removeEmptyContentElements(configParameters);
        Class<?> returnType = getAnnotationParameterType(annotationClass, name);
        boolean isArray = returnType.isArray();
        if (!isArray) {
            if (parameters.isEmpty()) {
                return "";
            }
            return convertStringToReturnType(helper, parameters.getFirst(), returnType, defaultPackage);
        }
        else {
            return parameters.stream()
                .map(value -> convertStringToReturnType(helper, value.trim(), returnType.getComponentType(),
                    defaultPackage))
                .toArray(size -> (Object[]) Array.newInstance(returnType.getComponentType(), size));
        }
    }

    private static Object convertStringToReturnType(ClassLoadingHelper helper, String value, Class<?> returnType,
        String defaultPackage)
    {
        try {
            if (returnType == byte.class) {
                return Byte.parseByte(value);
            }
            else if (returnType == short.class) {
                return Short.parseShort(value);
            }
            else if (returnType == int.class) {
                return Integer.parseInt(value);
            }
            else if (returnType == long.class) {
                return Long.parseLong(value);
            }
            else if (returnType == float.class) {
                return Float.parseFloat(value);
            }
            else if (returnType == double.class) {
                return Double.parseDouble(value);
            }
            else if (returnType == boolean.class) {
                return Boolean.parseBoolean(value);
            }
            else if (returnType == char.class) {
                return value.charAt(0);
            }
            else if (returnType == String.class) {
                return value;
            }
            else if (returnType == Class.class) {
                return helper.loadClass(value, defaultPackage);
            }
            else if (Enum.class.isAssignableFrom(returnType)) {
                Class<Enum> enumClass = (Class<Enum>) returnType;
                return Enum.valueOf(enumClass, value);
            }
            else {
                throw new RuntimeException("Unsupported type conversion");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to convert attribute values");
        }
    }

    @IgnoreForbiddenApisErrors(reason = "SecurityManager is deprecated in JDK17")
    private static <T> T run(PrivilegedAction<T> action)
    {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }
}
