package com.inventory.templateservice.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.templateservice.TestCacheConfig;
import com.inventory.templateservice.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestCacheConfig.class})
@Transactional
@Slf4j
class TemplatePerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private List<TemplateDTO> testTemplates;

    @BeforeEach
    void setUp() {
        testTemplates = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testTemplates.add(TemplateDTO.builder()
                    .templateCode("TPL_PERF_" + String.format("%03d", i))
                    .templateName("Performance Test Template " + i)
                    .description("Performance test template description " + i)
                    .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                    .status(TemplateStatus.DRAFT)
                    .tenantId("default")
                    .build());
        }
    }

    private Long extractIdFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("批量创建模板性能测试")
    void batchCreateTemplatesPerformanceTest() throws Exception {
        long startTime = System.nanoTime();
        int successCount = 0;

        for (TemplateDTO template : testTemplates.subList(0, 50)) {
            try {
                mockMvc.perform(post("/api/templates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(template)))
                        .andExpect(status().isCreated());
                successCount++;
            } catch (Exception e) {
                // Continue with next template
            }
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double avgTimeMs = durationMs / 50.0;

        log.info("批量创建50个模板总耗时: {}ms, 平均每个模板创建耗时: {}ms, 成功创建: {}/50",
                durationMs, String.format("%.2f", avgTimeMs), successCount);

        assertThat(successCount).isGreaterThan(0);
        assertThat(avgTimeMs).isLessThan(1000.0);
    }

    @Test
    @DisplayName("模板查询性能测试")
    void queryTemplatesPerformanceTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            TemplateDTO template = TemplateDTO.builder()
                    .templateCode("TPL_QUERY_" + String.format("%03d", i))
                    .templateName("Query Test Template " + i)
                    .businessDomain(BusinessDomain.INVENTORY_MANAGEMENT)
                    .status(TemplateStatus.PUBLISHED)
                    .tenantId("default")
                    .build();

            mockMvc.perform(post("/api/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(template)))
                    .andExpect(status().isCreated());
        }

        long startTime = System.nanoTime();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            mockMvc.perform(get("/api/templates/domain/{domain}", "INVENTORY_MANAGEMENT"))
                    .andExpect(status().isOk());
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double avgTimeMs = durationMs / (double) iterations;

        log.info("执行{}次查询总耗时: {}ms, 平均每次查询耗时: {}ms",
                iterations, durationMs, String.format("%.2f", avgTimeMs));

        assertThat(avgTimeMs).isLessThan(1000.0);
    }

    @Test
    @DisplayName("模板更新性能测试")
    void updateTemplatePerformanceTest() throws Exception {
        TemplateDTO template = TemplateDTO.builder()
                .templateCode("TPL_UPDATE_PERF")
                .templateName("Update Performance Test")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .tenantId("default")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isCreated())
                .andReturn();

        Long templateId = extractIdFromResponse(createResult);

        long startTime = System.nanoTime();
        int iterations = 50;

        for (int i = 0; i < iterations; i++) {
            TemplateDTO updateDTO = TemplateDTO.builder()
                    .templateCode("TPL_UPDATE_PERF")
                    .templateName("Updated Name " + i)
                    .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                    .status(TemplateStatus.DRAFT)
                    .tenantId("default")
                    .build();

            mockMvc.perform(put("/api/templates/{id}", templateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk());
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double avgTimeMs = durationMs / (double) iterations;

        log.info("执行{}次更新总耗时: {}ms, 平均每次更新耗时: {}ms",
                iterations, durationMs, String.format("%.2f", avgTimeMs));

        assertThat(avgTimeMs).isLessThan(1000.0);
    }

    @Test
    @DisplayName("并发读取性能测试")
    void concurrentReadPerformanceTest() throws Exception {
        for (int i = 0; i < 5; i++) {
            TemplateDTO template = TemplateDTO.builder()
                    .templateCode("TPL_CONCURRENT_" + String.format("%03d", i))
                    .templateName("Concurrent Test Template " + i)
                    .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                    .status(TemplateStatus.PUBLISHED)
                    .tenantId("default")
                    .build();

            mockMvc.perform(post("/api/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(template)))
                    .andExpect(status().isCreated());
        }

        long startTime = System.nanoTime();
        int threadCount = 10;
        List<Thread> threads = new ArrayList<>();
        List<Long> responseTimes = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            Thread thread = new Thread(() -> {
                try {
                    long threadStart = System.nanoTime();
                    mockMvc.perform(get("/api/templates/domain/{domain}", "CUSTOMER_MANAGEMENT"))
                            .andExpect(status().isOk());
                    long threadEnd = System.nanoTime();
                    synchronized (responseTimes) {
                        responseTimes.add(TimeUnit.NANOSECONDS.toMillis(threadEnd - threadStart));
                    }
                } catch (Exception e) {
                    // Handle exception
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join(5000);
        }

        long endTime = System.nanoTime();
        long totalDurationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("并发{}个线程读取总耗时: {}ms, 平均响应时间: {}ms, 成功请求数: {}/{}",
                threadCount, totalDurationMs, String.format("%.2f", avgResponseTime),
                responseTimes.size(), threadCount);

        assertThat(responseTimes.size()).isGreaterThan(threadCount / 2);
    }
}
