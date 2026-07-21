package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.user.api.AddressRequest;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 收货地址服务 —— 收货地址增删改查的业务逻辑核心
 * 负责地址列表查询、新增、修改、逻辑删除，所有操作均校验地址归属防止跨用户访问
 */
@Service
public class AddressService {
    private final AddressMapper addressMapper;
    public AddressService(AddressMapper addressMapper) { this.addressMapper = addressMapper; }

    /**
     * 查询收货地址列表 —— 返回当前用户所有未删除的地址
     * 结果按默认地址优先、ID 倒序排列
     * @param userId 当前用户 ID
     * @return 用户的所有有效收货地址列表
     */
    public List<UserAddress> list(Long userId) { return addressMapper.findByUserId(userId); }

    /**
     * 新增收货地址 —— 为指定用户创建一条新的收货地址记录
     * ① 将请求 DTO 转换为领域实体 UserAddress
     * ② 若新地址设为默认地址，先调用 clearDefault() 清除该用户已有的默认标记
     * ③ 执行 INSERT 落库
     * ④ 回查数据库返回包含自增 ID 的完整记录
     *
     * @param userId  当前用户 ID
     * @param request 地址请求（收件人、手机号、省市区、详细地址、是否默认）
     * @return 新创建的完整地址实体
     */
    @Transactional
    public UserAddress create(Long userId, AddressRequest request) {
        // ① 将请求 DTO 转为领域实体（id 为 null，由数据库自增）
        UserAddress address = from(userId, null, request);
        // ② 若新地址设为默认，则先清除该用户已有默认地址的标记
        if (request.defaultAddress()) addressMapper.clearDefault(userId);
        // ③ 执行 INSERT，MyBatis 自动回填自增主键
        addressMapper.insert(address);
        // ④ 回查数据库获取完整记录（含自增 ID、created_at 等）
        return addressMapper.findOwned(address.getId(), userId);
    }

    /**
     * 修改收货地址 —— 更新指定 ID 的地址信息
     * ① 校验地址归属：通过 id + userId 查询确认地址属于当前用户
     * ② 若设为默认地址，先清除旧默认标记
     * ③ 执行 UPDATE 落库
     * ④ 回查返回更新后的完整记录
     *
     * @param userId  当前用户 ID
     * @param id      要修改的地址主键
     * @param request 新的地址信息
     * @return 更新后的完整地址实体
     * @throws BusinessException 地址不存在或不属于当前用户
     */
    @Transactional
    public UserAddress update(Long userId, Long id, AddressRequest request) {
        // ① 校验地址归属：通过 id + userId 联合查询，防止跨用户越权操作
        if (addressMapper.findOwned(id, userId) == null) throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
        // ② 若新设为默认地址，则先清除该用户已有默认标记
        if (request.defaultAddress()) addressMapper.clearDefault(userId);
        // ③ 执行 UPDATE 落库
        addressMapper.update(from(userId, id, request));
        // ④ 回查获取更新后的完整记录
        return addressMapper.findOwned(id, userId);
    }

    /**
     * 删除收货地址 —— 逻辑删除指定 ID 的地址（设置 deleted=1）
     * 通过 id + userId 联合查询确保只能删除自己的地址
     *
     * @param userId 当前用户 ID
     * @param id     要删除的地址主键
     * @throws BusinessException 地址不存在或不属于当前用户
     */
    @Transactional
    public void delete(Long userId, Long id) {
        // 执行逻辑删除（deleted=1），同时清除默认标记；影响行数为 0 表示地址不存在或不属于当前用户
        if (addressMapper.deleteOwned(id, userId) == 0) throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
    }

    /**
     * 将 AddressRequest 请求 DTO 转换为 UserAddress 领域实体
     * @param userId  用户 ID（创建时填充，更新时保持原值）
     * @param id      地址主键（创建时为 null，更新时指定）
     * @param request 请求 DTO
     * @return 填充好字段的 UserAddress 实体
     */
    private UserAddress from(Long userId, Long id, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setId(id); address.setUserId(userId); address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone()); address.setProvince(request.province()); address.setCity(request.city());
        address.setDistrict(request.district()); address.setDetailAddress(request.detailAddress()); address.setDefaultAddress(request.defaultAddress());
        return address;
    }
}
