package com.inventory.productservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.productservice.dto.ProductCategoryDTO;
import com.inventory.productservice.entity.ProductCategory;
import com.inventory.productservice.exception.CategoryException;
import com.inventory.productservice.repository.IProductCategoryRepository;
import com.inventory.productservice.service.ProductCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final IProductCategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProductCategoryDTO getCategoryById(final Long id) {
        log.debug("Getting category by id: {}", id);
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Category", id));
        return toDTO(category);
    }

    @Override
    public List<ProductCategoryDTO> getCategoryTree() {
        log.debug("Getting category tree");
        List<ProductCategory> rootCategories = categoryRepository
                .findByParentIdIsNullOrderBySortOrderAsc();
        return rootCategories.stream()
                .map(this::toDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryDTO> getSubCategories(final Long parentId) {
        log.debug("Getting sub categories for parent id: {}", parentId);
        List<ProductCategory> subCategories = categoryRepository
                .findByParentIdOrderBySortOrderAsc(parentId);
        return subCategories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductCategoryDTO createCategory(final ProductCategoryDTO dto) {
        log.info("Creating category: {}", dto.getName());
        ProductCategory category = new ProductCategory();
        category.setName(dto.getName());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        if (dto.getParentId() != null) {
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> EntityNotFoundException.forEntity("Parent Category", dto.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        ProductCategory saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public ProductCategoryDTO updateCategory(final Long id, final ProductCategoryDTO dto) {
        log.info("Updating category with id: {}", id);
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Category", id));

        category.setName(dto.getName());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }

        ProductCategory saved = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(final Long id) {
        log.info("Deleting category with id: {}", id);
        List<ProductCategory> children = categoryRepository
                .findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw CategoryException.cannotDeleteWithSubCategories();
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public ProductCategoryDTO updateCategoryStatus(final Long id, final String status) {
        log.info("Updating category status with id: {} to {}", id, status);
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Category", id));
        category.setStatus(status);
        ProductCategory saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void moveCategory(final Long id, final Long newParentId) {
        log.info("Moving category {} to parent {}", id, newParentId);
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Category", id));

        if (newParentId == null) {
            category.setParent(null);
            category.setLevel(1);
        } else {
            ProductCategory newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> EntityNotFoundException.forEntity("Parent Category", newParentId));
            category.setParent(newParent);
            category.setLevel(newParent.getLevel() + 1);
        }
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void sortCategories(final List<Long> categoryIds) {
        log.info("Sorting categories");
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            ProductCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> EntityNotFoundException.forEntity("Category", categoryId));
            category.setSortOrder(i);
            categoryRepository.save(category);
        }
    }

    private ProductCategoryDTO toDTO(final ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        dto.setLevel(category.getLevel());
        dto.setSortOrder(category.getSortOrder());
        dto.setStatus(category.getStatus());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    private ProductCategoryDTO toDTOWithChildren(final ProductCategory category) {
        ProductCategoryDTO dto = toDTO(category);
        List<ProductCategory> children = categoryRepository
                .findByParentIdOrderBySortOrderAsc(category.getId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::toDTOWithChildren)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildren(new ArrayList<>());
        }
        return dto;
    }
}
