package com.aigc.manager;

import com.aigc.dto.ImageGenerateResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步任务管理器
 * 
 * <p>用于管理视频生成等耗时任务的异步执行和状态查询。</p>
 */
@Slf4j
@Component
public class TaskManager {

    /**
     * 任务状态存储
     */
    private final Map<String, TaskInfo> taskMap = new ConcurrentHashMap<>();

    /**
     * 异步线程池
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }

    /**
     * 任务信息
     */
    @Data
    public static class TaskInfo {
        private String taskId;
        private TaskStatus status;
        private ImageGenerateResult result;
        private String errorMessage;
        private long createTime;
        private long updateTime;
    }

    /**
     * 创建并提交异步任务
     * 
     * @param taskId 任务ID
     * @param taskRunnable 任务执行逻辑
     */
    public void submitTask(String taskId, TaskRunnable taskRunnable) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus(TaskStatus.PENDING);
        taskInfo.setCreateTime(System.currentTimeMillis());
        taskInfo.setUpdateTime(System.currentTimeMillis());
        taskMap.put(taskId, taskInfo);

        executorService.submit(() -> {
            try {
                taskInfo.setStatus(TaskStatus.PROCESSING);
                taskInfo.setUpdateTime(System.currentTimeMillis());

                ImageGenerateResult result = taskRunnable.execute();

                taskInfo.setStatus(TaskStatus.SUCCESS);
                taskInfo.setResult(result);
                taskInfo.setUpdateTime(System.currentTimeMillis());
                log.info("任务执行成功, taskId: {}", taskId);
            } catch (Exception e) {
                log.error("任务执行失败, taskId: {}", taskId, e);
                taskInfo.setStatus(TaskStatus.FAILED);
                taskInfo.setErrorMessage(e.getMessage());
                taskInfo.setUpdateTime(System.currentTimeMillis());
            }
        });
    }

    /**
     * 查询任务状态
     * 
     * @param taskId 任务ID
     * @return 任务信息
     */
    public TaskInfo getTaskInfo(String taskId) {
        return taskMap.get(taskId);
    }

    /**
     * 清理过期任务（超过1小时的任务）
     */
    public void cleanupExpiredTasks() {
        long now = System.currentTimeMillis();
        long expireTime = 60 * 60 * 1000;

        taskMap.entrySet().removeIf(entry -> {
            TaskInfo taskInfo = entry.getValue();
            return (now - taskInfo.getUpdateTime()) > expireTime;
        });
    }

    /**
     * 任务执行接口
     */
    @FunctionalInterface
    public interface TaskRunnable {
        ImageGenerateResult execute() throws Exception;
    }
}
