package com.inotee.swarmbuy.common.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 与校验器IsMobileValidator绑定 ，校验器需实现ConstraintValidator<IsMobile,String>接口 ，其中第一个泛型为注解，第二个为需要校验的参数的类型
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class })
public @interface  IsMobile {

    boolean required() default true;

    String message() default "手机号码格式错误";  //如果校验不通过 提示什么信息

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
