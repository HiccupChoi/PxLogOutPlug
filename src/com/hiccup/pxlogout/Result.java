package com.hiccup.pxlogout;

/**
 * @Author: Hiccup
 * @Date: 2019/9/25 10:19 上午
 * 返回给前端的展示结果
 */

public class Result {

    boolean success;

    String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" + "success=" + success + ", message='" + message + '\'' + '}';
    }
}
