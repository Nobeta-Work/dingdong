package cn.nobeta.dingdong.user.domain;

/**
 * 用户收货地址领域实体，映射 user_address 表 —— 收货地址增删改查链路的数据载体
 * 新增时 id 由数据库自增生成；userId 标识地址归属，所有操作均通过 id+userId 联合校验防越权
 */
public class UserAddress {
    /** 地址主键，数据库自增 */
    private Long id;
    /** 用户 ID，标识地址归属 */
    private Long userId;
    /** 收件人姓名 */
    private String receiverName;
    /** 收件人手机号 */
    private String receiverPhone;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区/县 */
    private String district;
    /** 详细地址（街道/门牌号） */
    private String detailAddress;
    /** 是否为默认地址：true-默认 false-非默认，新增/修改时由 Service 层维护互斥逻辑 */
    private Boolean defaultAddress;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public Boolean getDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(Boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
