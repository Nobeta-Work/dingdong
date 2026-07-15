package cn.nobeta.dingdong.common.api;

/**
 * Unified REST response envelope shared by the services.
 *
 * @param <T> response payload type
 */
public record ApiResponse<T>(String code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("OK", "success", data, null);
    }
}
