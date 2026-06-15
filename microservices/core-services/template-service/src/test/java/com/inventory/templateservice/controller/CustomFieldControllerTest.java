package com.inventory.templateservice.controller;

import com.inventory.common.template.FieldType;
import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.ValidationRuleDTO;
import com.inventory.common.template.service.ICustomFieldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CustomFieldControllerTest {

    @Mock
    private ICustomFieldService customFieldService;

    @InjectMocks
    private CustomFieldController customFieldController;

    private CustomFieldDTO testField;
    private ValidationRuleDTO testRule;

    @BeforeEach
    void setUp() {
        testField = new CustomFieldDTO();
        testField.setId(1L);
        testField.setFieldCode("CUSTOM_FIELD_1");
        testField.setFieldName("Custom Field 1");
        testField.setFieldType(FieldType.TEXT);

        testRule = new ValidationRuleDTO();
        testRule.setId(10L);
        testRule.setRuleCode("RULE_001");
        testRule.setRuleName("Required");
    }

    @Test
    void testCreateCustomField() {
        CustomFieldDTO input = new CustomFieldDTO();
        input.setFieldCode("CF-002");
        when(customFieldService.createCustomField(eq(1L), any(CustomFieldDTO.class))).thenReturn(testField);

        ResponseEntity<CustomFieldDTO> response = customFieldController.createCustomField(1L, input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateCustomField() {
        when(customFieldService.updateCustomField(eq(1L), any(CustomFieldDTO.class))).thenReturn(testField);

        ResponseEntity<CustomFieldDTO> response = customFieldController.updateCustomField(1L, testField);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteCustomField() {
        doNothing().when(customFieldService).deleteCustomField(1L);

        ResponseEntity<Void> response = customFieldController.deleteCustomField(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customFieldService).deleteCustomField(1L);
    }

    @Test
    void testActivateCustomField() {
        doNothing().when(customFieldService).activateCustomField(1L);

        ResponseEntity<Void> response = customFieldController.activateCustomField(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customFieldService).activateCustomField(1L);
    }

    @Test
    void testDeactivateCustomField() {
        doNothing().when(customFieldService).deactivateCustomField(1L);

        ResponseEntity<Void> response = customFieldController.deactivateCustomField(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customFieldService).deactivateCustomField(1L);
    }

    @Test
    void testGetCustomFieldFound() {
        when(customFieldService.getCustomFieldById(1L)).thenReturn(Optional.of(testField));

        ResponseEntity<CustomFieldDTO> response = customFieldController.getCustomField(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetCustomFieldNotFound() {
        when(customFieldService.getCustomFieldById(99L)).thenReturn(Optional.empty());

        ResponseEntity<CustomFieldDTO> response = customFieldController.getCustomField(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetCustomFieldsByTemplate() {
        when(customFieldService.getCustomFieldsByTemplate(1L)).thenReturn(List.of(testField));

        ResponseEntity<List<CustomFieldDTO>> response = customFieldController.getCustomFieldsByTemplate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetActiveCustomFields() {
        when(customFieldService.getActiveCustomFields(1L)).thenReturn(List.of(testField));

        ResponseEntity<List<CustomFieldDTO>> response = customFieldController.getActiveCustomFields(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testValidateFieldValue() {
        when(customFieldService.validateFieldValue(eq(1L), any())).thenReturn(Map.of());

        ResponseEntity<Map<String, Object>> response = customFieldController.validateFieldValue(1L,
                Map.of("value", "test"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("valid"));
    }

    @Test
    void testValidateFieldValueInvalid() {
        when(customFieldService.validateFieldValue(eq(1L), any()))
                .thenReturn(Map.of("format", "Invalid format"));

        ResponseEntity<Map<String, Object>> response = customFieldController.validateFieldValue(1L,
                Map.of("value", "bad"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().get("valid"));
    }

    @Test
    void testCreateValidationRule() {
        when(customFieldService.createValidationRule(any(ValidationRuleDTO.class))).thenReturn(testRule);

        ResponseEntity<ValidationRuleDTO> response = customFieldController.createValidationRule(testRule);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateValidationRule() {
        when(customFieldService.updateValidationRule(eq(10L), any(ValidationRuleDTO.class))).thenReturn(testRule);

        ResponseEntity<ValidationRuleDTO> response = customFieldController.updateValidationRule(10L, testRule);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteValidationRule() {
        doNothing().when(customFieldService).deleteValidationRule(10L);

        ResponseEntity<Void> response = customFieldController.deleteValidationRule(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customFieldService).deleteValidationRule(10L);
    }

    @Test
    void testGetValidationRuleFound() {
        when(customFieldService.getValidationRuleById(10L)).thenReturn(Optional.of(testRule));

        ResponseEntity<ValidationRuleDTO> response = customFieldController.getValidationRule(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetValidationRuleNotFound() {
        when(customFieldService.getValidationRuleById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ValidationRuleDTO> response = customFieldController.getValidationRule(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetValidationRuleByCodeFound() {
        when(customFieldService.getValidationRuleByCode("RULE_001")).thenReturn(Optional.of(testRule));

        ResponseEntity<ValidationRuleDTO> response = customFieldController.getValidationRuleByCode("RULE_001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetValidationRuleByCodeNotFound() {
        when(customFieldService.getValidationRuleByCode("MISSING")).thenReturn(Optional.empty());

        ResponseEntity<ValidationRuleDTO> response = customFieldController.getValidationRuleByCode("MISSING");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetAllValidationRules() {
        when(customFieldService.getAllValidationRules()).thenReturn(List.of(testRule));

        ResponseEntity<List<ValidationRuleDTO>> response = customFieldController.getAllValidationRules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetBuiltInValidationRules() {
        when(customFieldService.getBuiltInValidationRules()).thenReturn(List.of(testRule));

        ResponseEntity<List<ValidationRuleDTO>> response = customFieldController.getBuiltInValidationRules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetValidationRulesByFieldType() {
        when(customFieldService.getValidationRulesByFieldType(FieldType.TEXT)).thenReturn(List.of(testRule));

        ResponseEntity<List<ValidationRuleDTO>> response =
                customFieldController.getValidationRulesByFieldType(FieldType.TEXT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
