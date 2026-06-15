package com.inventory.adminservice.repository;

import com.inventory.adminservice.entity.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBackupRecordRepository extends JpaRepository<BackupRecord, Long> {

    Optional<BackupRecord> findByBackupName(String backupName);

    List<BackupRecord> findByBackupType(String backupType);

    List<BackupRecord> findByStatus(String status);

    List<BackupRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
