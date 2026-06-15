package com.inventory.businesspartnerservice.service.impl;

import com.inventory.businesspartnerservice.dto.BusinessPartnerDTO;
import com.inventory.businesspartnerservice.dto.BusinessPartnerStatisticsDTO;
import com.inventory.businesspartnerservice.entity.BusinessPartner;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import com.inventory.businesspartnerservice.exception.BusinessPartnerException;
import com.inventory.businesspartnerservice.repository.IBusinessPartnerRepository;
import com.inventory.businesspartnerservice.service.IBusinessPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BusinessPartnerServiceImpl implements IBusinessPartnerService {

    private final IBusinessPartnerRepository businessPartnerRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_NOT_FOUND_ID = "Business partner not found with id: ";
    private static final String ERROR_NOT_FOUND_CODE = "Business partner not found with code: ";
    private static final String ERROR_DUPLICATE_CODE = "Business partner with code ";
    private static final String ERROR_DUPLICATE_EMAIL = "Business partner with email ";

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public BusinessPartnerDTO createBusinessPartner(BusinessPartnerDTO dto) {
        log.info("Creating business partner: {}", dto.getPartnerCode());

        if (businessPartnerRepository.existsByPartnerCode(dto.getPartnerCode())) {
            throw new BusinessPartnerException(ERROR_DUPLICATE_CODE + dto.getPartnerCode() + " already exists");
        }

        BusinessPartner partner = modelMapper.map(dto, BusinessPartner.class);
        partner.setCreatedAt(LocalDateTime.now());
        partner.setUpdatedAt(LocalDateTime.now());

        BusinessPartner saved = businessPartnerRepository.save(partner);
        log.info("Business partner created: {}", saved.getId());
        return modelMapper.map(saved, BusinessPartnerDTO.class);
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public BusinessPartnerDTO getBusinessPartnerById(Long id) {
        return businessPartnerRepository.findById(id)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class))
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_ID + id));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public BusinessPartnerDTO getBusinessPartnerByCode(String partnerCode) {
        return businessPartnerRepository.findByPartnerCode(partnerCode)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class))
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_CODE + partnerCode));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public List<BusinessPartnerDTO> getAllBusinessPartners() {
        return businessPartnerRepository.findAll().stream()
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> getAllBusinessPartnersPaged(Pageable pageable) {
        return businessPartnerRepository.findAll(pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public BusinessPartnerDTO updateBusinessPartner(Long id, BusinessPartnerDTO dto) {
        log.info("Updating business partner: {}", id);

        BusinessPartner existing = businessPartnerRepository.findById(id)
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_ID + id));

        if (!existing.getPartnerCode().equals(dto.getPartnerCode())
                && businessPartnerRepository.existsByPartnerCode(dto.getPartnerCode())) {
            throw new BusinessPartnerException(ERROR_DUPLICATE_CODE + dto.getPartnerCode() + " already exists");
        }

        modelMapper.map(dto, existing);
        existing.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(businessPartnerRepository.save(existing), BusinessPartnerDTO.class);
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public void deleteBusinessPartner(Long id) {
        if (!businessPartnerRepository.existsById(id)) {
            throw new BusinessPartnerException(ERROR_NOT_FOUND_ID + id);
        }
        businessPartnerRepository.deleteById(id);
        log.info("Business partner deleted: {}", id);
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public List<BusinessPartnerDTO> getBusinessPartnersByType(PartnerType partnerType) {
        return businessPartnerRepository.findByPartnerType(partnerType).stream()
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> getBusinessPartnersByTypePaged(PartnerType partnerType, Pageable pageable) {
        return businessPartnerRepository.findByPartnerType(partnerType, pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public List<BusinessPartnerDTO> getActiveBusinessPartners() {
        return businessPartnerRepository.findByActive(true).stream()
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> getActiveBusinessPartnersPaged(Pageable pageable) {
        return businessPartnerRepository.findByActive(true, pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> searchBusinessPartners(String keyword, Pageable pageable) {
        return businessPartnerRepository.searchByKeyword(keyword, pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> getBusinessPartnersByCity(String city, Pageable pageable) {
        return businessPartnerRepository.findByCityIgnoreCase(city, pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @Cacheable(value = "businessPartners", keyGenerator = "customCacheKeyGenerator")
    public Page<BusinessPartnerDTO> getBusinessPartnersByCountry(String country, Pageable pageable) {
        return businessPartnerRepository.findByCountryIgnoreCase(country, pageable)
                .map(p -> modelMapper.map(p, BusinessPartnerDTO.class));
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public BusinessPartnerDTO toggleBusinessPartnerStatus(Long id) {
        BusinessPartner partner = businessPartnerRepository.findById(id)
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_ID + id));

        partner.setActive(!partner.isActive());
        partner.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(businessPartnerRepository.save(partner), BusinessPartnerDTO.class);
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public BusinessPartnerDTO activateBusinessPartner(Long id) {
        BusinessPartner partner = businessPartnerRepository.findById(id)
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_ID + id));

        partner.setActive(true);
        partner.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(businessPartnerRepository.save(partner), BusinessPartnerDTO.class);
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public BusinessPartnerDTO deactivateBusinessPartner(Long id) {
        BusinessPartner partner = businessPartnerRepository.findById(id)
                .orElseThrow(() -> new BusinessPartnerException(ERROR_NOT_FOUND_ID + id));

        partner.setActive(false);
        partner.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(businessPartnerRepository.save(partner), BusinessPartnerDTO.class);
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public int batchActivateBusinessPartners(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (businessPartnerRepository.existsById(id)) {
                BusinessPartner partner = businessPartnerRepository.findById(id).get();
                partner.setActive(true);
                partner.setUpdatedAt(LocalDateTime.now());
                businessPartnerRepository.save(partner);
                count++;
            }
        }
        log.info("Batch activated {} business partners", count);
        return count;
    }

    @Override
    @CacheEvict(value = "businessPartners", allEntries = true)
    @Transactional
    public int batchDeactivateBusinessPartners(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (businessPartnerRepository.existsById(id)) {
                BusinessPartner partner = businessPartnerRepository.findById(id).get();
                partner.setActive(false);
                partner.setUpdatedAt(LocalDateTime.now());
                businessPartnerRepository.save(partner);
                count++;
            }
        }
        log.info("Batch deactivated {} business partners", count);
        return count;
    }

    @Override
    public BusinessPartnerStatisticsDTO getStatistics() {
        long total = businessPartnerRepository.count();
        long active = businessPartnerRepository.countByActive(true);

        Map<String, Long> byType = new HashMap<>();
        for (PartnerType type : PartnerType.values()) {
            byType.put(type.name(), businessPartnerRepository.countByPartnerType(type));
        }

        return BusinessPartnerStatisticsDTO.builder()
                .totalPartners(total)
                .activePartners(active)
                .inactivePartners(total - active)
                .partnersByType(byType)
                .build();
    }

    @Override
    public List<String> getAllCities() {
        return businessPartnerRepository.findAllCities();
    }

    @Override
    public List<String> getAllCountries() {
        return businessPartnerRepository.findAllCountries();
    }

    @Override
    public boolean existsByPartnerCode(String partnerCode) {
        return businessPartnerRepository.existsByPartnerCode(partnerCode);
    }

    @Override
    public boolean existsByEmail(String email) {
        return businessPartnerRepository.existsByEmail(email);
    }

    @Override
    public void validatePartnerCodeUniqueness(String partnerCode) {
        if (businessPartnerRepository.existsByPartnerCode(partnerCode)) {
            throw new BusinessPartnerException(ERROR_DUPLICATE_CODE + partnerCode + " already exists");
        }
    }

    @Override
    public void validateEmailUniqueness(String email) {
        if (businessPartnerRepository.existsByEmail(email)) {
            throw new BusinessPartnerException(ERROR_DUPLICATE_EMAIL + email + " already exists");
        }
    }
}
