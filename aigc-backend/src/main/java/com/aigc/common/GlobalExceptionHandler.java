package com.aigc.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * <p>统一处理Controller层抛出的各类异常，返回标准化的错误响应。
 * 使用@RestControllerAdvice注解，对所有Controller进行AOP拦截。</p>
 * 
 * <p>处理的异常类型：</p>
 * <ul>
 *   <li>Exception: 所有未捕获的异常</li>
 *   <li>RuntimeException: 运行时异常</li>
 *   <li>IllegalArgumentException: 参数非法异常</li>
 *   <li>MethodArgumentNotValidException: 参数校验失败异常</li>
 *   <li>BindException: 参数绑定失败异常</li>
 * </ul>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>集中处理异常，避免Controller代码冗余</li>
 *   <li>统一错误响应格式，便于前端处理</li>
 *   <li>记录详细的错误日志，便于问题排查</li>
 *   <li>区分不同类型的异常，返回合适的HTTP状态码</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     * 
     * <p>作为兜底的异常处理器，捕获所有未被其他处理器处理的异常。
     * 返回500状态码，表示服务器内部错误。</p>
     * 
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>记录错误日志（包含堆栈信息）</li>
     *   <li>返回500状态码和错误消息</li>
     * </ol>
     * 
     * @param e 捕获的异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(500, "系统异常: " + e.getMessage());
    }

    /**
     * 处理运行时异常
     * 
     * <p>捕获所有RuntimeException及其子类异常。
     * 这类异常通常表示程序逻辑错误或外部依赖问题。</p>
     * 
     * <p>常见运行时异常：</p>
     * <ul>
     *   <li>NullPointerException: 空指针异常</li>
     *   <li>IndexOutOfBoundsException: 数组越界</li>
     *   <li>ClassCastException: 类型转换异常</li>
     *   <li>ArithmeticException: 算术异常</li>
     * </ul>
     * 
     * @param e 捕获的运行时异常
     * @return 统一错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.fail(500, "运行时异常: " + e.getMessage());
    }

    /**
     * 处理参数非法异常
     * 
     * <p>捕获IllegalArgumentException异常。
     * 通常由参数校验代码手动抛出。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>业务参数校验失败</li>
     *   <li>枚举值不匹配</li>
     *   <li>参数范围不合法</li>
     * </ul>
     * 
     * @param e 捕获的参数非法异常
     * @return 统一错误响应（400状态码）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: {}", e.getMessage(), e);
        return Result.fail(400, "参数异常: " + e.getMessage());
    }

    /**
     * 处理参数校验失败异常
     * 
     * <p>捕获@Valid注解校验失败时抛出的异常。
     * 提取所有字段错误信息，合并后返回。</p>
     * 
     * <p>校验注解示例：</p>
     * <pre>
     * &#64;NotBlank(message = "用户名不能为空")
     * private String username;
     * 
     * &#64;Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
     * private String password;
     * </pre>
     * 
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 400,
     *   "message": "参数校验失败: 用户名不能为空, 密码长度必须在6-20之间",
     *   "data": null,
     *   "timestamp": 1704067200000
     * }
     * </pre>
     * 
     * @param e 捕获的参数校验异常
     * @return 统一错误响应（400状态码）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", message);
        return Result.fail(400, "参数校验失败: " + message);
    }

    /**
     * 处理参数绑定失败异常
     * 
     * <p>捕获表单绑定失败时抛出的异常。
     * 通常发生在表单提交时，参数类型不匹配。</p>
     * 
     * <p>常见场景：</p>
     * <ul>
     *   <li>字符串无法转换为数字</li>
     *   <li>日期格式不正确</li>
     *   <li>枚举值不匹配</li>
     * </ul>
     * 
     * @param e 捕获的绑定异常
     * @return 统一错误响应（400状态码）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败: {}", message);
        return Result.fail(400, "参数绑定失败: " + message);
    }
}
