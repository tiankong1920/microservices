package com.invoice.invoiceservice.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class InvoiceCalculationRequest {

    @NotEmpty(message = "发票商品条目不能为空")
    @Valid
    private List<InvoiceItemDTO> items;
}
