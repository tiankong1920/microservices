package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.UnitInfoDTO;

public interface UnitInfoService {

    UnitInfoDTO createUnit(UnitInfoDTO dto);

    UnitInfoDTO updateUnit(Long id, UnitInfoDTO dto);

    UnitInfoDTO getUnitById(Long id);

    List<UnitInfoDTO> getAllUnits();

    void deleteUnit(Long id);

    List<UnitInfoDTO> searchUnits(String keyword);

    void incrementUsageCount(Long id);

    UnitInfoDTO findOrCreateUnit(String unitName);
}
