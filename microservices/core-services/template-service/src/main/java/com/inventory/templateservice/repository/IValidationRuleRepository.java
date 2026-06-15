package com.inventory.templateservice.repository;

import com.inventory.templateservice.entity.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IValidationRuleRepository extends JpaRepository<ValidationRule, Long> {

    Optional<ValidationRule> findByRuleCode(String ruleCode);

    List<ValidationRule> findByBuiltInTrue();

    List<ValidationRule> findByActiveTrue();

    List<ValidationRule> findByBuiltInTrueAndActiveTrue();

    @Query("SELECT r FROM ValidationRule r WHERE r.applicableFieldTypes LIKE CONCAT('%', :fieldType, '%') AND r.active = true")
    List<ValidationRule> findByApplicableFieldType(@Param("fieldType") String fieldType);

    boolean existsByRuleCode(String ruleCode);
}
