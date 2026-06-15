package com.inventory.productservice.controller;

import com.inventory.productservice.dto.ProductCategoryDTO;
import com.inventory.productservice.service.ProductCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductCategoryControllerTest {

    @Mock
    private ProductCategoryService categoryService;

    @InjectMocks
    private ProductCategoryController categoryController;

    private ProductCategoryDTO rootCategory;
    private ProductCategoryDTO childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = new ProductCategoryDTO();
        rootCategory.setId(1L);
        rootCategory.setName("Electronics");
        rootCategory.setLevel(1);

        childCategory = new ProductCategoryDTO();
        childCategory.setId(2L);
        childCategory.setName("Phones");
        childCategory.setParentId(1L);
        childCategory.setLevel(2);
    }

    @Test
    void testGetCategory() {
        when(categoryService.getCategoryById(1L)).thenReturn(rootCategory);

        ResponseEntity<ProductCategoryDTO> response = categoryController.getCategory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics", response.getBody().getName());
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void testGetCategoryTree() {
        when(categoryService.getCategoryTree()).thenReturn(List.of(rootCategory, childCategory));

        ResponseEntity<List<ProductCategoryDTO>> response = categoryController.getCategoryTree();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetSubCategories() {
        when(categoryService.getSubCategories(1L)).thenReturn(List.of(childCategory));

        ResponseEntity<List<ProductCategoryDTO>> response = categoryController.getSubCategories(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getParentId());
    }

    @Test
    void testCreateCategory() {
        ProductCategoryDTO input = new ProductCategoryDTO();
        input.setName("Clothing");

        ProductCategoryDTO created = new ProductCategoryDTO();
        created.setId(3L);
        created.setName("Clothing");
        created.setLevel(1);

        when(categoryService.createCategory(any(ProductCategoryDTO.class))).thenReturn(created);

        ResponseEntity<ProductCategoryDTO> response = categoryController.createCategory(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getId());
    }

    @Test
    void testUpdateCategory() {
        ProductCategoryDTO input = new ProductCategoryDTO();
        input.setName("Electronics & Gadgets");

        ProductCategoryDTO updated = new ProductCategoryDTO();
        updated.setId(1L);
        updated.setName("Electronics & Gadgets");

        when(categoryService.updateCategory(eq(1L), any(ProductCategoryDTO.class))).thenReturn(updated);

        ResponseEntity<ProductCategoryDTO> response = categoryController.updateCategory(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics & Gadgets", response.getBody().getName());
    }

    @Test
    void testDeleteCategory() {
        doNothing().when(categoryService).deleteCategory(1L);

        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void testUpdateCategoryStatus() {
        ProductCategoryDTO updated = new ProductCategoryDTO();
        updated.setId(1L);
        updated.setName("Electronics");
        updated.setStatus("INACTIVE");

        when(categoryService.updateCategoryStatus(1L, "INACTIVE")).thenReturn(updated);

        ResponseEntity<ProductCategoryDTO> response = categoryController.updateCategoryStatus(1L, "INACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INACTIVE", response.getBody().getStatus());
    }

    @Test
    void testMoveCategory() {
        doNothing().when(categoryService).moveCategory(2L, 5L);

        ResponseEntity<Void> response = categoryController.moveCategory(2L, 5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryService).moveCategory(2L, 5L);
    }

    @Test
    void testSortCategories() {
        doNothing().when(categoryService).sortCategories(List.of(1L, 2L, 3L));

        ResponseEntity<Void> response = categoryController.sortCategories(List.of(1L, 2L, 3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService).sortCategories(List.of(1L, 2L, 3L));
    }
}
