package com.aigc.controller;

import com.aigc.common.Result;
import com.aigc.entity.SystemConfig;
import com.aigc.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/list")
    public Result<List<SystemConfig>> listAll(@RequestAttribute("userId") Long userId) {
        log.info("查询用户配置: userId={}", userId);
        return Result.success(systemConfigService.listByUserId(userId));
    }

    @GetMapping("/map")
    public Result<Map<String, String>> getConfigMap(@RequestAttribute("userId") Long userId) {
        log.info("获取配置映射: userId={}", userId);
        return Result.success(systemConfigService.getConfigMapByUserId(userId));
    }

    @GetMapping("/get/{configKey}")
    public Result<SystemConfig> getByKey(@PathVariable String configKey, @RequestAttribute("userId") Long userId) {
        log.info("查询用户配置: configKey={}, userId={}", configKey, userId);
        SystemConfig config = systemConfigService.getByKeyAndUserId(configKey, userId);
        return config != null ? Result.success(config) : Result.fail("配置不存在");
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody SystemConfig config, @RequestAttribute("userId") Long userId) {
        log.info("保存用户配置: configKey={}, userId={}", config.getConfigKey(), userId);
        boolean success = systemConfigService.saveOrUpdateByUserId(config, userId);
        return success ? Result.success("保存成功", null) : Result.fail("保存失败");
    }

    @PostMapping("/batch-update")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> configMap, @RequestAttribute("userId") Long userId) {
        log.info("批量更新用户配置: userId={}", userId);
        boolean success = systemConfigService.batchUpdateByUserId(configMap, userId);
        return success ? Result.success("批量更新成功", null) : Result.fail("批量更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        log.info("删除用户配置: id={}, userId={}", id, userId);
        boolean success = systemConfigService.deleteById(id);
        return success ? Result.success("删除成功", null) : Result.fail("删除失败");
    }

    @DeleteMapping("/delete/key/{configKey}")
    public Result<Void> deleteByKeyAndUserId(@PathVariable String configKey, @RequestAttribute("userId") Long userId) {
        log.info("删除用户配置: configKey={}, userId={}", configKey, userId);
        boolean success = systemConfigService.deleteByKeyAndUserId(configKey, userId);
        return success ? Result.success("删除成功", null) : Result.fail("删除失败");
    }
}
