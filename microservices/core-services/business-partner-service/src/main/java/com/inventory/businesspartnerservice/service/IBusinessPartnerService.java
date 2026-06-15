package com.inventory.businesspartnerservice.service;

import com.inventory.businesspartnerservice.dto.BusinessPartnerDTO;
import com.inventory.businesspartnerservice.dto.BusinessPartnerStatisticsDTO;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBusinessPartnerService {

    BusinessPartnerDTO createBusinessPartner(BusinessPartnerDTO businessPartnerDTO);

    BusinessPartnerDTO getBusinessPartnerById(Long id);

    BusinessPartnerDTO getBusinessPartnerByCode(String partnerCode);

    List<BusinessPartnerDTO> getAllBusinessPartners();

    Page<BusinessPartnerDTO> getAllBusinessPartnersPaged(Pageable pageable);

    BusinessPartnerDTO updateBusinessPartner(Long id, BusinessPartnerDTO businessPartnerDTO);

    void deleteBusinessPartner(Long id);

    List<BusinessPartnerDTO> getBusinessPartnersByType(PartnerType partnerType);

    Page<BusinessPartnerDTO> getBusinessPartnersByTypePaged(PartnerType partnerType, Pageable pageable);

    List<BusinessPartnerDTO> getActiveBusinessPartners();

    Page<BusinessPartnerDTO> getActiveBusinessPartnersPaged(Pageable pageable);

    Page<BusinessPartnerDTO> searchBusinessPartners(String keyword, Pageable pageable);

    Page<BusinessPartnerDTO> getBusinessPartnersByCity(String city, Pageable pageable);

    Page<BusinessPartnerDTO> getBusinessPartnersByCountry(String country, Pageable pageable);

    BusinessPartnerDTO toggleBusinessPartnerStatus(Long id);

    BusinessPartnerDTO activateBusinessPartner(Long id);

    BusinessPartnerDTO deactivateBusinessPartner(Long id);

    int batchActivateBusinessPartners(List<Long> ids);

    int batchDeactivateBusinessPartners(List<Long> ids);

    BusinessPartnerStatisticsDTO getStatistics();

    List<String> getAllCities();

    List<String> getAllCountries();

    boolean existsByPartnerCode(String partnerCode);

    boolean existsByEmail(String email);

    void validatePartnerCodeUniqueness(String partnerCode);

    void validateEmailUniqueness(String email);
}
