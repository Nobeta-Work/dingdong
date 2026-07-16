package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;

/**
 * 用户地址 Dubbo 契约接口（用户服务提供，订单服务消费）
 * <p>用于跨服务获取用户地址快照，确保下单时地址信息固定，不受后续修改影响。</p>
 */
public interface UserAddressFacade {

    /**
     * 获取地址快照
     * <p>返回下单时刻的地址详细信息，包含省市区和详细地址。
     * 快照设计避免用户后续修改地址影响历史订单展示。</p>
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @return 地址快照信息
     */
    AddressSnapshot getAddressSnapshot(Long userId, Long addressId);

    /** 地址快照记录 */
    record AddressSnapshot(Long addressId, String receiverName, String receiverPhone,
                           String province, String city, String district, String detailAddress) implements Serializable { }
}
