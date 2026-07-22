package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.InventoryRequests;
import cn.nobeta.dingdong.product.domain.InventoryChangeLog;
import cn.nobeta.dingdong.product.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {
    private final InventoryService service;
    public AdminInventoryController(InventoryService service) { this.service = service; }

    @GetMapping("/changes")
    public ApiResponse<InventoryService.Page> changes(@RequestParam(required=false) Long skuId,
            @RequestParam(required=false) String businessType, @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size) {
        return ApiResponse.success(service.page(skuId, businessType, page, size));
    }

    @PostMapping("/skus/{skuId}/adjustments")
    public ApiResponse<InventoryChangeLog> adjust(@PathVariable Long skuId,
            @Valid @RequestBody InventoryRequests.AdjustmentRequest request) {
        return ApiResponse.success(service.adjust(skuId, request.requestId(), request.quantityDelta(), request.reason()));
    }
}
