package com.inventory.templateservice.repository;

import com.inventory.templateservice.entity.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITemplateFieldRepository extends JpaRepository<TemplateField, Long> {

    List<TemplateField> findByTemplate_Id(Long templateId);

    List<TemplateField> findByTemplate_IdOrderByDisplayOrderAsc(Long templateId);

    Optional<TemplateField> findByTemplate_IdAndFieldCode(Long templateId, String fieldCode);

    List<TemplateField> findByTemplate_IdAndRequiredTrue(Long templateId);

    List<TemplateField> findByTemplate_IdAndSearchableTrue(Long templateId);

    @Query("SELECT MAX(f.displayOrder) FROM TemplateField f WHERE f.template.id = :templateId")
    Integer findMaxDisplayOrder(@Param("templateId") Long templateId);

    boolean existsByTemplate_IdAndFieldCode(Long templateId, String fieldCode);

    @Transactional
    void deleteByTemplate_Id(Long templateId);

    long countByTemplate_Id(Long templateId);
}
