package cn.nobeta.dingdong.user.security;

import cn.nobeta.dingdong.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {
    @Test
    void createsAndParsesSignedJwt() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), "test-secret-that-is-long-enough-for-hmac", 60);
        String token = service.create(new CurrentUser(7L, "nobeta", "USER"));
        CurrentUser user = service.parse(token);
        assertEquals(7L, user.id());
        assertEquals("nobeta", user.username());
        assertEquals("USER", user.role());
    }
    @Test
    void rejectsTamperedJwt() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), "test-secret-that-is-long-enough-for-hmac", 60);
        String token = service.create(new CurrentUser(7L, "nobeta", "USER"));
        int signatureStart = token.lastIndexOf('.') + 1;
        String tampered = token.substring(0, signatureStart) + (token.charAt(signatureStart) == 'a' ? 'b' : 'a') + token.substring(signatureStart + 1);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.parse(tampered));
        assertEquals("AUTH_TOKEN_INVALID", exception.getCode());
    }
    @Test
    void seededAdminHashMatchesDevelopmentPassword() {
        var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        assertTrue(encoder.matches("password", "$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2"));
    }
}
