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

/** Minimal, dependency-free HS256 JWT implementation for the development baseline. */
@Service
public class JwtTokenService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expiresInSeconds;

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret,
                           @Value("${security.jwt.expires-in-seconds:7200}") long expiresInSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiresInSeconds = expiresInSeconds;
    }

    public String create(CurrentUser user) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.id());
            payload.put("username", user.username());
            payload.put("role", user.role());
            payload.put("iat", now);
            payload.put("exp", now + expiresInSeconds);
            String header = ENCODER.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String body = ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = header + "." + body;
            return signingInput + "." + ENCODER.encodeToString(sign(signingInput));
        } catch (Exception exception) {
            throw new IllegalStateException("无法生成登录令牌", exception);
        }
    }

    public CurrentUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw invalid();
            byte[] actualSignature = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), actualSignature)) throw invalid();
            Map<String, Object> claims = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() { });
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiresAt) throw new BusinessException("AUTH_TOKEN_EXPIRED", "登录已过期，请重新登录");
            return new CurrentUser(((Number) claims.get("sub")).longValue(), (String) claims.get("username"), (String) claims.get("role"));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    public long getExpiresInSeconds() { return expiresInSeconds; }
    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }
    private BusinessException invalid() { return new BusinessException("AUTH_TOKEN_INVALID", "无效的登录令牌"); }
}
