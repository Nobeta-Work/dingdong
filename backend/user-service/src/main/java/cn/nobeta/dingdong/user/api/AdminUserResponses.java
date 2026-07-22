package cn.nobeta.dingdong.user.api;

import cn.nobeta.dingdong.user.domain.MallUser;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminUserResponses {
    private AdminUserResponses() { }

    public record AdminUser(Long id, String username, String nickname, String phone, String email,
                            String avatarUrl, String role, Integer status,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static AdminUser from(MallUser user) {
            return new AdminUser(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(),
                    user.getEmail(), user.getAvatarUrl(), user.getRole(), user.getStatus(),
                    user.getCreatedAt(), user.getUpdatedAt());
        }
    }

    public record Page<T>(List<T> items, long total, int page, int size) { }
}
