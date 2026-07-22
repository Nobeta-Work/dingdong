package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    private SmsService service;

    @BeforeEach
    void setUp() {
        service = new SmsService(redisTemplate, "mock");
    }

    @Test
    void mockCodeIsReturnedAndStoredWithExpiryAndRateLimit() {
        String phone = "13800138000";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey("sms:rate:login:" + phone)).thenReturn(false);

        SmsService.SendCodeResult result = service.sendCode(phone, "login");

        assertTrue(result.mock());
        assertTrue(result.debugCode().matches("\\d{6}"));
        assertEquals(300, result.expireSeconds());
        assertEquals(60, result.retryAfterSeconds());
        verify(valueOperations).set("sms:code:login:" + phone, result.debugCode(), 300, TimeUnit.SECONDS);
        verify(valueOperations).set("sms:rate:login:" + phone, "1", 60, TimeUnit.SECONDS);
    }

    @Test
    void unsupportedSceneIsRejected() {
        assertThrows(BusinessException.class, () -> service.sendCode("13800138000", "unknown"));
    }
}
