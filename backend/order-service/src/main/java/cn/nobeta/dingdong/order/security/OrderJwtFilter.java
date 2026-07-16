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
@Component
public class OrderJwtFilter extends OncePerRequestFilter {
 private final ObjectMapper mapper; private final byte[] secret;
 public OrderJwtFilter(ObjectMapper mapper,@Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret){this.mapper=mapper;this.secret=secret.getBytes(StandardCharsets.UTF_8);}
 @Override protected boolean shouldNotFilter(HttpServletRequest request){return !request.getServletPath().startsWith("/api/");}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String header=req.getHeader("Authorization");if(header==null||!header.startsWith("Bearer ")){reject(res,"AUTH_UNAUTHORIZED","请先登录");return;}
  try{Map<String,Object> claims=parse(header.substring(7));OrderUserContext.set(new CurrentOrderUser(((Number)claims.get("sub")).longValue(),(String)claims.get("role")));chain.doFilter(req,res);}catch(Exception e){reject(res,"AUTH_TOKEN_INVALID","无效或已过期的登录令牌");}finally{OrderUserContext.clear();}
 }
 private Map<String,Object> parse(String token)throws Exception{String[] p=token.split("\\.");if(p.length!=3)throw new IllegalArgumentException();if(!MessageDigest.isEqual(Base64.getUrlDecoder().decode(p[2]),sign(p[0]+"."+p[1])))throw new IllegalArgumentException();Map<String,Object> c=mapper.readValue(Base64.getUrlDecoder().decode(p[1]),new TypeReference<>(){});if(Instant.now().getEpochSecond()>=((Number)c.get("exp")).longValue())throw new IllegalArgumentException();return c;}
 private byte[] sign(String text)throws Exception{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(secret,"HmacSHA256"));return m.doFinal(text.getBytes(StandardCharsets.UTF_8));}
 private void reject(HttpServletResponse r,String code,String message)throws IOException{r.setStatus(401);r.setContentType(MediaType.APPLICATION_JSON_VALUE);mapper.writeValue(r.getWriter(),new ErrorBody(code,message));}
 private record ErrorBody(String code,String message){}
}
