package com.lnsoft.annotation;

import java.lang.annotation.*;

/**
 * Created By Chr on 2019/1/24/0024.
 */
@Target({ElementType.TYPE, ElementType.METHOD})   //只能在类上或方法上
@Retention(RetentionPolicy.RUNTIME) //表示运行时可以通过反射获取
@Documented //javadoc
public @interface ChrRequestMapping {
    /**
     * 为什么会有value方法：因为注解：@ChrRequestMapping("chr")的括号里有值
     * 调用这个方法，就可以获得这个括号的名字
     *
     * @return
     */
    String value() default "";
}
