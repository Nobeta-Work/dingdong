package cn.nobeta.dingdong.common.api;

/**
 * Lightweight payload returned by each service baseline endpoint.
 */
public record ServiceInfo(String service, String status) {
}
