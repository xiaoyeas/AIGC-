package com.aigc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 
 * <p>所有API接口的统一返回格式，包含状态码、消息、数据和时间戳。
 * 采用泛型设计，支持任意类型的数据返回。</p>
 * 
 * <p>响应结构：</p>
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },
 *   "timestamp": 1704067200000
 * }
 * </pre>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>统一响应格式，便于前端统一处理</li>
 *   <li>包含时间戳，便于问题排查</li>
 *   <li>提供静态工厂方法，简化对象创建</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 成功响应（无数据）
 * return Result.success();
 * 
 * // 成功响应（有数据）
 * return Result.success(user);
 * 
 * // 成功响应（自定义消息）
 * return Result.success("查询成功", user);
 * 
 * // 失败响应
 * return Result.fail("用户不存在");
 * 
 * // 失败响应（自定义状态码）
 * return Result.fail(404, "资源未找到");
 * </pre>
 * 
 * @param <T> 数据类型
 * @author AIGC Platform
 * @version 1.0.0
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 
     * <p>HTTP状态码风格的响应码：</p>
     * <ul>
     *   <li>200: 成功</li>
     *   <li>400: 客户端参数错误</li>
     *   <li>500: 服务端错误</li>
     * </ul>
     */
    private Integer code;

    /**
     * 响应消息
     * 
     * <p>描述响应结果的文字信息。
     * 成功时显示操作结果，失败时显示错误原因。</p>
     */
    private String message;

    /**
     * 响应数据
     * 
     * <p>实际返回的业务数据，可以是任意类型。
     * 失败响应时通常为null。</p>
     */
    private T data;

    /**
     * 时间戳
     * 
     * <p>响应生成的时间，毫秒级Unix时间戳。
     * 用于日志追踪和问题排查。</p>
     */
    private Long timestamp;

    /**
     * 默认构造函数
     * 
     * <p>自动设置当前时间戳。</p>
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功响应（无数据）
     * 
     * <p>适用于不需要返回数据的成功操作，如删除、更新等。</p>
     * 
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 创建成功响应（有数据）
     * 
     * <p>适用于查询操作，返回查询到的数据。</p>
     * 
     * @param <T>  数据类型
     * @param data 返回的数据
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（自定义消息和数据）
     * 
     * <p>适用于需要自定义成功消息的场景。</p>
     * 
     * @param <T>     数据类型
     * @param message 成功消息
     * @param data    返回的数据
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 创建失败响应（默认状态码）
     * 
     * <p>使用默认状态码500，适用于服务端错误。</p>
     * 
     * @param <T>     数据类型
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建失败响应（自定义状态码）
     * 
     * <p>适用于需要区分不同错误类型的场景。</p>
     * 
     * <p>常用状态码：</p>
     * <ul>
     *   <li>400: 参数错误</li>
     *   <li>401: 未授权</li>
     *   <li>403: 禁止访问</li>
     *   <li>404: 资源不存在</li>
     *   <li>500: 服务器内部错误</li>
     * </ul>
     * 
     * @param <T>     数据类型
     * @param code    错误状态码
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
