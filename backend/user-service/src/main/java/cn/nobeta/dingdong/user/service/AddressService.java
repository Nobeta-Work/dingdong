package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.user.api.AddressRequest;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressService {
    private final AddressMapper addressMapper;
    public AddressService(AddressMapper addressMapper) { this.addressMapper = addressMapper; }
    public List<UserAddress> list(Long userId) { return addressMapper.findByUserId(userId); }

    @Transactional
    public UserAddress create(Long userId, AddressRequest request) {
        UserAddress address = from(userId, null, request);
        if (request.defaultAddress()) addressMapper.clearDefault(userId);
        addressMapper.insert(address);
        return addressMapper.findOwned(address.getId(), userId);
    }

    @Transactional
    public UserAddress update(Long userId, Long id, AddressRequest request) {
        if (addressMapper.findOwned(id, userId) == null) throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
        if (request.defaultAddress()) addressMapper.clearDefault(userId);
        addressMapper.update(from(userId, id, request));
        return addressMapper.findOwned(id, userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (addressMapper.deleteOwned(id, userId) == 0) throw new BusinessException("USER_ADDRESS_NOT_FOUND", "收货地址不存在");
    }

    private UserAddress from(Long userId, Long id, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setId(id); address.setUserId(userId); address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone()); address.setProvince(request.province()); address.setCity(request.city());
        address.setDistrict(request.district()); address.setDetailAddress(request.detailAddress()); address.setDefaultAddress(request.defaultAddress());
        return address;
    }
}
