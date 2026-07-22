package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.user.api.AdminUserResponses;
import cn.nobeta.dingdong.user.domain.MallUser;
import cn.nobeta.dingdong.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AdminUserService {
    private final UserMapper mapper;
    public AdminUserService(UserMapper mapper) { this.mapper = mapper; }

    public AdminUserResponses.Page<AdminUserResponses.AdminUser> page(String keyword, String role, Integer status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<AdminUserResponses.AdminUser> items = mapper.findAdminPage(keyword, role, status,
                (safePage - 1) * safeSize, safeSize).stream().map(AdminUserResponses.AdminUser::from).toList();
        return new AdminUserResponses.Page<>(items, mapper.countAdminPage(keyword, role, status), safePage, safeSize);
    }

    public MallUser detail(Long id) {
        MallUser user = mapper.findById(id);
        if (user == null) throw new BusinessException("ADMIN_USER_NOT_FOUND", "用户不存在");
        return user;
    }

    @Transactional
    public MallUser changeStatus(Long operatorId, Long id, Integer status) {
        MallUser target = detail(id);
        if (operatorId.equals(id)) throw new BusinessException("ADMIN_SELF_DISABLE_FORBIDDEN", "不能修改当前管理员自己的状态");
        if ("ADMIN".equals(target.getRole())) throw new BusinessException("ADMIN_USER_PROTECTED", "管理员账号状态不可在此接口修改");
        if (mapper.updateStatus(id, status) == 0) throw new BusinessException("ADMIN_USER_NOT_FOUND", "用户不存在");
        return detail(id);
    }
}
