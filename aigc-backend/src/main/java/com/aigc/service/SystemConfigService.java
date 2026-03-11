package com.aigc.service;

import com.aigc.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    List<SystemConfig> listAll();
    
    List<SystemConfig> listByUserId(Long userId);

    SystemConfig getByKey(String configKey);
    
    SystemConfig getByKeyAndUserId(String configKey, Long userId);

    String getValueByKey(String configKey);
    
    String getValueByKeyAndUserId(String configKey, Long userId);

    boolean saveOrUpdate(SystemConfig config);
    
    boolean saveOrUpdateByUserId(SystemConfig config, Long userId);

    boolean updateByKey(String configKey, String configValue);
    
    boolean updateByKeyAndUserId(String configKey, String configValue, Long userId);

    boolean deleteById(Long id);

    boolean deleteByKey(String configKey);
    
    boolean deleteByKeyAndUserId(String configKey, Long userId);

    Map<String, String> getAllConfigMap();
    
    Map<String, String> getConfigMapByUserId(Long userId);

    boolean batchUpdate(Map<String, String> configMap);
    
    boolean batchUpdateByUserId(Map<String, String> configMap, Long userId);
}
