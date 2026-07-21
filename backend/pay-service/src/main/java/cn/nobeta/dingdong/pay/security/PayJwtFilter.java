package cn.nobeta.dingdong.pay.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
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
import java.util.*;

/**
 * 支付服务 JWT 认证过滤器 —— 登录链路中支付相关 API 的安全拦截器
 * 每个支付微服务独立校验 JWT，不信任网关传递的请求头（零信任架构）：
 * ① 拦截所有 /api/* 请求，从 Authorization 头提取 Bearer token
 * ② 自实现 HMAC-SHA256 签名校验 + 过期时间校验
 * ③ 认证通过后将用户 ID 和角色存入 PayUserContext (ThreadLocal)
 * ④ 认证失败返回 401 Unauthorized
 * ⑤ 请求结束后 finally 块清理 ThreadLocal，防止内存泄漏
 *
 * 密钥与 user-service 的 JwtTokenService 共享同一值，
 * 确保 token 由 user-service 登录签发后可被支付服务独立校验。
 */
@Component
public class PayJwtFilter extends OncePerRequestFilter {
    /** JSON 序列化工具，用于返回错误响应和解析 JWT Payload */
    private final ObjectMapper m;
    /** HMAC-SHA256 签名密钥的字节形式，由配置中的明文字符串转换 */
    private final byte[] secret;

    /**
     * 构造函数注入 ObjectMapper 和 JWT 签名密钥
     * @param m Jackson ObjectMapper，用于 JSON 读写
     * @param s JWT 签名密钥，从 security.jwt.secret 配置读取，有默认值仅用于开发
     */
    public PayJwtFilter(ObjectMapper m,
            @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String s) {
        this.m = m;
        secret = s.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 跳过非 /api/ 路径，如静态资源和内部端点
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest r) {
        return !r.getServletPath().startsWith("/api/");
    }

    /**
     * 过滤请求的核心逻辑 —— 支付服务 JWT 认证入口
     * 流程：
     * ① 提取并校验 Authorization 头格式（Bearer token）
     * ② 调用 parse() 解析校验 token（三段式、签名、过期）
     * ③ 构建 CurrentPayUser 并存入 PayUserContext
     * ④ 放行到后续 Filter 和 Controller
     * ⑤ finally 块清理 ThreadLocal
     */
    @Override
    protected void doFilterInternal(HttpServletRequest q, HttpServletResponse r, FilterChain c)
            throws ServletException, IOException {
        // ① 从请求头获取 Authorization 字段
        String h = q.getHeader("Authorization");
        // ② 校验格式：必须以 "Bearer " 开头，否则拒绝
        if (h == null || !h.startsWith("Bearer ")) {
            deny(r);
            return;
        }
        try {
            // ③ 去掉 "Bearer " 前缀（7 个字符），调用 parse() 解析校验 token
            Map<String, Object> x = parse(h.substring(7));
            // ④ 认证通过，从 token 中提取用户 ID 和角色，构建 CurrentPayUser 存入上下文
            PayUserContext.set(new CurrentPayUser(((Number) x.get("sub")).longValue(), (String) x.get("role")));
            // ⑤ 放行请求
            c.doFilter(q, r);
        } catch (Exception e) {
            // ⑥ 认证失败（签名不一致 / 过期 / 格式错误等），返回 401
            deny(r);
        } finally {
            // ⑦ 请求处理完毕，清理 ThreadLocal 防止内存泄漏和线程复用串扰
            PayUserContext.clear();
        }
    }

    /**
     * 解析并校验 JWT Token
     * 与 user-service 的 JwtTokenService.parse() 使用相同逻辑：
     * ① 校验三段式格式（Header.Payload.Signature）
     * ② 重新计算 HMAC-SHA256 签名并与 token 中签名做恒定时间比较（MessageDigest.isEqual 防时序攻击）
     * ③ 反序列化 Payload JSON 为 Map
     * ④ 校验 exp 过期时间
     *
     * @param t 去掉 "Bearer " 前缀后的纯 token 字符串
     * @return JWT Payload 的 claims 键值对（含 sub、username、role、iat、exp）
     * @throws IllegalArgumentException token 格式错误、签名不匹配或已过期
     */
    private Map<String, Object> parse(String t) throws Exception {
        // ① 按 "." 分割为三段：Header、Payload、Signature
        String[] p = t.split("\\.");
        // ② 校验三段式格式 + 签名恒定时间比较
        if (p.length != 3 || !MessageDigest.isEqual(Base64.getUrlDecoder().decode(p[2]), sign(p[0] + "." + p[1])))
            throw new IllegalArgumentException();
        // ③ 解码并反序列化 Payload
        Map<String, Object> x = m.readValue(Base64.getUrlDecoder().decode(p[1]), new TypeReference<>() {});
        // ④ 校验过期时间：当前时间 >= exp 则 token 已过期
        if (Instant.now().getEpochSecond() >= ((Number) x.get("exp")).longValue())
            throw new IllegalArgumentException();
        return x;
    }

    /**
     * HMAC-SHA256 签名计算
     * @param t 签名输入（Header.Body 字符串）
     * @return 签名字节数组
     */
    private byte[] sign(String t) throws Exception {
        Mac x = Mac.getInstance("HmacSHA256");
        x.init(new SecretKeySpec(secret, "HmacSHA256"));
        return x.doFinal(t.getBytes(StandardCharsets.UTF_8));
    }

    /** 返回 401 Unauthorized 的 JSON 错误响应 */
    private void deny(HttpServletResponse r) throws IOException {
        r.setStatus(401);
        r.setContentType(MediaType.APPLICATION_JSON_VALUE);
        m.writeValue(r.getWriter(), new Error("AUTH_UNAUTHORIZED", "请先登录"));
    }

    /** 错误响应体：code + message */
    private record Error(String code, String message) {}
}
