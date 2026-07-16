package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AddressRequest;
import cn.nobeta.dingdong.user.api.UserResponses.AddressResponse;
import cn.nobeta.dingdong.user.security.CurrentUserContext;
import cn.nobeta.dingdong.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addressService;
    public AddressController(AddressService addressService) { this.addressService = addressService; }
    @GetMapping public ApiResponse<List<AddressResponse>> list() { return ApiResponse.success(addressService.list(CurrentUserContext.require().id()).stream().map(AddressResponse::from).toList()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<AddressResponse> create(@Valid @RequestBody AddressRequest request) { return ApiResponse.success(AddressResponse.from(addressService.create(CurrentUserContext.require().id(), request))); }
    @PutMapping("/{id}") public ApiResponse<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) { return ApiResponse.success(AddressResponse.from(addressService.update(CurrentUserContext.require().id(), id, request))); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { addressService.delete(CurrentUserContext.require().id(), id); return ApiResponse.success(null); }
}
