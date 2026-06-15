package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findBySupplierId(Long supplierId);

    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findByPaymentMethod(String paymentMethod);

    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findByRelatedDocumentId(Long relatedDocumentId);
}
