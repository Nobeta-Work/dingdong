package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;

/** Stable user-address snapshot contract consumed by order-service. */
public interface UserAddressFacade {
    AddressSnapshot getAddressSnapshot(Long userId, Long addressId);
    record AddressSnapshot(Long addressId, String receiverName, String receiverPhone,
                           String province, String city, String district, String detailAddress) implements Serializable { }
}
