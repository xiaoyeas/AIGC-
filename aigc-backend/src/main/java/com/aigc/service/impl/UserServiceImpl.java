package com.aigc.service.impl;

import com.aigc.entity.SystemConfig;
import com.aigc.entity.User;
import com.aigc.mapper.UserMapper;
import com.aigc.service.SystemConfigService;
import com.aigc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SystemConfigService systemConfigService;

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(String username, String password) {
        User existingUser = findByUsername(username);
        if (existingUser != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        boolean saved = save(user);
        
        if (saved) {
            log.info("用户注册成功，初始化配置: userId={}, username={}", user.getId(), username);
            initializeUserConfig(user.getId());
        }
        
        return saved;
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    private void initializeUserConfig(Long userId) {
        String[] configKeys = {
            "image.api.url",
            "image.api.key",
            "image.model.name",
            "video.api.url",
            "video.api.key",
            "video.model.name",
            "frontend.tunnel.url",
            "backend.tunnel.url"
        };
        
        for (String configKey : configKeys) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(configKey);
            
            // 设置默认值
            switch (configKey) {
                case "image.api.url":
                    config.setConfigValue("https://open.bigmodel.cn/api/paas/v4");
                    break;
                case "image.api.key":
                    config.setConfigValue("");
                    break;
                case "image.model.name":
                    config.setConfigValue("cogview-3-flash");
                    break;
                case "video.api.url":
                    config.setConfigValue("https://open.bigmodel.cn/api/paas/v4");
                    break;
                case "video.api.key":
                    config.setConfigValue("");
                    break;
                case "video.model.name":
                    config.setConfigValue("cogvideox-3");
                    break;
                case "frontend.tunnel.url":
                    config.setConfigValue("1ov163849ga82.vicp.fun");
                    break;
                case "backend.tunnel.url":
                    config.setConfigValue("http://1ov163849ga82.vicp.fun:59798");
                    break;
                default:
                    config.setConfigValue("");
                    break;
            }
            
            config.setUserId(userId);
            config.setConfigDesc(getConfigDesc(configKey));
            systemConfigService.saveOrUpdateByUserId(config, userId);
        }
    }
    
    private String getConfigDesc(String configKey) {
        switch (configKey) {
            case "image.api.url":
                return "图片API地址";
            case "image.api.key":
                return "图片API密钥";
            case "image.model.name":
                return "图片模型名称";
            case "video.api.url":
                return "视频API地址";
            case "video.api.key":
                return "视频API密钥";
            case "video.model.name":
                return "视频模型名称";
            case "frontend.tunnel.url":
                return "前端隧道地址";
            case "backend.tunnel.url":
                return "后端隧道地址";
            default:
                return "";
        }
    }
}