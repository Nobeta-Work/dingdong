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

/** Verifies the same HS256 JWT issued by user-service before allowing product administration. */
@Component
public class AdminJwtFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper; private final byte[] secret;
    public AdminJwtFilter(ObjectMapper objectMapper, @Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret) { this.objectMapper=objectMapper; this.secret=secret.getBytes(StandardCharsets.UTF_8); }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getServletPath().startsWith("/api/admin/"); }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String authorization=request.getHeader("Authorization");
        if(authorization==null || !authorization.startsWith("Bearer ")) { reject(response,"AUTH_UNAUTHORIZED","请先登录"); return; }
        try {
            Map<String,Object> claims=parse(authorization.substring(7));
            if(!"ADMIN".equals(claims.get("role"))) { reject(response,"AUTH_FORBIDDEN","需要管理员权限"); return; }
            chain.doFilter(request,response);
        } catch (Exception exception) { reject(response,"AUTH_TOKEN_INVALID","无效或已过期的登录令牌"); }
    }
    private Map<String,Object> parse(String token) throws Exception {
        String[] parts=token.split("\\."); if(parts.length!=3) throw new IllegalArgumentException();
        byte[] signature=Base64.getUrlDecoder().decode(parts[2]);
        if(!MessageDigest.isEqual(signature, sign(parts[0]+"."+parts[1]))) throw new IllegalArgumentException();
        Map<String,Object> claims=objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]),new TypeReference<>(){});
        if(Instant.now().getEpochSecond()>=((Number)claims.get("exp")).longValue()) throw new IllegalArgumentException();
        return claims;
    }
    private byte[] sign(String text) throws Exception { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret,"HmacSHA256")); return mac.doFinal(text.getBytes(StandardCharsets.UTF_8)); }
    private void reject(HttpServletResponse response,String code,String message) throws IOException { response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);response.setContentType(MediaType.APPLICATION_JSON_VALUE);objectMapper.writeValue(response.getWriter(),new ErrorBody(code,message)); }
    private record ErrorBody(String code,String message) { }
}
