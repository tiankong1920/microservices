package com.inventory.templateservice.repository;

import com.inventory.common.template.FieldType;
import com.inventory.templateservice.entity.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICustomFieldRepository extends JpaRepository<CustomField, Long> {

    List<CustomField> findByTemplate_Id(Long templateId);

    List<CustomField> findByTemplate_IdAndActiveTrue(Long templateId);

    Optional<CustomField> findByTemplate_IdAndFieldCode(Long templateId, String fieldCode);

    List<CustomField> findByTemplate_IdAndFieldType(Long templateId, FieldType fieldType);

    @Query("SELECT COUNT(c) FROM CustomField c WHERE c.template.id = :templateId AND c.active = true")
    long countActiveByTemplateId(@Param("templateId") Long templateId);

    boolean existsByTemplate_IdAndFieldCode(Long templateId, String fieldCode);

    @Transactional
    void deleteByTemplate_Id(Long templateId);
}
