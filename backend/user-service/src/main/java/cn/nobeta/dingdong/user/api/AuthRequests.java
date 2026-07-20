package cn.nobeta.dingdong.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
    private AuthRequests() { }
    /**
     * 用户注册请求 DTO
     * username/phone/email 需全局唯一，password 将在服务端经 BCrypt 加密后存储
     * 各字段的约束校验由 Spring Bean Validation 在控制器层自动触发
     */
    public record RegisterRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_]{4,32}$", message = "用户名应为 4-32 位字母、数字或下划线") String username,
            @NotBlank @Size(min = 8, max = 72, message = "密码长度应为 8-72 位") String password,
            @NotBlank @Size(max = 32) String nickname,
            @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确") String phone,
            @jakarta.validation.constraints.Email(message = "邮箱格式不正确") String email) { }
    /**
     * 用户登录请求 DTO
     * 前端提交登录表单时发送，仅需 username 和 password 两个必填字段
     * 由 Spring Bean Validation 在控制器层自动校验非空
     */
    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    /**
     * 用户资料修改请求 DTO —— 用户资料修改链路的入参
     * nickname 为必填，phone/email/avatarUrl 为可选字段
     * phone 和 email 需全局唯一（仅当值发生变化时由 UserService 校验），空白字符串将被转为 null
     * 各字段的约束校验由 Spring Bean Validation 在控制器层自动触发
     */
    public record ProfileRequest(@NotBlank @Size(max = 32) String nickname,
                                 @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确") String phone,
                                 @jakarta.validation.constraints.Email(message = "邮箱格式不正确") String email,
                                 @Size(max = 512) String avatarUrl) { }
}
