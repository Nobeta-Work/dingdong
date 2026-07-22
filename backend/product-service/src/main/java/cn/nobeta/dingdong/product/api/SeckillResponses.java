package cn.nobeta.dingdong.product.api;
import cn.nobeta.dingdong.product.domain.SeckillOrder;
public final class SeckillResponses {
    private SeckillResponses() { }
    public record Accepted(String requestId, String status, Long remainingStock) { }
    public record Result(String requestId, String status, Long orderId) {
        public static Result success(SeckillOrder order){return new Result(order.getRequestId(),order.getStatus(),order.getId());}
    }
    public record Consistency(Long activityId, int initialStock, long redisStock, int databaseStock,
            long successfulOrders, long pendingMessages, boolean consistent) { }
}
