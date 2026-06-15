package com.inventory.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    @NotBlank(message = "订单编号不能为空")
    @Size(max = 50, message = "订单编号长度不能超过50个字符")
    private String orderNumber;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 200, message = "客户名称长度不能超过200个字符")
    private String customerName;

    private String status;

    private List<OrderItemDTO> items;

    private Double totalAmount;
}
