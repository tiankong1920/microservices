package com.inventory.businesspartnerservice.repository;

import com.inventory.businesspartnerservice.entity.BusinessPartner;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {

    Optional<BusinessPartner> findByPartnerCode(String partnerCode);

    List<BusinessPartner> findByPartnerType(PartnerType partnerType);

    Page<BusinessPartner> findByPartnerType(PartnerType partnerType, Pageable pageable);

    List<BusinessPartner> findByActive(boolean active);

    Page<BusinessPartner> findByActive(boolean active, Pageable pageable);

    Page<BusinessPartner> findByPartnerTypeAndActive(PartnerType partnerType, boolean active, Pageable pageable);

    @Query("SELECT bp FROM BusinessPartner bp WHERE " +
           "LOWER(bp.partnerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(bp.partnerCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(bp.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BusinessPartner> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<BusinessPartner> findByCityIgnoreCase(String city, Pageable pageable);

    Page<BusinessPartner> findByCountryIgnoreCase(String country, Pageable pageable);

    @Query("SELECT DISTINCT bp.city FROM BusinessPartner bp WHERE bp.city IS NOT NULL ORDER BY bp.city")
    java.util.List<String> findAllCities();

    @Query("SELECT DISTINCT bp.country FROM BusinessPartner bp WHERE bp.country IS NOT NULL ORDER BY bp.country")
    java.util.List<String> findAllCountries();

    long countByPartnerType(PartnerType partnerType);

    long countByActive(boolean active);

    boolean existsByPartnerCode(String partnerCode);

    boolean existsByEmail(String email);
}
