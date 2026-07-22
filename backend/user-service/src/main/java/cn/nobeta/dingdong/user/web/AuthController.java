package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AuthRequests;
import cn.nobeta.dingdong.user.api.UserResponses;
import cn.nobeta.dingdong.user.service.SmsService;
import cn.nobeta.dingdong.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 —— 用户登录功能链路入口
 * 所有 /api/auth/** 请求经 Gateway 路由到此处（此路径为公开接口，无需 token）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SmsService smsService;

    public AuthController(UserService userService, SmsService smsService) {
        this.userService = userService;
        this.smsService = smsService;
    }

    /**
     * 用户注册接口
     * 接收注册请求，委托 UserService 完成注册逻辑，
     * 将注册成功的用户实体转换为脱敏的 UserProfile 后封装为统一响应返回
     */
    @PostMapping("/register")
    public ApiResponse<UserResponses.UserProfile> register(
            @Valid @RequestBody AuthRequests.RegisterRequest request) {
        UserResponses.UserProfile profile = UserResponses.UserProfile.from(
                userService.register(request)
        );
        return ApiResponse.success(profile);
    }

    /**
     * 用户登录接口 —— 登录链路核心入口
     * 接收用户名 + 密码，委托 UserService.login() 完成凭证校验与 JWT 签发，
     * 将生成的 token、有效期、脱敏用户信息封装为 LoginResponse 统一响应
     *
     * @param request 包含 username 和 password 的登录请求体
     * @return 包含 JWT token、过期秒数、用户信息的统一响应
     */
    @PostMapping("/login")
    public ApiResponse<UserResponses.LoginResponse> login(
            @Valid @RequestBody AuthRequests.LoginRequest request) {
        UserService.LoginResult result = userService.login(request);
        UserResponses.UserProfile profile = UserResponses.UserProfile.from(result.user());
        UserResponses.LoginResponse loginResponse = new UserResponses.LoginResponse(
                result.token(), result.expiresIn(), profile
        );
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/sms/code")
    public ApiResponse<SmsService.SendCodeResult> sendSmsCode(@Valid @RequestBody AuthRequests.SendSmsCodeRequest request) {
        return ApiResponse.success(smsService.sendCode(request.phone(), request.scene()));
    }

    @PostMapping("/sms/login")
    public ApiResponse<UserResponses.LoginResponse> smsLogin(
            @Valid @RequestBody AuthRequests.SmsLoginRequest request) {
        UserService.LoginResult result = userService.smsLogin(request.phone(), request.code());
        UserResponses.UserProfile profile = UserResponses.UserProfile.from(result.user());
        UserResponses.LoginResponse loginResponse = new UserResponses.LoginResponse(
                result.token(), result.expiresIn(), profile
        );
        return ApiResponse.success(loginResponse);
    }

}
