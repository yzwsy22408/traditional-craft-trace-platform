package com.graduation.crafttrace.common;

/**
 * 全局统一返回结果类
 */
public class Result {
    private boolean success;
    private String message;
    private Object data;

    public Result() {}

    public static Result ok(String message) {
        Result r = new Result();
        r.setSuccess(true);
        r.setMessage(message);
        return r;
    }

    public static Result error(String message) {
        Result r = new Result();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    // Getter and Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}