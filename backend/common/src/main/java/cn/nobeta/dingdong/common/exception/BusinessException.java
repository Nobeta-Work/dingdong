package cn.nobeta.dingdong.common.exception;

/**
 * Expected business-rule failure exposed through the unified API envelope.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
