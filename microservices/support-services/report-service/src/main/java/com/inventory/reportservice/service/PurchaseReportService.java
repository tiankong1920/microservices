package com.inventory.reportservice.service;

import com.inventory.reportservice.dto.PurchaseReportDTO;
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
public class PurchaseReportService {
    private final IReportRepository reportRepository;
    private final ModelMapper modelMapper;
    private final IReportService reportService;

    public PurchaseReportDTO generatePurchaseReport(PurchaseReportDTO purchaseReportDTO) {
        // 1. 生成基础报表
        ReportRequestDTO baseReportDTO = ReportRequestDTO.builder()
                .reportName(purchaseReportDTO.getReportName())
                .reportType("PURCHASE_REPORT")
                .startDate(purchaseReportDTO.getStartDate())
                .endDate(purchaseReportDTO.getEndDate())
                .build();

        ReportResponseDTO generatedReport = reportService.generateReport(baseReportDTO);

        // 2. 模拟采购报表数据生成
        PurchaseReportDTO result = modelMapper.map(generatedReport, PurchaseReportDTO.class);
        result.setTotalOrders(120L);

        // 模拟按产品采购数据
        List<PurchaseReportDTO.PurchaseByProductDTO> purchaseByProduct = new ArrayList<>();
        purchaseByProduct.add(PurchaseReportDTO.PurchaseByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .purchaseAmount(new java.math.BigDecimal(12000))
                .purchaseQuantity(240L)
                .orderCount(40)
                .build());
        purchaseByProduct.add(PurchaseReportDTO.PurchaseByProductDTO.builder()
                .productId(2L)
                .productName("产品B")
                .productCode("PROD002")
                .purchaseAmount(new java.math.BigDecimal(20000))
                .purchaseQuantity(400L)
                .orderCount(60)
                .build());
        purchaseByProduct.add(PurchaseReportDTO.PurchaseByProductDTO.builder()
                .productId(3L)
                .productName("产品C")
                .productCode("PROD003")
                .purchaseAmount(new java.math.BigDecimal(8000))
                .purchaseQuantity(160L)
                .orderCount(20)
                .build());
        result.setPurchaseByProduct(purchaseByProduct);

        // 模拟按日期采购数据
        List<PurchaseReportDTO.PurchaseByDateDTO> purchaseByDate = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            purchaseByDate.add(PurchaseReportDTO.PurchaseByDateDTO.builder()
                    .date(date)
                    .purchaseAmount(new java.math.BigDecimal(5600 + i * 800))
                    .purchaseQuantity(112L + i * 16)
                    .orderCount(16L + i * 2)
                    .build());
        }
        result.setPurchaseByDate(purchaseByDate);

        // 模拟按供应商采购数据
        List<PurchaseReportDTO.PurchaseBySupplierDTO> purchaseBySupplier = new ArrayList<>();
        purchaseBySupplier.add(PurchaseReportDTO.PurchaseBySupplierDTO.builder()
                .supplierId(1L)
                .supplierName("供应商A")
                .purchaseAmount(new java.math.BigDecimal(18000))
                .purchaseQuantity(360L)
                .orderCount(50L)
                .build());
        purchaseBySupplier.add(PurchaseReportDTO.PurchaseBySupplierDTO.builder()
                .supplierId(2L)
                .supplierName("供应商B")
                .purchaseAmount(new java.math.BigDecimal(12000))
                .purchaseQuantity(240L)
                .orderCount(35L)
                .build());
        purchaseBySupplier.add(PurchaseReportDTO.PurchaseBySupplierDTO.builder()
                .supplierId(3L)
                .supplierName("供应商C")
                .purchaseAmount(new java.math.BigDecimal(10000))
                .purchaseQuantity(200L)
                .orderCount(35L)
                .build());
        result.setPurchaseBySupplier(purchaseBySupplier);

        // 3. 计算汇总数据
        result.setTotalPurchaseAmount(purchaseByProduct.stream()
                .map(PurchaseReportDTO.PurchaseByProductDTO::getPurchaseAmount)
                .reduce(new java.math.BigDecimal(0), java.math.BigDecimal::add));
        result.setTotalPurchaseQuantity(purchaseByProduct.stream()
                .map(PurchaseReportDTO.PurchaseByProductDTO::getPurchaseQuantity)
                .reduce(0L, Long::sum));
        result.setTotalOrders(purchaseByProduct.stream()
                .map(PurchaseReportDTO.PurchaseByProductDTO::getOrderCount)
                .map(Long::valueOf)
                .reduce(0L, Long::sum));

        return result;
    }

    public PurchaseReportDTO getPurchaseReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase report not found with id: " + id));

        // 模拟采购报表数据
        PurchaseReportDTO result = modelMapper.map(report, PurchaseReportDTO.class);
        result.setTotalOrders(120L);

        // 填充模拟数据
        List<PurchaseReportDTO.PurchaseByProductDTO> purchaseByProduct = new ArrayList<>();
        purchaseByProduct.add(PurchaseReportDTO.PurchaseByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .purchaseAmount(new java.math.BigDecimal(12000))
                .purchaseQuantity(240L)
                .orderCount(40)
                .build());
        result.setPurchaseByProduct(purchaseByProduct);

        result.setPurchaseByDate(new ArrayList<>());
        result.setPurchaseBySupplier(new ArrayList<>());

        return result;
    }

    public List<PurchaseReportDTO> getPurchaseReportsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Report> reports = reportRepository.findByReportTypeAndReportDateBetween(
                ReportType.PURCHASE_REPORT, startDate, endDate);
        return reports.stream()
                .map(report -> {
                    PurchaseReportDTO dto = modelMapper.map(report, PurchaseReportDTO.class);
                    dto.setTotalOrders(120L);
                    dto.setPurchaseByProduct(new ArrayList<>());
                    dto.setPurchaseByDate(new ArrayList<>());
                    dto.setPurchaseBySupplier(new ArrayList<>());
                    return dto;
                })
                .toList();
    }
}
