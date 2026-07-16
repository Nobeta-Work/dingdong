package cn.nobeta.dingdong.user.api;

import cn.nobeta.dingdong.user.domain.MallUser;
import cn.nobeta.dingdong.user.domain.UserAddress;

public final class UserResponses {
    private UserResponses() { }
    public record UserProfile(Long id, String username, String nickname, String phone, String email, String avatarUrl, String role) {
        public static UserProfile from(MallUser user) {
            return new UserProfile(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getEmail(), user.getAvatarUrl(), user.getRole());
        }
    }
    public record LoginResponse(String token, long expiresIn, UserProfile user) { }
    public record AddressResponse(Long id, String receiverName, String receiverPhone, String province, String city, String district, String detailAddress, boolean defaultAddress) {
        public static AddressResponse from(UserAddress address) {
            return new AddressResponse(address.getId(), address.getReceiverName(), address.getReceiverPhone(), address.getProvince(), address.getCity(), address.getDistrict(), address.getDetailAddress(), Boolean.TRUE.equals(address.getDefaultAddress()));
        }
    }
}
