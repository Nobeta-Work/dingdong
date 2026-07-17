package cn.nobeta.dingdong.common.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 价格校验链路异常处理说明：
 * - BusinessException — 业务异常（如库存不足、SKU 不存在等），统一返回 400 Bad Request
 * - MethodArgumentNotValidException / ConstraintViolationException — 参数校验异常
 *   （如 SkuRequest.price 违反 @DecimalMin("0.01") 约束），返回字段级错误信息
 * - Exception — 未预期异常，返回 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常（含价格校验链中的库存不足、SKU 不可售等） */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理参数校验异常（含价格字段校验失败）
     * 当 SkuRequest#price() 的 @DecimalMin("0.01") 或 @Digits(integer=10,fraction=2)
     * 约束被违反时触发，返回形如 "price: 价格必须大于等于 0.01" 的错误信息。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception exception) {
        String message = "请求参数不合法";
        if (exception instanceof MethodArgumentNotValidException invalid
                && invalid.getBindingResult().getFieldError() != null) {
            FieldError error = invalid.getBindingResult().getFieldError();
            message = error.getField() + ": " + error.getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    /** 处理未预期的系统异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("SYSTEM_ERROR", "系统繁忙，请稍后重试"));
    }
}