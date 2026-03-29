package com.lzq.simulatedtradingsystem.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {
    private String code; // 状态码
    private String message; // 错误信息
    private T data; // 数据

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
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
