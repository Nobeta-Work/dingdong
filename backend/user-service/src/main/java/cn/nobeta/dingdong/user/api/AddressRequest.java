package cn.nobeta.dingdong.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 32) String receiverName,
        @NotBlank @Pattern(regexp = "^1\\d{10}$", message = "收件人手机号格式不正确") String receiverPhone,
        @NotBlank @Size(max = 32) String province,
        @NotBlank @Size(max = 32) String city,
        @NotBlank @Size(max = 32) String district,
        @NotBlank @Size(max = 128) String detailAddress,
        boolean defaultAddress) { }
