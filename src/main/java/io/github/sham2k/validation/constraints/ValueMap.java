package io.github.sham2k.validation.constraints;

import io.github.sham2k.validation.validator.ValueMapValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ValueMapValidator.class })
public @interface ValueMap
{
    // 错误信息
    String message() default "{valueMapValidateFail}";

    // 所属组
    Class<?>[] groups() default {};

    // 附件数据
    Class<? extends Payload>[] payload() default {};

    // 要校验的属性名（如没指定，为本对象，否则为本对象的属性）
    String targetName() default "";

    // 校验配置组名称
    String defineName() default "";

    /**
     * Defines several {@code @ValueMap} annotations on the same element.
     */
    @Target({ TYPE, FIELD })
    @Retention(RUNTIME)
    @Documented
    public @interface List
    {
        ValueMap[] value();
    }
}
