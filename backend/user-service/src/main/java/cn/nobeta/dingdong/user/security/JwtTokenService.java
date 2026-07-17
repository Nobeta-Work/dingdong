package cn.nobeta.dingdong.user.security;

import cn.nobeta.dingdong.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT Token 签发与校验服务 —— 登录链路中 token 生成和后续验证的核心组件
 * 使用 HMAC-SHA256 (HS256) 算法自实现 JWT，无任何第三方依赖。
 * secret 密钥通过配置文件注入，默认值仅适用于本地开发。
 */
@Service
public class JwtTokenService {
    /** Base64 URL 编码器（无填充），用于 JWT Header 和 Payload 编码 */
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    /** Base64 URL 解码器，用于 JWT 校验时解码 Header、Payload 和 Signature */
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper objectMapper;
    /** HMAC-SHA256 签名密钥的字节形式，由配置中的明文字符串转换 */
    private final byte[] secret;
    /** Token 有效期（秒），默认 7200 秒（2 小时） */
    private final long expiresInSeconds;

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret,
                           @Value("${security.jwt.expires-in-seconds:7200}") long expiresInSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiresInSeconds = expiresInSeconds;
    }

    /**
     * 签发 JWT Token —— 登录成功后调用
     * 流程：
     * ① 构造 Payload，包含 sub（用户ID）、username、role、iat（签发时间）、exp（过期时间）
     * ② 将固定 Header {"alg":"HS256","typ":"JWT"} 做 Base64Url 编码
     * ③ 将 Payload JSON 做 Base64Url 编码
     * ④ 以 Header.Body 为签名输入，使用 HMAC-SHA256 生成签名并 Base64Url 编码
     * ⑤ 返回格式：Header.Body.Signature
     *
     * @param user 当前登录用户信息（id、username、role）
     * @return 完整的 JWT Token 字符串
     */
    public String create(CurrentUser user) {
        try {
            // 获取当前时间戳（秒）作为签发时间
            long now = Instant.now().getEpochSecond();
            // 构造 JWT Payload（载荷）
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.id());              // subject：用户 ID
            payload.put("username", user.username());    // 用户名
            payload.put("role", user.role());            // 用户角色
            payload.put("iat", now);                     // issued at：签发时间
            payload.put("exp", now + expiresInSeconds);  // expiration：过期时间
            // 编码固定 Header
            String header = ENCODER.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            // 编码 Payload
            String body = ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            // 构建签名输入：Header.Body
            String signingInput = header + "." + body;
            // 对签名输入做 HMAC-SHA256，编码后拼接到末尾
            return signingInput + "." + ENCODER.encodeToString(sign(signingInput));
        } catch (Exception exception) {
            throw new IllegalStateException("无法生成登录令牌", exception);
        }
    }

    /**
     * 解析并校验 JWT Token —— 每次认证请求调用
     * 流程：
     * ① 按 "." 分割三段式 token
     * ② 重新计算签名并与 token 中签名做恒定时间比较（防止时序攻击）
     * ③ 反序列化 Payload JSON
     * ④ 校验 exp 是否已过期
     * ⑤ 校验通过后构造 CurrentUser 返回
     *
     * @param token Bearer 之后的 JWT 字符串
     * @return 解析出的 CurrentUser 对象
     * @throws BusinessException token 无效或已过期
     */
    public CurrentUser parse(String token) {
        try {
            // ① 按 "." 分割为三段：Header、Payload、Signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw invalid();
            // ② 解码签名并做恒定时间比较（MessageDigest.isEqual 防止时序侧信道攻击）
            byte[] actualSignature = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), actualSignature)) throw invalid();
            // ③ 解码并反序列化 Payload
            Map<String, Object> claims = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() { });
            // ④ 校验过期时间
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiresAt) throw new BusinessException("AUTH_TOKEN_EXPIRED", "登录已过期，请重新登录");
            // ⑤ 校验通过，构建 CurrentUser 对象返回
            return new CurrentUser(((Number) claims.get("sub")).longValue(), (String) claims.get("username"), (String) claims.get("role"));
        } catch (BusinessException exception) {
            // 业务异常（如过期）直接透传
            throw exception;
        } catch (Exception exception) {
            // 其他异常（格式错误、签名不匹配等）统一视为无效 token
            throw invalid();
        }
    }

    /** 获取 token 有效期秒数（供登录接口返回 expiresIn） */
    public long getExpiresInSeconds() { return expiresInSeconds; }

    /**
     * HMAC-SHA256 签名计算
     * @param value 签名输入（Header.Body）
     * @return 签名字节数组
     */
    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    /** 构造无效 token 的业务异常 */
    private BusinessException invalid() { return new BusinessException("AUTH_TOKEN_INVALID", "无效的登录令牌"); }
}
