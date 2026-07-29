package com.wsf.controller.handler;

import com.wsf.infrastructure.common.result.Result;
import com.wsf.infrastructure.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理参数校验失败异常（如 @Valid 校验不通过）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // 提取所有字段校验错误信息，拼接为一条提示
        String errorMsg = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        // 可自定义业务错误码，例如 4001
        // 使用 Result.failed(int status, IResultCode resultCode, String msg)
        Result<Void> result = Result.failed(
                HttpStatus.BAD_REQUEST.value(),
                ResultCode.INVALID_USER_INPUT,  // 请确保您有该枚举常量
                errorMsg
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理 IllegalArgumentException，通常表示请求参数不合法 -> 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("非法参数异常: {}", ex.getMessage());
        Result<Void> result = Result.failed(
                HttpStatus.BAD_REQUEST.value(),
                ResultCode.USER_REQUEST_PARAMETER_ERROR,        // 请确保您有该枚举常量
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理 NoSuchElementException，通常表示资源未找到 -> 404
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("资源未找到: {}", ex.getMessage());
        Result<Void> result = Result.failed(
                HttpStatus.NOT_FOUND.value(),
                ResultCode.USER_RESOURCE_NOT_FOUND, // 请确保您有该枚举常量
                "请求的资源不存在: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理 IllegalStateException，通常表示资源冲突或状态异常 -> 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> handleIllegalState(IllegalStateException ex) {
        log.warn("状态冲突异常: {}", ex.getMessage());
        Result<Void> result = Result.failed(
                HttpStatus.CONFLICT.value(),
                ResultCode.USER_OPERATION_EXCEPTION,     // 请确保您有该枚举常量
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    /**
     * 兜底处理所有未捕获的 Exception -> 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("系统内部异常: ", ex); // 打印完整堆栈
        Result<Void> result = Result.failed(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ResultCode.SYSTEM_ERROR,
                "系统内部错误，请稍后重试"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
