package de.cjdev.scrolls;

import io.netty.util.internal.UnstableApi;
import net.hollowcube.luau.LuaState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UnstableApi
public interface ReflectedUserData extends LuaUserData {
    Map<Class<?>, Map<String, MethodHandle>> METHOD_CACHE = new ConcurrentHashMap<>();
    Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    @Override
    default Object getLuaField(String field) {
        Class<?> clazz = getClass();
        try {
            return getFieldCached(clazz, field).get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void setLuaField(String field, Object value) {
        Class<?> clazz = getClass();
        try {
            getFieldCached(clazz, field).set(this, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default int callMethod(LuaState state, String method, Object... args) {
        Class<?> clazz = getClass();
        try {
            MethodHandle method1 = getMethodCached(clazz, method);
            method1.invoke(this, args);
            if (method1.type().returnType() != void.class) {
                return 1;
            }
        } catch (Throwable _) {
        }
        return 0;
    }

    private static Field getFieldCached(Class<?> clazz, String name) {
        return FIELD_CACHE
                .computeIfAbsent(clazz, _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> {
                    try {
                        return clazz.getField(n);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static MethodHandle getMethodCached(Class<?> clazz, String name) {
        return METHOD_CACHE
                .computeIfAbsent(clazz, _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> {
                    for (Method m : clazz.getMethods()) {
                        if (m.getName().equals(n)) {
                            try {
                                return MethodHandles.lookup().unreflect(m);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    throw new RuntimeException("Method not found: " + n);
                });
    }
}
