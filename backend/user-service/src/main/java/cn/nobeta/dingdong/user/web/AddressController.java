package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AddressRequest;
import cn.nobeta.dingdong.user.api.UserResponses.AddressResponse;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.security.CurrentUserContext;
import cn.nobeta.dingdong.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 收货地址控制器 —— 收货地址增删改查链路的 REST 入口
 * 所有接口均需携带有效 JWT token，通过 CurrentUserContext 获取当前用户身份
 * 提供地址列表查询、新增、修改、删除四个端点，数据均隔离在当前用户范围内
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 查询收货地址列表 —— 返回当前用户所有未删除的收货地址
     * 按默认地址优先、最近创建优先排序
     * 对应前端 addressApi.list() → GET /api/addresses
     */
    @GetMapping
    public ApiResponse<List<AddressResponse>> list() {
        Long userId = CurrentUserContext.require().id();
        List<UserAddress> addressList = addressService.list(userId);
        List<AddressResponse> responseList = addressList.stream()
                .map(AddressResponse::from)
                .toList();
        return ApiResponse.success(responseList);
    }

    /**
     * 新增收货地址 —— 为当前用户创建一条新的收货地址记录
     * 若设置为默认地址，会先清除该用户已有的默认地址标记
     * 返回 HTTP 201 状态码
     * 对应前端 addressApi.create() → POST /api/addresses
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        Long userId = CurrentUserContext.require().id();
        UserAddress saved = addressService.create(userId, request);
        return ApiResponse.success(AddressResponse.from(saved));
    }

    /**
     * 修改收货地址 —— 更新指定 ID 的收货地址信息
     * 先校验地址归属（防止跨用户操作），若设为默认则先清除旧默认标记
     * 对应前端 addressApi.update() → PUT /api/addresses/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody AddressRequest request) {
        Long userId = CurrentUserContext.require().id();
        UserAddress updated = addressService.update(userId, id, request);
        return ApiResponse.success(AddressResponse.from(updated));
    }

    /**
     * 删除收货地址 —— 逻辑删除指定 ID 的地址记录（deleted=1）
     * 先校验地址归属，属于当前用户方可删除
     * 对应前端 addressApi.remove() → DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = CurrentUserContext.require().id();
        addressService.delete(userId, id);
        return ApiResponse.success(null);
    }

}
