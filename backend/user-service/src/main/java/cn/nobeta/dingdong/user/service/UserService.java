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

import java.util.Random;
import java.util.Objects;
import java.util.UUID;

/**
 * 用户服务 —— 用户注册、登录认证、个人资料查询与修改的业务逻辑核心
 * 负责用户注册、登录认证、个人信息管理。
 * 登录方法 login() 完成：用户名查询 → BCrypt 密码比对 → 账号状态校验 → JWT 签发
 * 资料查询 profile() 完成：按用户 ID 查库 → 账号状态校验
 * 资料修改 updateProfile() 完成：加载当前用户 → 手机号/邮箱唯一性校验 → 字段更新 → 持久化 → 回查
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtTokenService tokenService;
    private final SmsService smsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Random RANDOM = new Random();

    public UserService(UserMapper userMapper, JwtTokenService tokenService, SmsService smsService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
        this.smsService = smsService;
    }

    /**
     * 用户注册
     * 校验用户名唯一性、手机号/邮箱唯一性，使用 BCrypt 加密密码后持久化，
     * 最后回查插入后的完整用户数据（含自增 ID 和默认时间戳）返回
     */
    @Transactional
    public MallUser register(AuthRequests.RegisterRequest request) {
        String code = blankToNull(request.code());
        String phone = blankToNull(request.phone());

        if (code != null) {
            if (phone == null) {
                throw new BusinessException("USER_PHONE_REQUIRED", "验证码注册需要手机号");
            }
            if (!smsService.verifyCode(phone, "register", code)) {
                throw new BusinessException("SMS_CODE_INVALID", "验证码错误或已过期");
            }
        }

        if (code == null) {
            if (blankToNull(request.username()) == null) {
                throw new BusinessException("USER_USERNAME_REQUIRED", "用户名不能为空");
            }
            if (userMapper.findByUsername(request.username()) != null) {
                throw new BusinessException("USER_USERNAME_EXISTS", "用户名已存在");
            }
        }

        MallUser user = new MallUser();
        user.setUsername(code != null && blankToNull(request.username()) == null
                ? "user_" + phone.substring(phone.length() - 4) + "_" + RANDOM.nextInt(1000)
                : request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setPhone(phone);
        user.setEmail(blankToNull(request.email()));
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
        return userMapper.findByUsername(user.getUsername());
    }

    /**
     * 用户登录 —— 登录链路核心服务
     * ① 按用户名查库获取 MallUser
     * ② 使用 BCrypt 比对用户传入的明文密码与库中存储的密文哈希
     * ③ 校验账号状态（status=1 为正常）
     * ④ 调用 JwtTokenService.create() 签发包含用户 ID、用户名、角色的 JWT
     * ⑤ 返回 LoginResult（token + 过期秒数 + 用户实体）
     *
     * @param request 包含 username 和 password 的登录请求
     * @return LoginResult，含 JWT token、有效期秒数、完整用户实体
     * @throws BusinessException 用户名不存在/密码错误 或 账号被禁用
     */
    public LoginResult login(AuthRequests.LoginRequest request) {
        MallUser user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_LOGIN_FAILED", "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("AUTH_USER_DISABLED", "当前用户已被禁用");
        }
        String token = tokenService.create(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginResult(token, tokenService.getExpiresInSeconds(), user);
    }

    public LoginResult smsLogin(String phone, String code) {
        if (!smsService.verifyCode(phone, "login", code)) {
            throw new BusinessException("SMS_CODE_INVALID", "验证码错误或已过期");
        }

        MallUser user = userMapper.findByPhone(phone);
        if (user == null) {
            user = autoCreateByPhone(phone);
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("AUTH_USER_DISABLED", "当前用户已被禁用");
        }

        String token = tokenService.create(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginResult(token, tokenService.getExpiresInSeconds(), user);
    }

    private MallUser autoCreateByPhone(String phone) {
        MallUser user = new MallUser();
        user.setUsername("phone_" + phone);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole("USER");
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            return userMapper.findByPhone(phone);
        }
        return userMapper.findByUsername(user.getUsername());
    }

    /**
     * 用户资料查询 —— 根据用户 ID 查询个人资料
     * ① 通过 userMapper.findById() 按主键查库（查询条件含 deleted=0，逻辑删除用户不可查）
     * ② 校验账号状态：status=1（正常）方可返回，否则抛出 AUTH_UNAUTHORIZED 异常
     *
     * @param userId 当前登录用户 ID（由 Controller 层从 CurrentUserContext 获取传入）
     * @return 完整的 MallUser 领域实体（由 Controller 层通过 UserProfile.from() 脱敏后返回前端）
     * @throws BusinessException 用户不存在或已被禁用
     */
    public MallUser profile(Long userId) {
        MallUser user = userMapper.findById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("AUTH_UNAUTHORIZED", "登录状态无效");
        }
        return user;
    }

    /**
     * 用户资料修改 —— 更新当前用户的昵称、手机号、邮箱、头像 URL
     * ① 先调用 profile() 加载当前用户的完整信息（同时校验用户存在且状态正常）
     * ② 校验手机号/邮箱唯一性：仅在值发生变化时才查库，若已被其他用户占用则抛出对应业务异常
     * ③ 将请求中的新值设置到用户实体上（空白字符串统一转为 null）
     * ④ 调用 userMapper.updateProfile() 持久化到数据库
     * ⑤ 回查数据库返回更新后的完整用户信息（保证返回的是最新落库数据）
     *
     * @param userId  当前登录用户 ID
     * @param request 用户资料修改请求（昵称必填，手机号/邮箱/头像URL可选）
     * @return 更新后的完整 MallUser 实体
     * @throws BusinessException 用户不存在/被禁用/手机号已存在/邮箱已存在
     */
    @Transactional
    public MallUser updateProfile(Long userId, AuthRequests.ProfileRequest request) {
        MallUser current = profile(userId);
        String phone = blankToNull(request.phone());
        String email = blankToNull(request.email());
        if (!Objects.equals(phone, current.getPhone())) {
            if (phone == null) throw new BusinessException("USER_PHONE_REQUIRED", "手机号不能为空");
            if (!smsService.verifyCode(phone, "change-phone", blankToNull(request.smsCode()))) {
                throw new BusinessException("SMS_CODE_INVALID", "验证码错误或已过期");
            }
        }
        // ③ 手机号唯一性校验：仅当手机号有值且与当前值不同时才查库
        if (phone != null && !phone.equals(current.getPhone()) && userMapper.countByPhone(phone) > 0) throw new BusinessException("USER_PHONE_EXISTS", "手机号已被使用");
        // ④ 邮箱唯一性校验：仅当邮箱有值且与当前值不同时才查库
        if (email != null && !email.equals(current.getEmail()) && userMapper.countByEmail(email) > 0) throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已被使用");
        // ⑤ 将请求中的新值设置到当前用户实体
        current.setNickname(request.nickname());
        current.setPhone(phone);
        current.setEmail(email);
        current.setAvatarUrl(blankToNull(request.avatarUrl()));
        userMapper.updateProfile(current);
        return profile(userId);
    }

    @Transactional
    public void changePassword(Long userId, AuthRequests.ChangePasswordRequest request) {
        MallUser current = profile(userId);
        if (!passwordEncoder.matches(request.currentPassword(), current.getPasswordHash())) {
            throw new BusinessException("USER_PASSWORD_INCORRECT", "当前密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), current.getPasswordHash())) {
            throw new BusinessException("USER_PASSWORD_UNCHANGED", "新密码不能与当前密码相同");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 校验手机号和邮箱的唯一性
     * 仅当字段非空时才查询数据库，若已有未删除用户占用则抛出业务异常
     */
    private void assertUniqueContact(String phone, String email) {
        if (blankToNull(phone) != null && userMapper.countByPhone(phone) > 0) {
            throw new BusinessException("USER_PHONE_EXISTS", "手机号已被使用");
        }
        if (blankToNull(email) != null && userMapper.countByEmail(email) > 0) {
            throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已被使用");
        }
    }

    /**
     * 将空白字符串转为 null
     * 用于可选字段，避免空字符串入库导致唯一约束误判
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record LoginResult(String token, long expiresIn, MallUser user) { }

}
