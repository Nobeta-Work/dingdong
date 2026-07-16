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
    @PostMapping("/register")
    public ApiResponse<UserResponses.UserProfile> register(@Valid @RequestBody AuthRequests.RegisterRequest request) {
        return ApiResponse.success(UserResponses.UserProfile.from(userService.register(request)));
    }
    @PostMapping("/login")
    public ApiResponse<UserResponses.LoginResponse> login(@Valid @RequestBody AuthRequests.LoginRequest request) {
        UserService.LoginResult result = userService.login(request);
        return ApiResponse.success(new UserResponses.LoginResponse(result.token(), result.expiresIn(), UserResponses.UserProfile.from(result.user())));
    }
}
