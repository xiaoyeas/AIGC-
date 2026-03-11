package com.aigc.config;

import com.aigc.common.Result;
import com.aigc.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 放行认证相关接口
        if (requestURI.startsWith("/auth/") || requestURI.equals("/auth")) {
            return true;
        }
        
        // 放行静态资源
        if (requestURI.startsWith("/static/") || requestURI.endsWith(".png") || requestURI.endsWith(".jpg")) {
            return true;
        }
        
        String token = getTokenFromRequest(request);
        
        if (token == null || token.isEmpty()) {
            log.warn("请求未携带Token: {}", requestURI);
            return sendUnauthorizedResponse(response, "未提供认证令牌");
        }
        
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token验证失败: {}", requestURI);
            return sendUnauthorizedResponse(response, "认证令牌无效或已过期");
        }
        
        // 将用户信息设置到请求属性中，供后续使用
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        
        log.debug("Token验证成功: userId={}, username={}, uri={}", userId, username, requestURI);
        return true;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return request.getParameter("token");
    }

    private boolean sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<?> result = Result.fail(401, message);
        objectMapper.writeValue(response.getWriter(), result);
        
        return false;
    }
}