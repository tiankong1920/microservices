package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.ReceiptDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IReceiptService {

    List<ReceiptDTO> getAllReceipts();

    ReceiptDTO getReceiptById(Long id);

    ReceiptDTO getReceiptByReceiptNumber(String receiptNumber);

    List<ReceiptDTO> getReceiptsByCustomerId(Long customerId);

    List<ReceiptDTO> getReceiptsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<ReceiptDTO> getReceiptsByPaymentMethod(String paymentMethod);

    List<ReceiptDTO> getReceiptsByReceiptStatus(String receiptStatus);

    ReceiptDTO createReceipt(ReceiptDTO receiptDTO);

    ReceiptDTO updateReceipt(Long id, ReceiptDTO receiptDTO);

    void deleteReceipt(Long id);

    ReceiptDTO updateReceiptStatus(Long id, String receiptStatus);

    BigDecimal getTotalReceiptByCustomerId(Long customerId);
}
