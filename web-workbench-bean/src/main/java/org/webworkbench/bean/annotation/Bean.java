package org.webworkbench.bean.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    String[] name() default {};

    String[] value() default {} ;

    String type();

}
