package com.wp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 翁鹏
 * 字段注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 字段名
     */
    String value();

    /**
     * 是否为主键
     */
    boolean isPrimaryKey() default false;
}
