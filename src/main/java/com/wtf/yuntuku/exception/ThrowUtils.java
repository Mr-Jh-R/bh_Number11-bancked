package com.wtf.yuntuku.exception;

/**
 * 异常处理工具类
 */
public class ThrowUtils {

    /**
     * 抛出异常
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwif(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 抛出异常
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwif(boolean condition, ErrorCode errorCode) {
        throwif(condition, new BusinessException(errorCode));
    }

    /**
     * 抛出异常
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 消息
     */
    public static void throwif(boolean condition, ErrorCode errorCode,String message) {
        throwif(condition, new BusinessException(errorCode,message));
    }
}
