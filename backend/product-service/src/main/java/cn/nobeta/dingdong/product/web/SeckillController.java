package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.SeckillRequests;
import cn.nobeta.dingdong.product.api.SeckillResponses;
import cn.nobeta.dingdong.product.domain.SeckillActivity;
import cn.nobeta.dingdong.product.security.ProductUserContext;
import cn.nobeta.dingdong.product.service.SeckillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    private final SeckillService service; public SeckillController(SeckillService service){this.service=service;}
    @GetMapping("/activities") public ApiResponse<List<SeckillActivity>> active(){return ApiResponse.success(service.active());}
    @PostMapping("/activities/{id}/orders") @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<SeckillResponses.Accepted> purchase(@PathVariable Long id,@Valid @RequestBody SeckillRequests.PurchaseRequest request){return ApiResponse.success(service.purchase(ProductUserContext.require().id(),id,request.requestId()));}
    @GetMapping("/orders/{requestId}") public ApiResponse<SeckillResponses.Result> result(@PathVariable String requestId){return ApiResponse.success(service.result(ProductUserContext.require().id(),requestId));}
}
