package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.user.api.AuthRequests;
import cn.nobeta.dingdong.user.api.UserResponses;
import cn.nobeta.dingdong.user.security.CurrentUserContext;
import cn.nobeta.dingdong.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @GetMapping
    public ApiResponse<UserResponses.UserProfile> profile() { return ApiResponse.success(UserResponses.UserProfile.from(userService.profile(CurrentUserContext.require().id()))); }
    @PutMapping
    public ApiResponse<UserResponses.UserProfile> update(@Valid @RequestBody AuthRequests.ProfileRequest request) { return ApiResponse.success(UserResponses.UserProfile.from(userService.updateProfile(CurrentUserContext.require().id(), request))); }
}
