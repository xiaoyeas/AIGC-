package com.aigc.service;

import com.aigc.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    boolean registerUser(String username, String password);

    boolean validatePassword(String rawPassword, String encodedPassword);
}