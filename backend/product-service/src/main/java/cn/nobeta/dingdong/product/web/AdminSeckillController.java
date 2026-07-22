package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.SeckillRequests;
import cn.nobeta.dingdong.product.api.SeckillResponses;
import cn.nobeta.dingdong.product.domain.SeckillActivity;
import cn.nobeta.dingdong.product.service.SeckillService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/seckill/activities")
public class AdminSeckillController {
    private final SeckillService service; public AdminSeckillController(SeckillService service){this.service=service;}
    @GetMapping public ApiResponse<List<SeckillActivity>> all(){return ApiResponse.success(service.all());}
    @PostMapping public ApiResponse<SeckillActivity> create(@Valid @RequestBody SeckillRequests.CreateActivityRequest request){return ApiResponse.success(service.create(request));}
    @PostMapping("/{id}/activate") public ApiResponse<SeckillActivity> activate(@PathVariable Long id){return ApiResponse.success(service.activate(id));}
    @PostMapping("/{id}/warmup") public ApiResponse<Void> warmup(@PathVariable Long id){service.warmup(id);return ApiResponse.success(null);}
    @PostMapping("/{id}/end") public ApiResponse<SeckillActivity> end(@PathVariable Long id){return ApiResponse.success(service.end(id));}
    @GetMapping("/{id}/consistency") public ApiResponse<SeckillResponses.Consistency> consistency(@PathVariable Long id){return ApiResponse.success(service.consistency(id));}
}
