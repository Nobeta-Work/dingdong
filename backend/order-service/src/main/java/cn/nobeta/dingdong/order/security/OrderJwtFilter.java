package cn.nobeta.dingdong.order.security;
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
 * JWT 认证过滤器
 * 拦截所有 /api/* 请求，解析 Bearer Token 并设置用户上下文。
 * 使用 HMAC-SHA256 算法验证 Token 签名和有效期。
 */
@Component
public class OrderJwtFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;
    private final byte[] secret;

    public OrderJwtFilter(ObjectMapper mapper,
                          @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret) {
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** 仅拦截 /api/* 路径，放行静态资源 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    /**
     * 过滤请求：解析 JWT → 设置用户上下文 → 放行 → 清理上下文
     * @param req HTTP 请求
     * @param res HTTP 响应
     * @param chain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        // 校验 Authorization 头格式
        if (header == null || !header.startsWith("Bearer ")) {
            reject(res, "AUTH_UNAUTHORIZED", "请先登录");
            return;
        }
        try {
            // 解析 Token 并设置用户上下文
            Map<String, Object> claims = parse(header.substring(7));
            OrderUserContext.set(new CurrentOrderUser(
                    ((Number) claims.get("sub")).longValue(),
                    (String) claims.get("role")
            ));
            chain.doFilter(req, res);
        } catch (Exception e) {
            reject(res, "AUTH_TOKEN_INVALID", "无效或已过期的登录令牌");
        } finally {
            OrderUserContext.clear();
        }
    }

    /**
     * 解析并验证 JWT Token
        * 校验流程：
        * 1. 校验格式（三段式）
        * 2. 校验签名（HMAC-SHA256）
        * 3. 校验有效期（exp 字段）
     */
    private Map<String, Object> parse(String token) throws Exception {
        String[] p = token.split("\\.");
        if (p.length != 3) throw new IllegalArgumentException();
        // 校验签名
        if (!MessageDigest.isEqual(Base64.getUrlDecoder().decode(p[2]), sign(p[0] + "." + p[1])))
            throw new IllegalArgumentException();
        // 解析 Payload
        Map<String, Object> c = mapper.readValue(
                Base64.getUrlDecoder().decode(p[1]),
                new TypeReference<>() {}
        );
        // 校验过期时间
        if (Instant.now().getEpochSecond() >= ((Number) c.get("exp")).longValue())
            throw new IllegalArgumentException();
        return c;
    }

    /** 计算 HMAC-SHA256 签名 */
    private byte[] sign(String text) throws Exception {
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(new SecretKeySpec(secret, "HmacSHA256"));
        return m.doFinal(text.getBytes(StandardCharsets.UTF_8));
    }

    /** 返回 401 错误响应 */
    private void reject(HttpServletResponse r, String code, String message) throws IOException {
        r.setStatus(401);
        r.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(r.getWriter(), new ErrorBody(code, message));
    }

    private record ErrorBody(String code, String message) {}
}
