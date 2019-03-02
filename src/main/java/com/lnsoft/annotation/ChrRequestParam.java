package com.lnsoft.annotation;

import java.lang.annotation.*;

/**
 * Created By Chr on 2019/1/24/0024.
 */
@Target({ElementType.PARAMETER})   //只能在方法的参数上
@Retention(RetentionPolicy.RUNTIME) //表示运行时可以通过反射获取
@Documented //javadoc
public @interface ChrRequestParam {
    String value() default "";
}
