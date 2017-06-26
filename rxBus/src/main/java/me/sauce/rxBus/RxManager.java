package me.sauce.rxBus;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sauce on 2017/5/22.
 */

public class RxManager {
    private static final Map<Class<?>, Class<?>> CLASS_MAP = new LinkedHashMap<>();

    private static final Map<Class<?>, Constructor<? extends UnSubscribe>> BINDINGS = new LinkedHashMap<>();
    private static final Map<Class<?>, Method> UNBINDINGS = new LinkedHashMap<>();

    @NonNull
    public static UnSubscribe init(Object o) {
        UnSubscribe unSubscribe = UnSubscribe.EMPTY;

        try {
            Constructor<? extends UnSubscribe> constructor = findBindingConstructorForClass(o.getClass());
            if (constructor != null) {
                unSubscribe = constructor.newInstance(o);
            }
        } catch (NullPointerException | InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return unSubscribe;
    }

    @Nullable
    @CheckResult
    private static Constructor<? extends UnSubscribe> findBindingConstructorForClass(Class<?> cls) {
        Constructor<? extends UnSubscribe> bindingConstructor = BINDINGS.get(cls);
        if (bindingConstructor == null) {
            String clsName = cls.getName();
            if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
                return null;
            }
            try {
                Class<?> bindingClass = Class.forName(clsName + "_BusManager");
                    bindingConstructor = (Constructor<? extends UnSubscribe>) bindingClass.getConstructor(cls);
            } catch (ClassNotFoundException e) {
                bindingConstructor = findBindingConstructorForClass(cls.getSuperclass());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
            }
            BINDINGS.put(cls, bindingConstructor);
        }
        return bindingConstructor;
    }


}
