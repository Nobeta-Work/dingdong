package cn.nobeta.dingdong.user.api;

import cn.nobeta.dingdong.user.domain.MallUser;
import cn.nobeta.dingdong.user.domain.UserAddress;

public final class UserResponses {
    private UserResponses() { }
    /**
     * 用户信息响应 DTO —— 用户资料查询与修改链路的返回对象
     * 从 MallUser 领域实体转换而来，已去除 passwordHash 等敏感字段
     * 用于 GET/PUT /api/users/me 接口的响应数据
     */
    public record UserProfile(Long id, String username, String nickname, String phone, String email, String avatarUrl, String role) {
        /**
         * 由 MallUser 领域实体构建脱敏的 UserProfile 响应对象
         * 注册成功后通过此方法将用户数据返回，确保密码哈希不泄露到响应中
         * 用户资料查询和修改完成后也通过此方法对返回数据做脱敏处理
         */
        public static UserProfile from(MallUser user) {
            return new UserProfile(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getEmail(), user.getAvatarUrl(), user.getRole());
        }
    }
    /**
     * 登录响应 DTO
     * 包含 JWT token（前端存入 localStorage）、过期秒数（前端可据此做 token 刷新提示）、
     * 以及脱敏后的用户信息（不含 passwordHash 等敏感字段）
     */
    public record LoginResponse(String token, long expiresIn, UserProfile user) { }
    /**
     * 收货地址响应 DTO —— 收货地址查询链路的返回对象
     * 从 UserAddress 领域实体转换而来，defaultAddress 使用 boolean 类型
     * 用于 GET/POST/PUT /api/addresses 接口的响应数据
     */
    public record AddressResponse(Long id, String receiverName, String receiverPhone, String province, String city, String district, String detailAddress, boolean defaultAddress) {
        /**
         * 由 UserAddress 领域实体构建 AddressResponse 响应对象
         * defaultAddress 字段通过 Boolean.TRUE.equals() 安全解包，避免 NPE
         */
        public static AddressResponse from(UserAddress address) {
            return new AddressResponse(address.getId(), address.getReceiverName(), address.getReceiverPhone(), address.getProvince(), address.getCity(), address.getDistrict(), address.getDetailAddress(), Boolean.TRUE.equals(address.getDefaultAddress()));
        }
    }
}
