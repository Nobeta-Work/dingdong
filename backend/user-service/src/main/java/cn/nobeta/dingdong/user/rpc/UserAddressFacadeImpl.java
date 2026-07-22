package cn.nobeta.dingdong.user.rpc;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.UserAddressFacade;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.mapper.AddressMapper;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserAddressFacadeImpl implements UserAddressFacade {

    private final AddressMapper addressMapper;

    public UserAddressFacadeImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public AddressSnapshot getAddressSnapshot(Long userId, Long addressId) {
        UserAddress address = addressMapper.findOwned(addressId, userId);
        if (address == null) {
            throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
        }
        return new AddressSnapshot(
                address.getId(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetailAddress()
        );
    }

}
