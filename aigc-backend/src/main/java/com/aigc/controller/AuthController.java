package com.aigc.controller;

import com.aigc.common.Result;
import com.aigc.dto.LoginRequest;
import com.aigc.dto.LoginResponse;
import com.aigc.dto.RegisterRequest;
import com.aigc.entity.User;
import com.aigc.service.UserService;
import com.aigc.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        
        boolean success = userService.registerUser(request.getUsername(), request.getPassword());
        if (success) {
            return Result.success("注册成功", null);
        } else {
            return Result.fail("用户名已存在");
        }
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());
        
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        
        boolean passwordValid = userService.validatePassword(request.getPassword(), user.getPassword());
        if (!passwordValid) {
            return Result.fail("用户名或密码错误");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        Long expiresIn = jwtUtils.getExpiration();
        
        LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), expiresIn);
        return Result.success("登录成功", response);
    }
}