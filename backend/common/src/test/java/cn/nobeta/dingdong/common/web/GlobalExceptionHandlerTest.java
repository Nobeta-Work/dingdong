package cn.nobeta.dingdong.common.web;

import cn.nobeta.dingdong.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void preservesBusinessCodeAndMessage() {
        var response = handler.handleBusiness(new BusinessException("SECKILL_SOLD_OUT", "活动库存已售罄"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("SECKILL_SOLD_OUT", response.getBody().code());
        assertEquals("活动库存已售罄", response.getBody().message());
    }

    @Test
    void mapsAuthenticationAndUploadErrors() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleBusiness(new BusinessException("AUTH_UNAUTHORIZED", "请先登录")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleBusiness(new BusinessException("AUTH_FORBIDDEN", "需要管理员权限")).getStatusCode());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                handler.handleBusiness(new BusinessException("FILE_TYPE_INVALID", "仅支持图片")).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY,
                handler.handleBusiness(new BusinessException("FILE_UPLOAD_FAILED", "上传失败")).getStatusCode());
    }
}
