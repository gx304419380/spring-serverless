package com.fly.serverless.util;

import com.fly.serverless.common.BaseException;

import java.util.Objects;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/10
 */
public class Assert {


    /**
     * 断言是否为真
     *
     * @param expression 布尔值
     * @param code       code
     * @param msg        msg
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, Integer code, String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new BaseException(code, msg);
        }
    }

    /**
     * 断言是否为真，如果为 {@code false}
     *
     * @param expression 布尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression) throws IllegalArgumentException {
        isTrue(expression, 400, "[Assertion failed] - this expression must be true");
    }

    // ----------------------------------------------------------------------------------------------------------- Check not null
    /**
     * 断言对象是否不为{@code null}
     *
     * @param object 被检查对象
     * @param code   code
     * @param msg    msg
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> void notNull(T object, Integer code, String msg) throws IllegalArgumentException {
        if (object == null) {
            throw new BaseException(code, msg);
        }
    }

    public static <T> void isEqual(T a, T b, Integer code, String msg) throws IllegalArgumentException {
        if (!Objects.equals(a, b)) {
            throw new BaseException(code, msg);
        }
    }

    public static void notEmpty(String o, Integer code, String msg) {
        if (o == null || o.length() == 0) {
            throw new BaseException(code, msg);
        }
    }
}
