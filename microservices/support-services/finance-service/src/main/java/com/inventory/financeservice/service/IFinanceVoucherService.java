package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.FinanceVoucherDTO;

import java.util.List;

/**
 * Finance Voucher Service Interface.
 */
public interface IFinanceVoucherService {

    List<FinanceVoucherDTO> getAllFinanceVouchers();
    FinanceVoucherDTO getFinanceVoucherById(Long id);
    FinanceVoucherDTO createFinanceVoucher(FinanceVoucherDTO financeVoucherDTO);
    FinanceVoucherDTO updateFinanceVoucher(Long id, FinanceVoucherDTO financeVoucherDTO);
    void deleteFinanceVoucher(Long id);

}
