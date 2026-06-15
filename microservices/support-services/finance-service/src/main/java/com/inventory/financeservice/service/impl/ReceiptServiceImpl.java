package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.ReceiptDTO;
import com.inventory.financeservice.entity.Receipt;
import com.inventory.financeservice.exception.ReceiptNotFoundException;
import com.inventory.financeservice.repository.IReceiptRepository;
import com.inventory.financeservice.service.IReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReceiptServiceImpl implements IReceiptService {

    private static final String LOG_RECEIPT_NOT_FOUND = "Receipt not found with id: {}";

    private final IReceiptRepository receiptRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ReceiptDTO> getAllReceipts() {
        log.info("Getting all receipts");
        final List<Receipt> receipts = receiptRepository.findAll();
        log.info("Found {} receipts", receipts.size());
        return receipts.stream()
                .map(receipt -> modelMapper.map(receipt, ReceiptDTO.class))
                .toList();
    }

    @Override
    public ReceiptDTO getReceiptById(Long id) {
        log.info("Getting receipt by id: {}", id);
        final Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RECEIPT_NOT_FOUND, id);
                    return new ReceiptNotFoundException(id);
                });
        return modelMapper.map(receipt, ReceiptDTO.class);
    }

    @Override
    public ReceiptDTO getReceiptByReceiptNumber(String receiptNumber) {
        log.info("Getting receipt by receipt number: {}", receiptNumber);
        final Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> {
                    log.error("Receipt not found with receipt number: {}", receiptNumber);
                    return new ReceiptNotFoundException("receiptNumber", receiptNumber);
                });
        return modelMapper.map(receipt, ReceiptDTO.class);
    }

    @Override
    public List<ReceiptDTO> getReceiptsByCustomerId(Long customerId) {
        log.info("Getting receipts by customer id: {}", customerId);
        final List<Receipt> receipts = receiptRepository.findByCustomerId(customerId);
        log.info("Found {} receipts for customer id: {}", receipts.size(), customerId);
        return receipts.stream()
                .map(receipt -> modelMapper.map(receipt, ReceiptDTO.class))
                .toList();
    }

    @Override
    public List<ReceiptDTO> getReceiptsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting receipts by date range: {} to {}", startDate, endDate);
        final List<Receipt> receipts = receiptRepository.findByReceiptDateBetween(startDate, endDate);
        log.info("Found {} receipts in date range", receipts.size());
        return receipts.stream()
                .map(receipt -> modelMapper.map(receipt, ReceiptDTO.class))
                .toList();
    }

    @Override
    public List<ReceiptDTO> getReceiptsByPaymentMethod(String paymentMethod) {
        log.info("Getting receipts by payment method: {}", paymentMethod);
        final List<Receipt> receipts = receiptRepository.findByPaymentMethod(paymentMethod);
        log.info("Found {} receipts with payment method: {}", receipts.size(), paymentMethod);
        return receipts.stream()
                .map(receipt -> modelMapper.map(receipt, ReceiptDTO.class))
                .toList();
    }

    @Override
    public List<ReceiptDTO> getReceiptsByReceiptStatus(String receiptStatus) {
        log.info("Getting receipts by receipt status: {}", receiptStatus);
        final List<Receipt> receipts = receiptRepository.findByReceiptStatus(receiptStatus);
        log.info("Found {} receipts with receipt status: {}", receipts.size(), receiptStatus);
        return receipts.stream()
                .map(receipt -> modelMapper.map(receipt, ReceiptDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public ReceiptDTO createReceipt(ReceiptDTO receiptDTO) {
        log.info("Creating receipt: {}", receiptDTO.getReceiptNumber());

        final Receipt receipt = modelMapper.map(receiptDTO, Receipt.class);
        receipt.setReceiptDate(LocalDateTime.now());
        if (receipt.getReceiptStatus() == null) {
            receipt.setReceiptStatus("RECEIVED");
        }

        final Receipt savedReceipt = receiptRepository.save(receipt);
        log.info("Receipt created successfully with id: {}", savedReceipt.getId());
        return modelMapper.map(savedReceipt, ReceiptDTO.class);
    }

    @Override
    @Transactional
    public ReceiptDTO updateReceipt(Long id, ReceiptDTO receiptDTO) {
        log.info("Updating receipt with id: {}", id);

        final Receipt existingReceipt = receiptRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RECEIPT_NOT_FOUND, id);
                    return new ReceiptNotFoundException(id);
                });

        modelMapper.map(receiptDTO, existingReceipt);
        final Receipt updatedReceipt = receiptRepository.save(existingReceipt);
        log.info("Receipt updated successfully with id: {}", updatedReceipt.getId());
        return modelMapper.map(updatedReceipt, ReceiptDTO.class);
    }

    @Override
    @Transactional
    public void deleteReceipt(Long id) {
        log.info("Deleting receipt with id: {}", id);

        if (!receiptRepository.existsById(id)) {
            log.error(LOG_RECEIPT_NOT_FOUND, id);
            throw new ReceiptNotFoundException(id);
        }

        receiptRepository.deleteById(id);
        log.info("Receipt deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public ReceiptDTO updateReceiptStatus(Long id, String receiptStatus) {
        log.info("Updating receipt status to: {} for id: {}", receiptStatus, id);

        final Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RECEIPT_NOT_FOUND, id);
                    return new ReceiptNotFoundException(id);
                });

        receipt.setReceiptStatus(receiptStatus);
        final Receipt updatedReceipt = receiptRepository.save(receipt);
        log.info("Receipt status updated successfully with id: {}", updatedReceipt.getId());
        return modelMapper.map(updatedReceipt, ReceiptDTO.class);
    }

    @Override
    public BigDecimal getTotalReceiptByCustomerId(Long customerId) {
        log.info("Getting total receipt by customer id: {}", customerId);
        final List<Receipt> receipts = receiptRepository.findByCustomerId(customerId);
        final BigDecimal total = receipts.stream()
                .map(receipt -> receipt.getReceiptAmount() != null ? receipt.getReceiptAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total receipt for customer id {} is: {}", customerId, total);
        return total;
    }
}
