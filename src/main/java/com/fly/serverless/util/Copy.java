package com.fly.serverless.util;

import com.fly.serverless.common.BaseException;
import javassist.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * 一个工具类，用于对象属性拷贝
 * 基于javassist，动态生成字节码
 * 从而避免了反射调用
 *
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/30
 */
public class Copy {

    private static final ConcurrentHashMap<ConverterKey, Converter> CACHE = new ConcurrentHashMap<>(32);
    private static final AtomicInteger ID = new AtomicInteger();
    private static CtClass converterInterface;
    static { init(); }

    private static void init() {
        CtClass[] nestedClasses = new CtClass[0];
        try {
            nestedClasses = ClassPool.getDefault().get(Copy.class.getName()).getNestedClasses();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        Arrays.stream(nestedClasses)
                .filter(nestedClass -> nestedClass.getName().equals(Converter.class.getName()))
                .forEach(nestedClass -> converterInterface = nestedClass);
    }


    public static void copy(Object from, Object to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();

        Converter converter = getConverter(fromClass, toClass);
        converter.convert(from, to);
    }

    /**
     * 从缓存获取converter
     *
     * @param fromClass 源类
     * @param toClass   目标类
     * @return          转换器
     */
    private static Converter getConverter(Class<?> fromClass, Class<?> toClass) {
        ConverterKey key = new ConverterKey(fromClass, toClass);
        return CACHE.computeIfAbsent(key, Copy::generateConverter);
    }


    /**
     * 使用javassist生成一个转换器
     *
     * @param key   key
     * @return      converter
     */
    private static Converter generateConverter(ConverterKey key) {

        Class<?> fromClass = key.fromClass;
        Class<?> toClass = key.toClass;

        ClassPool pool = ClassPool.getDefault();
        CtClass converterClass = pool.makeClass("RuntimeConverter" + ID.getAndIncrement());

        try {
            converterClass.addInterface(converterInterface);

            CtMethod convertMethod = CtNewMethod.make(generateMethod(fromClass, toClass), converterClass);
            converterClass.addMethod(convertMethod);

            Class<?> type = converterClass.toClass(Copy.class.getClassLoader(), Copy.class.getProtectionDomain());
            return (Converter) type.newInstance();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 生成转换器方法
     *
     * @param fromClass 原始类
     * @param toClass   目标类
     * @return          方法代码
     */
    private static String generateMethod(Class<?> fromClass, Class<?> toClass) {
        String prefix = "public void convert(Object from, Object to) {\n";
        String castFromCode = fromClass.getName() + " a = (" + fromClass.getName() + ") from;\n";
        String castToCode = toClass.getName() + " b = (" + toClass.getName() + ") to;\n";
        String postfix = "}\n";

        Set<String> fromFields = getFields(fromClass);
        Set<String> toFields = getFields(toClass);

        fromFields.retainAll(toFields);

        StringBuilder code = new StringBuilder();
        for (String field : fromFields) {
            field = field.substring(0, 1).toUpperCase() + field.substring(1);
            code.append("b.set").append(field).append("(a.get").append(field).append("());\n");
        }

        return prefix + castFromCode + castToCode + code + postfix;
    }

    /**
     * 获取一个类（包含父类）的所有属性
     *
     * @param type type
     * @return  属性list
     */
    private static Set<String> getFields(Class<?> type) {

        Field[] fields = type.getDeclaredFields();
        Set<String> fieldSet = Stream.of(fields).map(Field::getName).collect(toSet());

        Class<?> parent = type.getSuperclass();
        if (type.equals(Object.class) || parent.equals(Object.class)) {
            return fieldSet;
        }

        Set<String> parentFieldSet = getFields(parent);
        fieldSet.addAll(parentFieldSet);

        return fieldSet;
    }


    /**
     * 用于缓存的键
     */
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class ConverterKey {
        Class<?> fromClass;
        Class<?> toClass;
    }

    public interface Converter {

        /**
         * 将一个对象复制到另一个对象
         * @param from  from
         * @param to    to
         */
        void convert(Object from, Object to);
    }

}
