package com.lzq.simulatedtradingsystem.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {
    private String code;
    private String message;
    private T data;

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return "1".equals(this.code);
    }

    // 成功（包含数据）
    public static <T> Result<T> success(T data) {
        return new Result<>("1", "success", data);
    }

    // 成功（不包含数据）
    public static <T> Result<T> success() {
        return new Result<>("1", "success", null);
    }

    // 失败
    public static <T> Result<T> failure(String message) {
        return new Result<>("0", message, null);
    }
}
