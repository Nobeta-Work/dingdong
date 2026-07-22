package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private final StringRedisTemplate redisTemplate;
    private static final Random RANDOM = new Random();
    private static final long CODE_EXPIRE_SECONDS = 300;
    private static final long RETRY_AFTER_SECONDS = 60;

    public SmsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void sendCode(String phone, String scene) {
        String rateKey = "sms:rate:" + phone;
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

        System.out.println("【模拟短信】场景: " + scene + " 手机号: " + phone + " 验证码: " + code);
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
}
