package com.inventory.adminservice.repository;

import com.inventory.adminservice.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PasswordHistory> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Transactional
    void deleteByUserId(Long userId);
}
