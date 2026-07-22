package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AuthRequests;
import cn.nobeta.dingdong.user.api.UserResponses;
import cn.nobeta.dingdong.user.security.CurrentUserContext;
import cn.nobeta.dingdong.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料控制器 —— 用户资料查询与修改链路的 REST 入口
 * 所有接口均需携带有效 JWT token，由 JwtAuthenticationFilter 校验后通过 CurrentUserContext 获取当前用户身份
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询当前登录用户的个人资料
     * 从 CurrentUserContext 获取当前用户 ID → 调用 UserService.profile() 查库 → 通过 UserProfile.from() 脱敏后返回
     * 对应前端 authApi.me() → GET /api/users/me
     */
    @GetMapping
    public ApiResponse<UserResponses.UserProfile> profile() {
        Long userId = CurrentUserContext.require().id();
        UserResponses.UserProfile profile = UserResponses.UserProfile.from(
                userService.profile(userId)
        );
        return ApiResponse.success(profile);
    }

    /**
     * 修改当前登录用户的个人资料
     * 从 CurrentUserContext 获取当前用户 ID → 调用 UserService.updateProfile() 校验并更新 → 脱敏后返回更新后的用户信息
     * 昵称必填，手机号/邮箱/头像URL为可选字段；手机号和邮箱需保证全局唯一
     * 对应前端 authApi.updateMe() → PUT /api/users/me
     */
    @PutMapping
    public ApiResponse<UserResponses.UserProfile> update(
            @Valid @RequestBody AuthRequests.ProfileRequest request) {
        Long userId = CurrentUserContext.require().id();
        UserResponses.UserProfile profile = UserResponses.UserProfile.from(
                userService.updateProfile(userId, request)
        );
        return ApiResponse.success(profile);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody AuthRequests.ChangePasswordRequest request) {
        Long userId = CurrentUserContext.require().id();
        userService.changePassword(userId, request);
        return ApiResponse.success(null);
    }

}
