package com.lnsoft.annotation;

import java.lang.annotation.*;

/**
 * Created By Chr on 2019/1/24/0024.
 */
@Target({ElementType.FIELD})   //只能在类的成员变量上使用
@Retention(RetentionPolicy.RUNTIME) //表示运行时可以通过反射获取
@Documented //javadoc
public @interface ChrAutowired {
    String value() default "";
}
