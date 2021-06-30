package com.fly.serverless.common;


/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/10
 */
public class BaseException extends RuntimeException {
    private Integer code;

    public BaseException() {
    }

    public BaseException(Throwable cause) {
        super(cause);
        this.code = 1;
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(Throwable cause, Integer code, String message) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public BaseException setCode(Integer code) {
        this.code = code;
        return this;
    }

}
