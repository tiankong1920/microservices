package com.inventory.reportservice.service;

import com.inventory.reportservice.dto.InventoryReportDTO;
import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.entity.Report;
import com.inventory.reportservice.entity.ReportType;
import com.inventory.reportservice.repository.IReportRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class InventoryReportService {
    private final IReportRepository reportRepository;
    private final ModelMapper modelMapper;
    private final IReportService reportService;

    public InventoryReportDTO generateInventoryReport(InventoryReportDTO inventoryReportDTO) {
        // 1. 生成基础报表
        ReportRequestDTO baseReportDTO = ReportRequestDTO.builder()
                .reportName(inventoryReportDTO.getReportName())
                .reportType("INVENTORY_REPORT")
                .startDate(inventoryReportDTO.getStartDate())
                .endDate(inventoryReportDTO.getEndDate())
                .build();

        ReportResponseDTO generatedReport = reportService.generateReport(baseReportDTO);

        // 2. 模拟库存报表数据生成
        InventoryReportDTO result = modelMapper.map(generatedReport, InventoryReportDTO.class);
        result.setTotalProducts(250L);

        // 模拟按产品库存数据
        List<InventoryReportDTO.InventoryByProductDTO> inventoryByProduct = new ArrayList<>();
        inventoryByProduct.add(InventoryReportDTO.InventoryByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .currentQuantity(500L)
                .unitPrice(new java.math.BigDecimal(50))
                .totalValue(new java.math.BigDecimal(25000))
                .category("电子产品")
                .status("正常")
                .build());
        inventoryByProduct.add(InventoryReportDTO.InventoryByProductDTO.builder()
                .productId(2L)
                .productName("产品B")
                .productCode("PROD002")
                .currentQuantity(300L)
                .unitPrice(new java.math.BigDecimal(80))
                .totalValue(new java.math.BigDecimal(24000))
                .category("家居用品")
                .status("正常")
                .build());
        inventoryByProduct.add(InventoryReportDTO.InventoryByProductDTO.builder()
                .productId(3L)
                .productName("产品C")
                .productCode("PROD003")
                .currentQuantity(100L)
                .unitPrice(new java.math.BigDecimal(120))
                .totalValue(new java.math.BigDecimal(12000))
                .category("办公用品")
                .status("低库存")
                .build());
        result.setInventoryByProduct(inventoryByProduct);

        // 模拟按仓库库存数据
        List<InventoryReportDTO.InventoryByWarehouseDTO> inventoryByWarehouse = new ArrayList<>();
        inventoryByWarehouse.add(InventoryReportDTO.InventoryByWarehouseDTO.builder()
                .warehouseId(1L)
                .warehouseName("主仓库")
                .totalProducts(150L)
                .totalQuantity(12000L)
                .totalValue(new java.math.BigDecimal(1200000))
                .build());
        inventoryByWarehouse.add(InventoryReportDTO.InventoryByWarehouseDTO.builder()
                .warehouseId(2L)
                .warehouseName("分仓库")
                .totalProducts(100L)
                .totalQuantity(8000L)
                .totalValue(new java.math.BigDecimal(800000))
                .build());
        result.setInventoryByWarehouse(inventoryByWarehouse);

        // 模拟库存变动数据
        List<InventoryReportDTO.InventoryMovementDTO> inventoryMovements = new ArrayList<>();
        inventoryMovements.add(InventoryReportDTO.InventoryMovementDTO.builder()
                .movementId(1L)
                .movementDate(LocalDateTime.now().minusDays(1))
                .movementType("入库")
                .productId(1L)
                .productName("产品A")
                .quantity(100L)
                .unitPrice(new java.math.BigDecimal(50))
                .totalAmount(new java.math.BigDecimal(5000))
                .sourceWarehouse("供应商")
                .targetWarehouse("主仓库")
                .reason("采购入库")
                .build());
        inventoryMovements.add(InventoryReportDTO.InventoryMovementDTO.builder()
                .movementId(2L)
                .movementDate(LocalDateTime.now().minusDays(2))
                .movementType("出库")
                .productId(2L)
                .productName("产品B")
                .quantity(50L)
                .unitPrice(new java.math.BigDecimal(80))
                .totalAmount(new java.math.BigDecimal(4000))
                .sourceWarehouse("主仓库")
                .targetWarehouse("客户")
                .reason("销售出库")
                .build());
        result.setInventoryMovements(inventoryMovements);

        // 3. 计算汇总数据
        result.setTotalStockQuantity(inventoryByProduct.stream()
                .map(InventoryReportDTO.InventoryByProductDTO::getCurrentQuantity)
                .reduce(0L, Long::sum));
        result.setTotalStockValue(inventoryByProduct.stream()
                .map(InventoryReportDTO.InventoryByProductDTO::getTotalValue)
                .reduce(new java.math.BigDecimal(0), java.math.BigDecimal::add));

        return result;
    }

    public InventoryReportDTO getInventoryReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory report not found with id: " + id));

        // 模拟库存报表数据
        InventoryReportDTO result = modelMapper.map(report, InventoryReportDTO.class);
        result.setTotalProducts(250L);

        // 填充模拟数据
        List<InventoryReportDTO.InventoryByProductDTO> inventoryByProduct = new ArrayList<>();
        inventoryByProduct.add(InventoryReportDTO.InventoryByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .currentQuantity(500L)
                .unitPrice(new java.math.BigDecimal(50))
                .totalValue(new java.math.BigDecimal(25000))
                .category("电子产品")
                .status("正常")
                .build());
        result.setInventoryByProduct(inventoryByProduct);

        result.setInventoryByWarehouse(new ArrayList<>());
        result.setInventoryMovements(new ArrayList<>());

        return result;
    }

    public List<InventoryReportDTO> getInventoryReportsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Report> reports = reportRepository.findByReportTypeAndReportDateBetween(
                ReportType.INVENTORY_REPORT, startDate, endDate);
        return reports.stream()
                .map(report -> {
                    InventoryReportDTO dto = modelMapper.map(report, InventoryReportDTO.class);
                    dto.setTotalProducts(250L);
                    dto.setInventoryByProduct(new ArrayList<>());
                    dto.setInventoryByWarehouse(new ArrayList<>());
                    dto.setInventoryMovements(new ArrayList<>());
                    return dto;
                })
                .toList();
    }
}
