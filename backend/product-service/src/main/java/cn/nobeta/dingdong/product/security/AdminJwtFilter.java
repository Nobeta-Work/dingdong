package cn.nobeta.dingdong.product.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * 管理后台 JWT 认证过滤器 —— 登录链路中商品管理 API 的安全拦截器
 * 此过滤器专用于 /api/admin/** 路径，在通用 JWT 校验基础上增加了角色权限控制：
 * ① 拦截所有 /api/admin/* 请求，从 Authorization 头提取 Bearer token
 * ② 自实现 HMAC-SHA256 签名校验 + 过期时间校验（与 user-service 逻辑一致）
 * ③ 额外校验 token 中 role 字段必须为 "ADMIN"（非管理员请求返回 403 Forbidden）
 * ④ 认证失败返回 401，角色不符返回 403
 *
 * 注意：此过滤器仅做准入判断，不绑定 ThreadLocal 上下文，
 *       因为 product-service 的管理接口通常不需要获取具体用户信息，
 *       仅需要验证请求者是否为管理员身份。
 */
@Component
public class AdminJwtFilter extends OncePerRequestFilter {
    /** JSON 序列化工具，用于返回错误响应和解析 JWT Payload */
    private final ObjectMapper objectMapper;
    /** HMAC-SHA256 签名密钥，与 user-service 的 JwtTokenService 使用相同值 */
    private final byte[] secret;

    /**
     * 构造函数注入 ObjectMapper 和 JWT 签名密钥
     * @param objectMapper Jackson ObjectMapper
     * @param secret JWT 签名密钥，从 security.jwt.secret 配置读取
     */
    public AdminJwtFilter(ObjectMapper objectMapper,
            @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 仅拦截管理后台路径 /api/admin/*，非管理接口直接放行
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean seckillProtected = ("POST".equals(request.getMethod()) && path.matches("/api/seckill/activities/[^/]+/orders"))
                || path.startsWith("/api/seckill/orders/");
        return !path.startsWith("/api/admin/") && !path.startsWith("/api/files") && !seckillProtected;
    }

    /**
     * 过滤请求的核心逻辑 —— 管理后台 JWT 认证 + 角色权限校验
     * 流程：
     * ① 提取并校验 Authorization 头格式（Bearer token）
     * ② 调用 parse() 解析校验 token（三段式、签名、过期）
     * ③ 校验 token 中 role 是否为 "ADMIN"
     * ④ 放行到后续 Filter 和 Controller
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // ① 从请求头获取 Authorization 字段
        String authorization = request.getHeader("Authorization");
        // ② 校验格式：必须以 "Bearer " 开头
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            reject(response, "AUTH_UNAUTHORIZED", "请先登录");
            return;
        }
        Map<String, Object> claims;
        try {
            // ③ 去掉 "Bearer " 前缀（7 个字符），调用 parse() 解析校验 token
            claims = parse(authorization.substring(7));
        } catch (Exception exception) {
            // ⑥ 认证失败（签名不一致 / 过期 / 格式错误），返回 401
            reject(response, "AUTH_TOKEN_INVALID", "无效或已过期的登录令牌");
            return;
        }
        String path = request.getServletPath();
        boolean adminProtected = path.startsWith("/api/admin/") || path.startsWith("/api/files");
        if (adminProtected && !"ADMIN".equals(claims.get("role"))) {
            reject(response, "AUTH_FORBIDDEN", "需要管理员权限");
            return;
        }
        ProductUserContext.set(new CurrentProductUser(((Number) claims.get("sub")).longValue(), String.valueOf(claims.get("role"))));
        try { chain.doFilter(request, response); } finally { ProductUserContext.clear(); }
    }

    /**
     * 解析并校验 JWT Token
     * 与 user-service 的 JwtTokenService.parse() 使用相同逻辑：
     * ① 校验三段式格式（Header.Payload.Signature）
     * ② 重新计算 HMAC-SHA256 签名并与 token 中签名做恒定时间比较（MessageDigest.isEqual 防时序攻击）
     * ③ 反序列化 Payload JSON 为 Map
     * ④ 校验 exp 过期时间
     *
     * @param token 去掉 "Bearer " 前缀后的纯 token 字符串
     * @return JWT Payload 的 claims 键值对（含 sub、username、role、iat、exp）
     * @throws IllegalArgumentException token 格式错误、签名不匹配或已过期
     */
    private Map<String, Object> parse(String token) throws Exception {
        // ① 按 "." 分割为三段：Header、Payload、Signature
        String[] parts = token.split("\\.");
        // ② 校验格式 + 签名恒定时间比较（防止时序攻击）
        if (parts.length != 3) throw new IllegalArgumentException();
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
        if (!MessageDigest.isEqual(signature, sign(parts[0] + "." + parts[1])))
            throw new IllegalArgumentException();
        // ③ 解码并反序列化 Payload
        Map<String, Object> claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]),
                new TypeReference<>() {});
        // ④ 校验过期时间：当前时间 >= exp 则 token 已过期
        if (Instant.now().getEpochSecond() >= ((Number) claims.get("exp")).longValue())
            throw new IllegalArgumentException();
        return claims;
    }

    /**
     * HMAC-SHA256 签名计算
     * @param text 签名输入（Header.Body 字符串）
     * @return 签名字节数组
     */
    private byte[] sign(String text) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
    }

    /** 返回 401 或 403 的 JSON 错误响应 */
    private void reject(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus("AUTH_FORBIDDEN".equals(code)
                ? HttpServletResponse.SC_FORBIDDEN
                : HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorBody(code, message));
    }

    /** 错误响应体：code + message */
    private record ErrorBody(String code, String message) {}
}
