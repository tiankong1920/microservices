package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    List<Receipt> findByCustomerId(Long customerId);

    List<Receipt> findByReceiptDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Receipt> findByPaymentMethod(String paymentMethod);

    List<Receipt> findByReceiptStatus(String receiptStatus);

    List<Receipt> findByRelatedDocumentId(Long relatedDocumentId);
}
