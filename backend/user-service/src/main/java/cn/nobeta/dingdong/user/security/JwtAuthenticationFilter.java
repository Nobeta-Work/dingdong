package cn.nobeta.dingdong.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.nobeta.dingdong.user.domain.MallUser;
import cn.nobeta.dingdong.user.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT 认证过滤器 —— 用户服务内部的请求级认证拦截器
 * 登录链路中，用户登录后的每次请求（除 /api/auth/** 和 /actuator/** 外）都必须经过此过滤器：
 * ① 从 Authorization 请求头提取 Bearer token
 * ② 调用 JwtTokenService.parse() 校验签名和有效期
 * ③ 查询数据库确认用户存在且未被禁用
 * ④ 将用户信息绑定到 CurrentUserContext，业务代码通过 require() 获取
 * ⑤ 请求结束后清理 ThreadLocal
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   ObjectMapper objectMapper,
                                   UserMapper userMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    /**
     * 跳过白名单路径：登录/注册接口（/api/auth/**）和健康检查（/actuator/**）
     * 登录接口本身不需要 token 即可访问
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.startsWith("/actuator/");
    }

    /**
     * 过滤请求的核心逻辑
     * 注意：使用 try-finally 确保无论认证成功与否，ThreadLocal 都会被清理
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        // ② 校验格式：必须以 "Bearer " 开头
        if (authorization == null || !authorization.startsWith("Bearer ")) { writeUnauthorized(response, "AUTH_UNAUTHORIZED", "请先登录"); return; }
        CurrentUser currentUser;
        try {
            // ③ 去掉 "Bearer " 前缀（7 个字符），调用 JwtTokenService 解析校验 token
            currentUser = tokenService.parse(authorization.substring(7));
            // ④ 查库确认用户存在且未被禁用（防止 token 有效但用户被后台禁用的情况）
            MallUser persistedUser = userMapper.findById(currentUser.id());
            if (persistedUser == null || !Integer.valueOf(1).equals(persistedUser.getStatus())) { writeUnauthorized(response, "AUTH_USER_DISABLED", "当前用户已被禁用"); return; }
            // ⑤ 认证通过，将用户信息绑定到当前线程的 ThreadLocal
        } catch (RuntimeException exception) {
            writeUnauthorized(response, "AUTH_TOKEN_INVALID", exception.getMessage());
            return;
        }
        CurrentUserContext.set(currentUser);
        try { filterChain.doFilter(request, response); }
        finally { CurrentUserContext.clear(); }
    }

    /** 返回 401 未授权的 JSON 响应 */
    private void writeUnauthorized(HttpServletResponse response,
                                   String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorBody(code, message));
    }

    private record ErrorBody(String code, String message) { }

}
