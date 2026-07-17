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

/**
 * 用户服务 —— 登录链路的业务逻辑核心
 * 负责用户注册、登录认证、个人信息管理。
 * 登录方法 login() 完成：用户名查询 → BCrypt 密码比对 → 账号状态校验 → JWT 签发
 */
@Service
public class UserService {
    private final UserMapper userMapper;
    private final JwtTokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, JwtTokenService tokenService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
    }

    /**
     * 用户注册
     * 校验用户名唯一性、手机号/邮箱唯一性，使用 BCrypt 加密密码后持久化，
     * 最后回查插入后的完整用户数据（含自增 ID 和默认时间戳）返回
     */
    @Transactional
    public MallUser register(AuthRequests.RegisterRequest request) {
        // 校验用户名唯一性：若已存在同名未删除用户则抛出业务异常
        if (userMapper.findByUsername(request.username()) != null) throw new BusinessException("USER_USERNAME_EXISTS", "用户名已存在");
        // 校验手机号与邮箱唯一性（可选字段，仅非空时校验）
        assertUniqueContact(request.phone(), request.email());
        // 构建用户领域实体
        MallUser user = new MallUser();
        user.setUsername(request.username());
        // 使用 BCrypt 对明文密码进行哈希加密后存储
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        // 将空白字符串转为 null，避免空字符串违反唯一索引约束
        user.setPhone(blankToNull(request.phone()));
        user.setEmail(blankToNull(request.email()));
        // 新注册用户默认角色为普通用户，状态为正常
        user.setRole("USER");
        user.setStatus(1);
        // 执行插入，MyBatis 生成的 SQL 会回填自增主键
        userMapper.insert(user);
        // 回查数据库，获取包含自增 ID、created_at、updated_at 等完整字段的用户对象
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
        // ① 按用户名查询用户（查询条件包含 deleted=0，逻辑删除的用户不可登录）
        MallUser user = userMapper.findByUsername(request.username());
        // ② 用户不存在 或 BCrypt 密码校验不通过 → 统一抛出"用户名或密码错误"（防止用户枚举攻击）
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_LOGIN_FAILED", "用户名或密码错误");
        }
        // ③ 检查账号状态：status=1 为正常，非 1 视为已禁用
        if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException("AUTH_USER_DISABLED", "当前用户已被禁用");
        // ④ 根据用户信息构建 CurrentUser，调用 JwtTokenService 签发 HS256 签名的 JWT
        String token = tokenService.create(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        // ⑤ 封装 LoginResult 返回：token、过期秒数、完整用户实体（由 Controller 层脱敏后返回前端）
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

    /**
     * 校验手机号和邮箱的唯一性
     * 仅当字段非空时才查询数据库，若已有未删除用户占用则抛出业务异常
     */
    private void assertUniqueContact(String phone, String email) {
        if (blankToNull(phone) != null && userMapper.countByPhone(phone) > 0) throw new BusinessException("USER_PHONE_EXISTS", "手机号已被使用");
        if (blankToNull(email) != null && userMapper.countByEmail(email) > 0) throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已被使用");
    }
    /**
     * 将空白字符串转为 null
     * 用于可选字段，避免空字符串入库导致唯一约束误判
     */
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    public record LoginResult(String token, long expiresIn, MallUser user) { }
}
