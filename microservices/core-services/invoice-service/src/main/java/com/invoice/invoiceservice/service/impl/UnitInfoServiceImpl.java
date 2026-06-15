package com.invoice.invoiceservice.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.UnitInfoDTO;
import com.invoice.invoiceservice.entity.UnitInfo;
import com.invoice.invoiceservice.repository.IUnitInfoRepository;
import com.invoice.invoiceservice.service.UnitInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnitInfoServiceImpl implements UnitInfoService {

    private final IUnitInfoRepository unitInfoRepository;

    @Override
    public UnitInfoDTO createUnit(UnitInfoDTO dto) {
        unitInfoRepository.findByUnitNameAndDeletedFalse(dto.getUnitName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("单位已存在: " + dto.getUnitName());
                });

        UnitInfo entity = mapToEntity(dto);
        UnitInfo saved = unitInfoRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    @CachePut(value = "unitInfo", key = "#id")
    public UnitInfoDTO updateUnit(Long id, UnitInfoDTO dto) {
        UnitInfo entity = unitInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("单位未找到，ID: " + id));

        if (dto.getUnitName() != null) entity.setUnitName(dto.getUnitName());
        if (dto.getAliases() != null) entity.setAliases(dto.getAliases());

        UnitInfo updated = unitInfoRepository.save(entity);
        return mapToDTO(updated);
    }

    @Override
    @Cacheable(value = "unitInfo", key = "#id")
    @Transactional(readOnly = true)
    public UnitInfoDTO getUnitById(Long id) {
        return mapToDTO(unitInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("单位未找到，ID: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitInfoDTO> getAllUnits() {
        return unitInfoRepository.findByDeletedFalse().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @CacheEvict(value = "unitInfo", key = "#id")
    public void deleteUnit(Long id) {
        UnitInfo entity = unitInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("单位未找到，ID: " + id));
        entity.setDeleted(true);
        unitInfoRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitInfoDTO> searchUnits(String keyword) {
        return unitInfoRepository.searchByKeyword(keyword).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void incrementUsageCount(Long id) {
        UnitInfo entity = unitInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("单位未找到，ID: " + id));
        entity.setUsageCount(entity.getUsageCount() + 1);
        unitInfoRepository.save(entity);
    }

    @Override
    @Transactional
    public UnitInfoDTO findOrCreateUnit(String unitName) {
        return unitInfoRepository.findByUnitNameAndDeletedFalse(unitName)
                .map(existing -> {
                    existing.setUsageCount(existing.getUsageCount() + 1);
                    return mapToDTO(unitInfoRepository.save(existing));
                })
                .orElseGet(() -> {
                    UnitInfo newUnit = new UnitInfo();
                    newUnit.setUnitName(unitName);
                    newUnit.setUsageCount(1);
                    return mapToDTO(unitInfoRepository.save(newUnit));
                });
    }

    private UnitInfo mapToEntity(UnitInfoDTO dto) {
        UnitInfo entity = new UnitInfo();
        entity.setUnitName(dto.getUnitName());
        entity.setAliases(dto.getAliases());
        entity.setTenantId(dto.getTenantId());
        return entity;
    }

    private UnitInfoDTO mapToDTO(UnitInfo entity) {
        return UnitInfoDTO.builder()
                .id(entity.getId())
                .unitName(entity.getUnitName())
                .aliases(entity.getAliases())
                .usageCount(entity.getUsageCount())
                .tenantId(entity.getTenantId())
                .build();
    }
}
