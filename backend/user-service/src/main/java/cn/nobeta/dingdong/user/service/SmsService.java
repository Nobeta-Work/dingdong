package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private final StringRedisTemplate redisTemplate;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> SUPPORTED_SCENES = Set.of("login", "register", "change-phone");
    private static final long CODE_EXPIRE_SECONDS = 300;
    private static final long RETRY_AFTER_SECONDS = 60;
    private final boolean mockMode;

    public SmsService(StringRedisTemplate redisTemplate, @Value("${dingdong.sms.mode:mock}") String mode) {
        this.redisTemplate = redisTemplate;
        this.mockMode = "mock".equalsIgnoreCase(mode);
    }

    /**
     * 当前项目尚未通过第三方短信服务的签名与模板认证，因此开发环境只提供 Mock 短信：
     * 验证码仍按真实链路写入 Redis、执行过期和频控，但同时返回给前端用于演示闭环。
     * 后续接入真实短信渠道时必须移除响应中的 debugCode。
     */
    public SendCodeResult sendCode(String phone, String scene) {
        if (!mockMode) {
            throw new BusinessException("SMS_PROVIDER_UNAVAILABLE", "第三方短信服务尚未完成认证");
        }
        if (!SUPPORTED_SCENES.contains(scene)) {
            throw new BusinessException("SMS_SCENE_INVALID", "不支持的验证码场景");
        }
        String rateKey = "sms:rate:" + scene + ":" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BusinessException("SMS_RATE_LIMITED", "发送太频繁，请60秒后再试");
        }

        String code = generateCode();

        redisTemplate.opsForValue().set(
                "sms:code:" + scene + ":" + phone,
                code,
                CODE_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                rateKey,
                "1",
                RETRY_AFTER_SECONDS,
                TimeUnit.SECONDS
        );

        return new SendCodeResult(true, code, CODE_EXPIRE_SECONDS, RETRY_AFTER_SECONDS);
    }

    public boolean verifyCode(String phone, String scene, String code) {
        String key = "sms:code:" + scene + ":" + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) return false;
        if (storedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    public record SendCodeResult(boolean mock, String debugCode, long expireSeconds, long retryAfterSeconds) { }
}
