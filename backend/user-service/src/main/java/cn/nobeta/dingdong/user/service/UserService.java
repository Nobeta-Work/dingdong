package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.user.api.AuthRequests;
import cn.nobeta.dingdong.user.domain.MallUser;
import cn.nobeta.dingdong.user.mapper.UserMapper;
import cn.nobeta.dingdong.user.security.CurrentUser;
import cn.nobeta.dingdong.user.security.JwtTokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final JwtTokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, JwtTokenService tokenService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
    }

    @Transactional
    public MallUser register(AuthRequests.RegisterRequest request) {
        if (userMapper.findByUsername(request.username()) != null) throw new BusinessException("USER_USERNAME_EXISTS", "用户名已存在");
        assertUniqueContact(request.phone(), request.email());
        MallUser user = new MallUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setPhone(blankToNull(request.phone()));
        user.setEmail(blankToNull(request.email()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return userMapper.findByUsername(user.getUsername());
    }

    public LoginResult login(AuthRequests.LoginRequest request) {
        MallUser user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_LOGIN_FAILED", "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException("AUTH_USER_DISABLED", "当前用户已被禁用");
        String token = tokenService.create(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginResult(token, tokenService.getExpiresInSeconds(), user);
    }

    public MallUser profile(Long userId) {
        MallUser user = userMapper.findById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException("AUTH_UNAUTHORIZED", "登录状态无效");
        return user;
    }

    @Transactional
    public MallUser updateProfile(Long userId, AuthRequests.ProfileRequest request) {
        MallUser current = profile(userId);
        String phone = blankToNull(request.phone());
        String email = blankToNull(request.email());
        if (phone != null && !phone.equals(current.getPhone()) && userMapper.countByPhone(phone) > 0) throw new BusinessException("USER_PHONE_EXISTS", "手机号已被使用");
        if (email != null && !email.equals(current.getEmail()) && userMapper.countByEmail(email) > 0) throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已被使用");
        current.setNickname(request.nickname());
        current.setPhone(phone);
        current.setEmail(email);
        current.setAvatarUrl(blankToNull(request.avatarUrl()));
        userMapper.updateProfile(current);
        return profile(userId);
    }

    private void assertUniqueContact(String phone, String email) {
        if (blankToNull(phone) != null && userMapper.countByPhone(phone) > 0) throw new BusinessException("USER_PHONE_EXISTS", "手机号已被使用");
        if (blankToNull(email) != null && userMapper.countByEmail(email) > 0) throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已被使用");
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    public record LoginResult(String token, long expiresIn, MallUser user) { }
}
