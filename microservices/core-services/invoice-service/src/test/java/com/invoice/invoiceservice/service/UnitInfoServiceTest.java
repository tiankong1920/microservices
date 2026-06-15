package com.invoice.invoiceservice.service;

import com.invoice.invoiceservice.dto.UnitInfoDTO;
import com.invoice.invoiceservice.entity.UnitInfo;
import com.invoice.invoiceservice.repository.IUnitInfoRepository;
import com.invoice.invoiceservice.service.impl.UnitInfoServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitInfoServiceTest {

    @Mock
    private IUnitInfoRepository unitInfoRepository;

    private UnitInfoService unitInfoService;

    @BeforeEach
    void setUp() {
        unitInfoService = new UnitInfoServiceImpl(unitInfoRepository);
    }

    @Test
    void testCreateUnit() {
        UnitInfoDTO dto = UnitInfoDTO.builder()
                .unitName("个")
                .aliases("只,件")
                .build();

        when(unitInfoRepository.findByUnitNameAndDeletedFalse("个")).thenReturn(Optional.empty());
        when(unitInfoRepository.save(any(UnitInfo.class))).thenAnswer(invocation -> {
            UnitInfo entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        UnitInfoDTO result = unitInfoService.createUnit(dto);

        assertNotNull(result);
        assertEquals("个", result.getUnitName());
    }

    @Test
    void testCreateUnitDuplicate() {
        UnitInfoDTO dto = UnitInfoDTO.builder()
                .unitName("个")
                .aliases("只,件")
                .build();

        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");

        when(unitInfoRepository.findByUnitNameAndDeletedFalse("个")).thenReturn(Optional.of(existing));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            unitInfoService.createUnit(dto);
        });
    }

    @Test
    void testFindOrCreateUnitExisting() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setUsageCount(5);

        when(unitInfoRepository.findByUnitNameAndDeletedFalse("个")).thenReturn(Optional.of(existing));
        when(unitInfoRepository.save(any(UnitInfo.class))).thenReturn(existing);

        UnitInfoDTO result = unitInfoService.findOrCreateUnit("个");

        assertNotNull(result);
        assertEquals("个", result.getUnitName());
        verify(unitInfoRepository).save(any(UnitInfo.class));
    }

    @Test
    void testFindOrCreateUnitNew() {
        when(unitInfoRepository.findByUnitNameAndDeletedFalse("箱")).thenReturn(Optional.empty());
        when(unitInfoRepository.save(any(UnitInfo.class))).thenAnswer(invocation -> {
            UnitInfo entity = invocation.getArgument(0);
            entity.setId(2L);
            return entity;
        });

        UnitInfoDTO result = unitInfoService.findOrCreateUnit("箱");

        assertNotNull(result);
        assertEquals("箱", result.getUnitName());
        assertEquals(1, result.getUsageCount());
    }

    @Test
    void testGetAllUnits() {
        UnitInfo unit1 = new UnitInfo();
        unit1.setId(1L);
        unit1.setUnitName("个");
        unit1.setUsageCount(10);

        UnitInfo unit2 = new UnitInfo();
        unit2.setId(2L);
        unit2.setUnitName("箱");
        unit2.setUsageCount(5);

        when(unitInfoRepository.findByDeletedFalse()).thenReturn(List.of(unit1, unit2));

        List<UnitInfoDTO> results = unitInfoService.getAllUnits();

        assertEquals(2, results.size());
    }

    @Test
    void testUpdateUnit() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setUsageCount(5);

        when(unitInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(unitInfoRepository.save(any(UnitInfo.class))).thenReturn(existing);

        UnitInfoDTO updateDto = UnitInfoDTO.builder()
                .unitName("箱子")
                .aliases("箱,盒")
                .build();

        UnitInfoDTO result = unitInfoService.updateUnit(1L, updateDto);

        assertNotNull(result);
        assertEquals("箱子", result.getUnitName());
        verify(unitInfoRepository).save(any(UnitInfo.class));
    }

    @Test
    void testUpdateUnitNotFound() {
        when(unitInfoRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        UnitInfoDTO updateDto = UnitInfoDTO.builder()
                .unitName("新名称")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            unitInfoService.updateUnit(999L, updateDto);
        });
    }

    @Test
    void testUpdateUnitPartial() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setAliases("原别名");
        existing.setUsageCount(5);

        when(unitInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(unitInfoRepository.save(any(UnitInfo.class))).thenReturn(existing);

        UnitInfoDTO updateDto = UnitInfoDTO.builder()
                .aliases("新别名")
                .build();

        UnitInfoDTO result = unitInfoService.updateUnit(1L, updateDto);

        assertNotNull(result);
        assertEquals("个", result.getUnitName());
        assertEquals("新别名", result.getAliases());
        verify(unitInfoRepository).save(any(UnitInfo.class));
    }

    @Test
    void testGetUnitById() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setUsageCount(5);

        when(unitInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));

        UnitInfoDTO result = unitInfoService.getUnitById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("个", result.getUnitName());
    }

    @Test
    void testGetUnitByIdNotFound() {
        when(unitInfoRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            unitInfoService.getUnitById(999L);
        });
    }

    @Test
    void testDeleteUnit() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setDeleted(false);

        when(unitInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(unitInfoRepository.save(any(UnitInfo.class))).thenReturn(existing);

        unitInfoService.deleteUnit(1L);

        verify(unitInfoRepository).save(any(UnitInfo.class));
    }

    @Test
    void testDeleteUnitNotFound() {
        when(unitInfoRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            unitInfoService.deleteUnit(999L);
        });
    }

    @Test
    void testSearchUnits() {
        UnitInfo unit1 = new UnitInfo();
        unit1.setId(1L);
        unit1.setUnitName("个");

        when(unitInfoRepository.searchByKeyword("个")).thenReturn(List.of(unit1));

        List<UnitInfoDTO> results = unitInfoService.searchUnits("个");

        assertEquals(1, results.size());
        assertEquals("个", results.get(0).getUnitName());
    }

    @Test
    void testIncrementUsageCount() {
        UnitInfo existing = new UnitInfo();
        existing.setId(1L);
        existing.setUnitName("个");
        existing.setUsageCount(5);

        when(unitInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(unitInfoRepository.save(any(UnitInfo.class))).thenReturn(existing);

        unitInfoService.incrementUsageCount(1L);

        verify(unitInfoRepository).save(any(UnitInfo.class));
    }
}
