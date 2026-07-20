package cn.nobeta.dingdong.user.rpc;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.UserAddressFacade;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.mapper.AddressMapper;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserAddressFacadeImpl implements UserAddressFacade {
    private final AddressMapper addressMapper;
    public UserAddressFacadeImpl(AddressMapper addressMapper) { this.addressMapper = addressMapper; }
    @Override
    public AddressSnapshot getAddressSnapshot(Long userId, Long addressId) {
        // 按用户 ID + 地址 ID 做归属校验，避免跨用户读取到他人的收货地址
        UserAddress address = addressMapper.findOwned(addressId, userId);
        // 数据不存在时直接抛业务异常，由订单服务在调用方侧中断下单流程
        if (address == null) throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
        // 将可变的地址实体转换为不可变快照，供订单服务持久化历史收货信息
        return new AddressSnapshot(address.getId(), address.getReceiverName(), address.getReceiverPhone(), address.getProvince(), address.getCity(), address.getDistrict(), address.getDetailAddress());
    }
}
