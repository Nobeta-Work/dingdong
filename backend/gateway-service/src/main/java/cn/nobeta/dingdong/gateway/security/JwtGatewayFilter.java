package cn.nobeta.dingdong.gateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * 网关层 JWT 全局过滤器 —— 登录链路中所有非公开 API 请求的统一认证入口
 * 在前端请求到达后端服务之前，Gateway 首先在此拦截并校验 JWT：
 * ① 判断请求路径是否为公开接口（登录/注册/商品浏览等无需登录的接口直接放行）
 * ② 从 Authorization 请求头提取 Bearer token
 * ③ 自实现 HS256 签名校验、有效期校验（与 user-service 的 JwtTokenService 逻辑一致）
 * ④ 校验通过后，将用户 ID 和角色注入请求头（X-User-Id, X-User-Role）透传给下游服务
 * ⑤ 校验失败返回 401 Unauthorized
 *
 * 注意：此过滤器复用与 user-service 相同的 JWT secret 密钥和解析逻辑，
 *       确保只有一个签发源（user-service 登录接口）时多服务可独立校验 token。
 */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final ObjectMapper objectMapper;
    /** HMAC-SHA256 签名密钥，与 user-service 的 JwtTokenService 使用相同值 */
    private final byte[] secret;

    public JwtGatewayFilter(ObjectMapper objectMapper,
            @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 全局过滤逻辑 —— 登录链路中每次 API 请求必经的认证检查点
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ① 判断是否为公开接口：auth/**（登录注册）、actuator/**（健康检查）、GET 商品/分类/品牌（浏览）
        if (isPublic(exchange)) return chain.filter(exchange);
        // ② 从请求头获取 Authorization 字段
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        // ③ 校验格式：必须以 "Bearer " 开头
        if (header == null || !header.startsWith("Bearer ")) return reject(exchange, "AUTH_UNAUTHORIZED", "请先登录");
        try {
            // ④ 去掉 "Bearer " 前缀（7 个字符），解析校验 JWT
            Map<String, Object> c = parse(header.substring(7));
            // ⑤ 认证通过，将用户 ID 和角色写入 X-User-Id / X-User-Role 请求头，透传给下游微服务
            return chain.filter(exchange.mutate().request(r -> r
                    .header("X-User-Id", String.valueOf(c.get("sub")))
                    .header("X-User-Role", String.valueOf(c.get("role")))).build());
        } catch (Exception e) {
            return reject(exchange, "AUTH_TOKEN_INVALID", "无效或已过期的登录令牌");
        }
    }

    /**
     * 判断请求是否为公开接口（无需登录 token）
     * 公开路径：
     * - /actuator/** ：健康检查端点
     * - /api/auth/** ：登录和注册接口
     * - GET /api/categories、/api/brands、/api/products ：商品浏览
     */
    private boolean isPublic(ServerWebExchange e) {
        String p = e.getRequest().getPath().value();
        if (p.startsWith("/actuator/") || p.startsWith("/api/auth/")) return true;
        if (e.getRequest().getMethod() == HttpMethod.GET && p.startsWith("/api/seckill/activities")) return true;
        return e.getRequest().getMethod() == HttpMethod.GET
                && (p.equals("/api/categories") || p.equals("/api/brands") || p.startsWith("/api/products"));
    }

    /**
     * 解析并校验 JWT（逻辑与 user-service 的 JwtTokenService.parse() 一致）
     * ① 校验三段式格式
     * ② 重新计算 HMAC-SHA256 签名并做恒定时间比较
     * ③ 校验 exp 过期时间
     */
    private Map<String, Object> parse(String token) throws Exception {
        // 按 "." 分割三段
        String[] p = token.split("\\.");
        if (p.length != 3) throw new IllegalArgumentException();
        // 重新计算签名并做恒定时间比较（防止时序攻击）
        if (!MessageDigest.isEqual(Base64.getUrlDecoder().decode(p[2]), sign(p[0] + "." + p[1])))
            throw new IllegalArgumentException();
        // 解码 Payload
        Map<String, Object> c = objectMapper.readValue(Base64.getUrlDecoder().decode(p[1]),
                new TypeReference<>() { });
        // 校验过期时间
        if (Instant.now().getEpochSecond() >= ((Number) c.get("exp")).longValue())
            throw new IllegalArgumentException();
        return c;
    }

    /** HMAC-SHA256 签名计算 */
    private byte[] sign(String t) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(t.getBytes(StandardCharsets.UTF_8));
    }

    /** 返回 401 Unauthorized 的 JSON 响应 */
    private Mono<Void> reject(ServerWebExchange e, String code, String message) {
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Map.of("code", code, "message", message));
        } catch (Exception ex) {
            body = "{\"code\":\"AUTH_UNAUTHORIZED\"}".getBytes(StandardCharsets.UTF_8);
        }
        e.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        e.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = e.getResponse().bufferFactory().wrap(body);
        return e.getResponse().writeWith(Mono.just(buffer));
    }

    /** 过滤器优先级：-100，确保在大多数 Gateway 过滤器之前执行 */
    @Override
    public int getOrder() { return -100; }
}
