package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AuthRequests;
import cn.nobeta.dingdong.user.api.UserResponses;
import cn.nobeta.dingdong.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }
    /**
     * 用户注册接口
     * 接收注册请求，委托 UserService 完成注册逻辑，
     * 将注册成功的用户实体转换为脱敏的 UserProfile 后封装为统一响应返回
     */
    @PostMapping("/register")
    public ApiResponse<UserResponses.UserProfile> register(@Valid @RequestBody AuthRequests.RegisterRequest request) {
        // 调用注册服务完成用户创建，将领域实体转为响应 DTO（脱敏处理），包装为统一成功响应
        return ApiResponse.success(UserResponses.UserProfile.from(userService.register(request)));
    }
    @PostMapping("/login")
    public ApiResponse<UserResponses.LoginResponse> login(@Valid @RequestBody AuthRequests.LoginRequest request) {
        UserService.LoginResult result = userService.login(request);
        return ApiResponse.success(new UserResponses.LoginResponse(result.token(), result.expiresIn(), UserResponses.UserProfile.from(result.user())));
    }
}
