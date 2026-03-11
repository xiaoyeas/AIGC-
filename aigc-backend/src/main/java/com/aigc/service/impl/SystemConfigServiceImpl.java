package com.aigc.service.impl;

import com.aigc.entity.SystemConfig;
import com.aigc.mapper.SystemConfigMapper;
import com.aigc.service.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(null);
    }

    @Override
    public SystemConfig getByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        return systemConfigMapper.selectOne(wrapper);
    }

    @Override
    public String getValueByKey(String configKey) {
        SystemConfig config = getByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(SystemConfig config) {
        SystemConfig existing = getByKey(config.getConfigKey());
        if (existing != null) {
            config.setId(existing.getId());
            return systemConfigMapper.updateById(config) > 0;
        } else {
            return systemConfigMapper.insert(config) > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByKey(String configKey, String configValue) {
        SystemConfig config = getByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            return systemConfigMapper.updateById(config) > 0;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        return systemConfigMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        return systemConfigMapper.delete(wrapper) > 0;
    }

    @Override
    public Map<String, String> getAllConfigMap() {
        List<SystemConfig> configList = listAll();
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configList) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdate(Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            updateByKey(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    public List<SystemConfig> listByUserId(Long userId) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getUserId, userId);
        return systemConfigMapper.selectList(wrapper);
    }

    @Override
    public SystemConfig getByKeyAndUserId(String configKey, Long userId) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .eq(SystemConfig::getUserId, userId);
        return systemConfigMapper.selectOne(wrapper);
    }

    @Override
    public String getValueByKeyAndUserId(String configKey, Long userId) {
        SystemConfig config = getByKeyAndUserId(configKey, userId);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateByUserId(SystemConfig config, Long userId) {
        config.setUserId(userId);
        SystemConfig existing = getByKeyAndUserId(config.getConfigKey(), userId);
        if (existing != null) {
            config.setId(existing.getId());
            return systemConfigMapper.updateById(config) > 0;
        } else {
            return systemConfigMapper.insert(config) > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByKeyAndUserId(String configKey, String configValue, Long userId) {
        SystemConfig config = getByKeyAndUserId(configKey, userId);
        if (config != null) {
            config.setConfigValue(configValue);
            return systemConfigMapper.updateById(config) > 0;
        } else {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setUserId(userId);
            return systemConfigMapper.insert(config) > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByKeyAndUserId(String configKey, Long userId) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .eq(SystemConfig::getUserId, userId);
        return systemConfigMapper.delete(wrapper) > 0;
    }

    @Override
    public Map<String, String> getConfigMapByUserId(Long userId) {
        List<SystemConfig> configList = listByUserId(userId);
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configList) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateByUserId(Map<String, String> configMap, Long userId) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            updateByKeyAndUserId(entry.getKey(), entry.getValue(), userId);
        }
        return true;
    }
}
