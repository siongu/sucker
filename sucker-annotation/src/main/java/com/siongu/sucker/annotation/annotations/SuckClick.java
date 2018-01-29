package com.siongu.sucker.annotation.annotations;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
        targetType = "android.view.View",
        setter = "setOnClickListener",
        type = "com.siongu.sucker.api.SuckerClickListener",
        method = @ListenerMethod(
                name = "handleClick",
                parameters = {"android.view.View"}
        )
)
public @interface SuckClick {
    @IdRes int[] value() default {-1};
}
