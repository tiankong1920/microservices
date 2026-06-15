package com.invoice.invoiceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoDTO {

    private Long id;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 200, message = "客户名称长度不能超过200个字符")
    private String customerName;

    @NotBlank(message = "纳税人识别号不能为空")
    @Pattern(regexp = "^[0-9A-Z]{15,20}$", message = "税号格式不正确，应为15-20位数字或大写字母")
    private String taxNumber;

    @Size(max = 500, message = "注册地址长度不能超过500个字符")
    private String registeredAddress;

    @Pattern(regexp = "^(\\d{3,4}-\\d{7,8})?$", message = "固定电话格式不正确，应为区号-号码格式")
    private String contactPhone;

    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确，应为11位数字")
    private String mobilePhone;

    @Size(max = 200, message = "开户银行长度不能超过200个字符")
    private String bankName;

    @Pattern(regexp = "^[0-9A-Za-z]{0,30}$", message = "银行账户格式不正确，应为数字或字母，最多30位")
    private String bankAccount;

    @Pattern(regexp = "^(\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*)?$", message = "邮箱格式不正确")
    private String email;

    @Size(max = 100, message = "联系人长度不能超过100个字符")
    private String contactPerson;

    private String remark;

    private String pinyinInitials;

    private String pinyinFull;

    private String status;

    private Integer usageCount;

    private java.time.LocalDateTime lastUsedAt;

    private String tenantId;

    private String createdBy;

    private String updatedBy;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}
