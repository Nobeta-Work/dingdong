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

/** Gateway-level JWT validation for every non-public API route. */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final ObjectMapper objectMapper; private final byte[] secret;
    public JwtGatewayFilter(ObjectMapper objectMapper,@Value("${security.jwt.secret:dingdong-change-this-development-secret-at-least-32-bytes}") String secret){this.objectMapper=objectMapper;this.secret=secret.getBytes(StandardCharsets.UTF_8);}
    @Override public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){
        if(isPublic(exchange))return chain.filter(exchange);
        String header=exchange.getRequest().getHeaders().getFirst("Authorization");
        if(header==null||!header.startsWith("Bearer "))return reject(exchange,"AUTH_UNAUTHORIZED","请先登录");
        try{Map<String,Object> c=parse(header.substring(7));return chain.filter(exchange.mutate().request(r->r.header("X-User-Id",String.valueOf(c.get("sub"))).header("X-User-Role",String.valueOf(c.get("role")))).build());}
        catch(Exception e){return reject(exchange,"AUTH_TOKEN_INVALID","无效或已过期的登录令牌");}
    }
    private boolean isPublic(ServerWebExchange e){String p=e.getRequest().getPath().value();if(p.startsWith("/actuator/")||p.startsWith("/api/auth/"))return true;return e.getRequest().getMethod()==HttpMethod.GET&&(p.equals("/api/categories")||p.equals("/api/brands")||p.startsWith("/api/products"));}
    private Map<String,Object> parse(String token)throws Exception{String[] p=token.split("\\.");if(p.length!=3)throw new IllegalArgumentException();if(!MessageDigest.isEqual(Base64.getUrlDecoder().decode(p[2]),sign(p[0]+"."+p[1])))throw new IllegalArgumentException();Map<String,Object> c=objectMapper.readValue(Base64.getUrlDecoder().decode(p[1]),new TypeReference<>(){});if(Instant.now().getEpochSecond()>=((Number)c.get("exp")).longValue())throw new IllegalArgumentException();return c;}
    private byte[] sign(String t)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return mac.doFinal(t.getBytes(StandardCharsets.UTF_8));}
    private Mono<Void> reject(ServerWebExchange e,String code,String message){byte[] body;try{body=objectMapper.writeValueAsBytes(Map.of("code",code,"message",message));}catch(Exception ex){body="{\"code\":\"AUTH_UNAUTHORIZED\"}".getBytes(StandardCharsets.UTF_8);}e.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);e.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);DataBuffer buffer=e.getResponse().bufferFactory().wrap(body);return e.getResponse().writeWith(Mono.just(buffer));}
    @Override public int getOrder(){return -100;}
}
