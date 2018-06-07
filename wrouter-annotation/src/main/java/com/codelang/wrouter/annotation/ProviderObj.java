package com.codelang.wrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangqi
 * @since 2018/6/7 11:04
 * <p>
 * 向外提供对象  比如View对象或是Fragment对象
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ProviderObj {
    String path();
}
