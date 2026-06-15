package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.InvoiceProductDTO;

public interface InvoiceProductService {

    InvoiceProductDTO createProduct(InvoiceProductDTO dto);

    InvoiceProductDTO updateProduct(Long id, InvoiceProductDTO dto);

    InvoiceProductDTO getProductById(Long id);

    List<InvoiceProductDTO> getAllProducts();

    List<InvoiceProductDTO> getActiveProducts();

    void deleteProduct(Long id);

    List<InvoiceProductDTO> searchProducts(String keyword);

    void incrementUsageCount(Long id);

    List<InvoiceProductDTO> getFrequentlyUsedProducts(int limit);

    List<InvoiceProductDTO> recommendProductsByCustomer(Long customerId, int limit);
}
