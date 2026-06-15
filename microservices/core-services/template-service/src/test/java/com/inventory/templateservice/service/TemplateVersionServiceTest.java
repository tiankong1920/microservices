package com.inventory.templateservice.service;

import com.inventory.common.template.constant.TemplateConstants;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateVersionDTO;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.entity.TemplateVersion;
import com.inventory.templateservice.repository.ITemplateRepository;
import com.inventory.templateservice.repository.ITemplateVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateVersionServiceTest {

    @Mock
    private ITemplateVersionRepository versionRepository;

    @Mock
    private ITemplateRepository templateRepository;

    @Mock
    private TemplateService templateService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    private TemplateVersionService versionService;

    private Template testTemplate;
    private TemplateVersion testVersion;

    @BeforeEach
    void setUp() {
        testTemplate = Template.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .name("Test Template")
                .version("1.0.0")
                .build();

        testVersion = TemplateVersion.builder()
                .id(1L)
                .template(testTemplate)
                .versionNumber("1.0.0")
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .changeType("major")
                .changeDescription("Initial version")
                .snapshot("{\"templateCode\":\"TPL_TEST_001\"}")
                .changedAt(LocalDateTime.now())
                .build();

        versionService = new TemplateVersionService(versionRepository, templateRepository, templateService, modelMapper, objectMapper);
    }

    @Test
    @DisplayName("创建版本 - 首个版本")
    void createVersion_FirstVersion() {
        TemplateDTO templateDTO = TemplateDTO.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .templateName("Test Template")
                .version("1.0.0")
                .build();

        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.of(templateDTO));
        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong())).thenReturn(Optional.empty());
        when(versionRepository.save(any(TemplateVersion.class))).thenReturn(testVersion);
        when(modelMapper.map(any(TemplateVersion.class), eq(TemplateVersionDTO.class)))
                .thenReturn(TemplateVersionDTO.builder().versionNumber("1.0.0").build());
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        TemplateVersionDTO result = versionService.createVersion(1L, "Initial version", "major");

        assertNotNull(result);
        assertEquals("1.0.0", result.getVersionNumber());
    }

    @Test
    @DisplayName("创建版本 - 次版本更新")
    void createVersion_MinorUpdate() {
        TemplateVersion existingVersion = TemplateVersion.builder()
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .build();

        TemplateDTO templateDTO = TemplateDTO.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .templateName("Test Template")
                .version("1.0.0")
                .build();

        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.of(templateDTO));
        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong()))
                .thenReturn(Optional.of(existingVersion));
        when(versionRepository.save(any(TemplateVersion.class))).thenAnswer(invocation -> {
            TemplateVersion v = invocation.getArgument(0);
            v.setId(2L);
            return v;
        });
        when(modelMapper.map(any(TemplateVersion.class), eq(TemplateVersionDTO.class)))
                .thenAnswer(invocation -> {
                    TemplateVersion v = invocation.getArgument(0);
                    return TemplateVersionDTO.builder().versionNumber(v.getVersionNumber()).build();
                });
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        TemplateVersionDTO result = versionService.createVersion(1L, "Minor update", "minor");

        assertNotNull(result);
        assertEquals("1.1.0", result.getVersionNumber());
    }

    @Test
    @DisplayName("创建版本 - 补丁版本更新")
    void createVersion_PatchUpdate() {
        TemplateVersion existingVersion = TemplateVersion.builder()
                .majorVersion(1)
                .minorVersion(1)
                .patchVersion(0)
                .build();

        TemplateDTO templateDTO = TemplateDTO.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .templateName("Test Template")
                .version("1.1.0")
                .build();

        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.of(templateDTO));
        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong()))
                .thenReturn(Optional.of(existingVersion));
        when(versionRepository.save(any(TemplateVersion.class))).thenAnswer(invocation -> {
            TemplateVersion v = invocation.getArgument(0);
            v.setId(2L);
            return v;
        });
        when(modelMapper.map(any(TemplateVersion.class), eq(TemplateVersionDTO.class)))
                .thenAnswer(invocation -> {
                    TemplateVersion v = invocation.getArgument(0);
                    return TemplateVersionDTO.builder().versionNumber(v.getVersionNumber()).build();
                });
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        TemplateVersionDTO result = versionService.createVersion(1L, "Patch update", "patch");

        assertNotNull(result);
        assertEquals("1.1.1", result.getVersionNumber());
    }

    @Test
    @DisplayName("获取版本历史")
    void getVersionHistory() {
        List<TemplateVersion> versions = List.of(testVersion);
        when(versionRepository.findByTemplate_IdOrderByChangedAtDesc(anyLong())).thenReturn(versions);
        when(modelMapper.map(any(TemplateVersion.class), eq(TemplateVersionDTO.class)))
                .thenReturn(TemplateVersionDTO.builder().versionNumber("1.0.0").build());

        List<TemplateVersionDTO> result = versionService.getVersionHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("生成下一个版本号 - 主版本")
    void generateNextVersion_Major() {
        TemplateVersion latestVersion = TemplateVersion.builder()
                .majorVersion(1)
                .minorVersion(2)
                .patchVersion(3)
                .build();

        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong()))
                .thenReturn(Optional.of(latestVersion));

        String result = versionService.generateNextVersion(1L, TemplateConstants.CHANGE_TYPE_MAJOR);

        assertEquals("2.0.0", result);
    }

    @Test
    @DisplayName("生成下一个版本号 - 次版本")
    void generateNextVersion_Minor() {
        TemplateVersion latestVersion = TemplateVersion.builder()
                .majorVersion(1)
                .minorVersion(2)
                .patchVersion(3)
                .build();

        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong()))
                .thenReturn(Optional.of(latestVersion));

        String result = versionService.generateNextVersion(1L, TemplateConstants.CHANGE_TYPE_MINOR);

        assertEquals("1.3.0", result);
    }

    @Test
    @DisplayName("生成下一个版本号 - 补丁版本")
    void generateNextVersion_Patch() {
        TemplateVersion latestVersion = TemplateVersion.builder()
                .majorVersion(1)
                .minorVersion(2)
                .patchVersion(3)
                .build();

        when(versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(anyLong()))
                .thenReturn(Optional.of(latestVersion));

        String result = versionService.generateNextVersion(1L, TemplateConstants.CHANGE_TYPE_PATCH);

        assertEquals("1.2.4", result);
    }

    @Test
    @DisplayName("版本兼容性检查 - 兼容")
    void isCompatible_True() {
        TemplateVersion v1 = TemplateVersion.builder().majorVersion(1).minorVersion(0).patchVersion(0).build();
        TemplateVersion v2 = TemplateVersion.builder().majorVersion(1).minorVersion(1).patchVersion(0).build();

        when(versionRepository.findById(1L)).thenReturn(Optional.of(v1));
        when(versionRepository.findById(2L)).thenReturn(Optional.of(v2));

        boolean result = versionService.isCompatible(1L, 1L, 2L);

        assertTrue(result);
    }

    @Test
    @DisplayName("版本兼容性检查 - 不兼容")
    void isCompatible_False() {
        TemplateVersion v1 = TemplateVersion.builder().majorVersion(1).minorVersion(0).patchVersion(0).build();
        TemplateVersion v2 = TemplateVersion.builder().majorVersion(2).minorVersion(0).patchVersion(0).build();

        when(versionRepository.findById(1L)).thenReturn(Optional.of(v1));
        when(versionRepository.findById(2L)).thenReturn(Optional.of(v2));

        boolean result = versionService.isCompatible(1L, 1L, 2L);

        assertFalse(result);
    }
}
