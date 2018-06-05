package com.codelang.wrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangqi
 * @since 2018/6/5 10:23
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Autowired {
    String name() default "";

}
