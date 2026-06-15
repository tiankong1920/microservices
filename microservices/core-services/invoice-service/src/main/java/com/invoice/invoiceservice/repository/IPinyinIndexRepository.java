package com.invoice.invoiceservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.entity.PinyinIndex;

@Repository
public interface IPinyinIndexRepository extends JpaRepository<PinyinIndex, Long> {

    List<PinyinIndex> findByPinyinInitialsStartingWith(String initials);

    List<PinyinIndex> findByEntityTypeAndPinyinInitialsStartingWith(String entityType, String initials);

    @Query("SELECT p FROM PinyinIndex p WHERE p.pinyinInitials LIKE CONCAT(:initials, '%') " +
           "OR p.pinyinFull LIKE LOWER(CONCAT('%', :pinyin, '%'))")
    List<PinyinIndex> searchByPinyin(@Param("initials") String initials, @Param("pinyin") String pinyin);

    @Query("SELECT p FROM PinyinIndex p WHERE p.entityType = :entityType AND " +
           "(p.pinyinInitials LIKE CONCAT(:initials, '%') " +
           "OR p.pinyinFull LIKE LOWER(CONCAT('%', :pinyin, '%')))")
    List<PinyinIndex> searchByPinyinAndType(
            @Param("entityType") String entityType,
            @Param("initials") String initials,
            @Param("pinyin") String pinyin);

    @Modifying
    @Transactional
    @Query("DELETE FROM PinyinIndex p WHERE p.entityType = :entityType AND p.entityId = :entityId")
    void deleteByEntityTypeAndEntityId(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    List<PinyinIndex> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
