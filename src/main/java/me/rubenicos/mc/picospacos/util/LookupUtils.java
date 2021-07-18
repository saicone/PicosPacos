package me.rubenicos.mc.picospacos.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LookupUtils {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<String, MethodHandle> methods = new HashMap<>();

    public static MethodHandle get(String name) {
        return methods.get(name);
    }

    public static void addMethod(String name, Class<?> clazz, String method, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.findVirtual(clazz, method, typeFrom(classes)));
    }

    public static void addStaticMethod(String name, Class<?> clazz, String method, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.findStatic(clazz, method, typeFrom(classes)));
    }

    public static void addField(String name, Class<?> clazz, String field, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        Field f = getField(clazz, field);
        methods.put(name, lookup.unreflectGetter(f));
    }

    public static Field getField(Class<?> clazz, String field) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f;
    }

    public static void addConstructor(String name, Class<?> clazz, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        methods.put(name, lookup.findConstructor(clazz, typeFrom(classes)));
    }

    private static MethodType typeFrom(Class<?>... classes) {
        return classes.length > 2 ? MethodType.methodType(classes[0], classes[1], Arrays.copyOfRange(classes, 2, classes.length)) : classes.length > 1 ? MethodType.methodType(classes[0], classes[1]) : MethodType.methodType(classes[0]);
    }
}
