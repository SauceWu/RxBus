package me.sauce;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by Android on 2016/6/8.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Subscribe {
    int tag() default RxBusTag.TAG_DEFAULT;

    EventThread thread() default EventThread.MAIN_THREAD;
}
