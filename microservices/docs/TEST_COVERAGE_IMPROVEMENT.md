# 测试覆盖率提升指南

## 当前基线

| 模块 | 覆盖率 | 目标 |
|------|--------|------|
| common | 4% | 90%+ |
| (其他模块) | 待测 | 90%+ |

## 快速开始

### 运行覆盖率报告
```powershell
cd E:\101\microservices\project-root
.\gradlew jacocoTestReport
```

### 查看报告
打开 `common/build/reports/jacoco/test/html/index.html`

## 提升策略

### 1. 优先覆盖核心业务类

按优先级排序需要覆盖的包：

| 优先级 | 包名 | 说明 |
|--------|------|------|
| P0 | com.inventory.common.core | 核心异常处理、全局异常处理器 |
| P0 | com.inventory.common.util | 工具类，数据校验 |
| P1 | com.inventory.common.saga | Saga编排逻辑 |
| P1 | com.inventory.common.resilience | 限流、熔断、降级 |
| P2 | com.inventory.common.monitoring | 监控指标采集 |
| P2 | com.inventory.common.security | 安全日志 |
| P3 | com.inventory.common.feign | Feign客户端 |

### 2. Service层测试模板

```java
package com.inventory.{service}.service.impl;

import com.inventory.{service}.repository.I{Service}Repository;
import com.inventory.{service}.service.I{Service}Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("{Service} Service Tests")
class {Service}ServiceImplTest {

    @Mock
    private I{Service}Repository repository;

    @InjectMocks
    private {Service}ServiceImpl service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should find entity by ID when exists")
    void testFindById_WhenExists_ReturnsEntity() {
        // Arrange
        Long id = 1L;
        {Entity} expected = {Entity}.builder()
                .id(id)
                .name("Test")
                .build();
        when(repository.findById(id)).thenReturn(java.util.Optional.of(expected));

        // Act
        {Entity} result = service.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test", result.getName());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when entity not found")
    void testFindById_WhenNotExists_ThrowsException() {
        // Arrange
        Long id = 999L;
        when(repository.findById(id)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows({Service}NotFoundException.class, () -> service.findById(id));
    }

    @Test
    @DisplayName("Should save entity successfully")
    void testSave_ValidEntity_ReturnsSavedEntity() {
        // Arrange
        {Entity} entity = {Entity}.builder()
                .name("New Entity")
                .build();
        {Entity} savedEntity = {Entity}.builder()
                .id(1L)
                .name("New Entity")
                .build();
        when(repository.save(any({Entity}.class))).thenReturn(savedEntity);

        // Act
        {Entity} result = service.save(entity);

        // Assert
        assertNotNull(result.getId());
        assertEquals("New Entity", result.getName());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("Should delete entity by ID")
    void testDeleteById_ExistingId_CallsRepository() {
        // Arrange
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        // Act
        service.deleteById(id);

        // Assert
        verify(repository).deleteById(id);
    }
}
```

### 3. Controller层测试模板

```java
package com.inventory.{service}.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.{service}.dto.{Entity}DTO;
import com.inventory.{service}.service.I{Service}Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({Entity}Controller.class)
@DisplayName("{Entity} Controller Tests")
class {Entity}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private I{Service}Service service;

    @Test
    @DisplayName("Should get all entities")
    @WithMockUser(roles = "USER")
    void testGetAll_ReturnsListOfEntities() throws Exception {
        // Arrange
        List<{Entity}DTO> entities = List.of(
            {Entity}DTO.builder().id(1L).name("Entity 1").build(),
            {Entity}DTO.builder().id(2L).name("Entity 2").build()
        );
        when(service.findAll()).thenReturn(entities);

        // Act & Assert
        mockMvc.perform(get("/api/v1/{entities}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Entity 1"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void testGetAll_NotAuthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/{entities}"))
                .andExpect(status().isUnauthorized());
    }
}
```

### 4. 边界条件测试

```java
@Test
@DisplayName("Should handle null input gracefully")
void testMethod_NullInput_ThrowsValidationException() {
    assertThrows(ValidationException.class, () -> service.process(null));
}

@Test
@DisplayName("Should handle empty list")
void testFindAll_EmptyList_ReturnsEmptyList() {
    when(repository.findAll()).thenReturn(List.of());
    List<Entity> result = service.findAll();
    assertTrue(result.isEmpty());
}

@Test
@DisplayName("Should handle concurrent access")
void testUpdate_ConcurrentModification_HandlesGracefully() {
    // 使用 CountDownLatch 模拟并发
}
```

## 覆盖率目标

| 层级 | 当前 | 目标 |
|------|------|------|
| Instructions | ~4% | ≥90% |
| Branches | ~6% | ≥80% |
| Lines | ~5% | ≥90% |
| Methods | ~4% | ≥95% |

## 验证命令

```powershell
# 生成聚合报告
.\gradlew jacocoTestReport

# 查看各模块覆盖率
.\gradlew jacocoRootReport

# 仅测试指定模块
.\gradlew :common:jacocoTestReport
```

## 注意事项

1. **不要为追求覆盖率而写无用测试** - 测试应验证业务逻辑
2. **优先覆盖关键路径** - 异常处理、边界条件、核心业务逻辑
3. **保持测试快速** - 单元测试应毫秒级完成
4. **测试应该是独立的** - 不依赖执行顺序
