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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    public JwtAuthenticationFilter(JwtTokenService tokenService, ObjectMapper objectMapper, UserMapper userMapper) { this.tokenService = tokenService; this.objectMapper = objectMapper; this.userMapper = userMapper; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.startsWith("/actuator/");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) { writeUnauthorized(response, "AUTH_UNAUTHORIZED", "请先登录"); return; }
        try {
            CurrentUser currentUser = tokenService.parse(authorization.substring(7));
            MallUser persistedUser = userMapper.findById(currentUser.id());
            if (persistedUser == null || !Integer.valueOf(1).equals(persistedUser.getStatus())) { writeUnauthorized(response, "AUTH_USER_DISABLED", "当前用户已被禁用"); return; }
            CurrentUserContext.set(currentUser);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            writeUnauthorized(response, "AUTH_TOKEN_INVALID", exception.getMessage());
        } finally { CurrentUserContext.clear(); }
    }
    private void writeUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorBody(code, message));
    }
    private record ErrorBody(String code, String message) { }
}
