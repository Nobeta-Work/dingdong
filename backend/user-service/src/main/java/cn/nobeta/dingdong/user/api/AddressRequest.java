package cn.nobeta.dingdong.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 收货地址请求 DTO —— 收货地址增删改链路的入参
 * 新增和修改地址时共用此 DTO，所有字符串字段均有长度和格式约束
 * defaultAddress 为 true 时，Service 层会自动清除该用户已有的默认标记
 */
public record AddressRequest(
        @NotBlank @Size(max = 32) String receiverName,
        @NotBlank @Pattern(regexp = "^1\\d{10}$", message = "收件人手机号格式不正确") String receiverPhone,
        @NotBlank @Size(max = 32) String province,
        @NotBlank @Size(max = 32) String city,
        @NotBlank @Size(max = 32) String district,
        @NotBlank @Size(max = 128) String detailAddress,
        boolean defaultAddress) { }
