package com.inventory.reportservice.service;

import com.inventory.reportservice.dto.SalesReportDTO;
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
public class SalesReportService {
    private final IReportRepository reportRepository;
    private final ModelMapper modelMapper;
    private final IReportService reportService;

    public SalesReportDTO generateSalesReport(SalesReportDTO salesReportDTO) {
        // 1. 生成基础报表
        ReportRequestDTO baseReportDTO = ReportRequestDTO.builder()
                .reportName(salesReportDTO.getReportName())
                .reportType("SALES_REPORT")
                .startDate(salesReportDTO.getStartDate())
                .endDate(salesReportDTO.getEndDate())
                .build();

        ReportResponseDTO generatedReport = reportService.generateReport(baseReportDTO);

        // 2. 模拟销售报表数据生成
        SalesReportDTO result = modelMapper.map(generatedReport, SalesReportDTO.class);
        result.setTotalOrders(150L);

        // 模拟按产品销售数据
        List<SalesReportDTO.SalesByProductDTO> salesByProduct = new ArrayList<>();
        salesByProduct.add(SalesReportDTO.SalesByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .salesAmount(new java.math.BigDecimal(15000))
                .salesQuantity(300L)
                .orderCount(50)
                .build());
        salesByProduct.add(SalesReportDTO.SalesByProductDTO.builder()
                .productId(2L)
                .productName("产品B")
                .productCode("PROD002")
                .salesAmount(new java.math.BigDecimal(25000))
                .salesQuantity(500L)
                .orderCount(80)
                .build());
        salesByProduct.add(SalesReportDTO.SalesByProductDTO.builder()
                .productId(3L)
                .productName("产品C")
                .productCode("PROD003")
                .salesAmount(new java.math.BigDecimal(10000))
                .salesQuantity(200L)
                .orderCount(20)
                .build());
        result.setSalesByProduct(salesByProduct);

        // 模拟按日期销售数据
        List<SalesReportDTO.SalesByDateDTO> salesByDate = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            salesByDate.add(SalesReportDTO.SalesByDateDTO.builder()
                    .date(date)
                    .salesAmount(new java.math.BigDecimal(7000 + i * 1000))
                    .salesQuantity(140L + i * 20)
                    .orderCount(20L + i * 3)
                    .build());
        }
        result.setSalesByDate(salesByDate);

        // 模拟按客户销售数据
        List<SalesReportDTO.SalesByCustomerDTO> salesByCustomer = new ArrayList<>();
        salesByCustomer.add(SalesReportDTO.SalesByCustomerDTO.builder()
                .customerId(1L)
                .customerName("客户A")
                .salesAmount(new java.math.BigDecimal(12000))
                .salesQuantity(240L)
                .orderCount(30L)
                .build());
        salesByCustomer.add(SalesReportDTO.SalesByCustomerDTO.builder()
                .customerId(2L)
                .customerName("客户B")
                .salesAmount(new java.math.BigDecimal(18000))
                .salesQuantity(360L)
                .orderCount(45L)
                .build());
        salesByCustomer.add(SalesReportDTO.SalesByCustomerDTO.builder()
                .customerId(3L)
                .customerName("客户C")
                .salesAmount(new java.math.BigDecimal(10000))
                .salesQuantity(200L)
                .orderCount(25L)
                .build());
        result.setSalesByCustomer(salesByCustomer);

        // 3. 计算汇总数据
        result.setTotalSalesAmount(salesByProduct.stream()
                .map(SalesReportDTO.SalesByProductDTO::getSalesAmount)
                .reduce(new java.math.BigDecimal(0), java.math.BigDecimal::add));
        result.setTotalSalesQuantity(salesByProduct.stream()
                .map(SalesReportDTO.SalesByProductDTO::getSalesQuantity)
                .reduce(0L, Long::sum));
        result.setTotalOrders(salesByProduct.stream()
                .map(SalesReportDTO.SalesByProductDTO::getOrderCount)
                .map(Long::valueOf)
                .reduce(0L, Long::sum));

        return result;
    }

    public SalesReportDTO getSalesReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales report not found with id: " + id));

        // 模拟销售报表数据
        SalesReportDTO result = modelMapper.map(report, SalesReportDTO.class);
        result.setTotalOrders(150L);

        // 填充模拟数据
        List<SalesReportDTO.SalesByProductDTO> salesByProduct = new ArrayList<>();
        salesByProduct.add(SalesReportDTO.SalesByProductDTO.builder()
                .productId(1L)
                .productName("产品A")
                .productCode("PROD001")
                .salesAmount(new java.math.BigDecimal(15000))
                .salesQuantity(300L)
                .orderCount(50)
                .build());
        result.setSalesByProduct(salesByProduct);

        result.setSalesByDate(new ArrayList<>());
        result.setSalesByCustomer(new ArrayList<>());

        return result;
    }

    public List<SalesReportDTO> getSalesReportsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Report> reports = reportRepository.findByReportTypeAndReportDateBetween(
                ReportType.SALES_REPORT, startDate, endDate);
        return reports.stream()
                .map(report -> {
                    SalesReportDTO dto = modelMapper.map(report, SalesReportDTO.class);
                    dto.setTotalOrders(150L);
                    dto.setSalesByProduct(new ArrayList<>());
                    dto.setSalesByDate(new ArrayList<>());
                    dto.setSalesByCustomer(new ArrayList<>());
                    return dto;
                })
                .toList();
    }
}
