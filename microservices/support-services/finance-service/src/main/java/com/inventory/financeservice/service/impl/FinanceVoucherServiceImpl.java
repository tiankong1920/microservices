package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.FinanceVoucherDTO;
import com.inventory.financeservice.entity.FinanceVoucher;
import com.inventory.financeservice.repository.IFinanceVoucherRepository;
import com.inventory.financeservice.service.IFinanceVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Finance Voucher Service Implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FinanceVoucherServiceImpl implements IFinanceVoucherService {

    private final IFinanceVoucherRepository financeVoucherRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<FinanceVoucherDTO> getAllFinanceVouchers() {
        log.debug("Getting all finance vouchers");
        return financeVoucherRepository.findAll().stream()
                .map(voucher -> modelMapper.map(voucher, FinanceVoucherDTO.class))
                .toList();
    }

    @Override
    public FinanceVoucherDTO getFinanceVoucherById(Long id) {
        log.debug("Getting finance voucher by id: {}", id);
        FinanceVoucher voucher = financeVoucherRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Finance Voucher not found with id: {}", id);
                    return new RuntimeException("Finance Voucher not found with id: " + id);
                });
        return modelMapper.map(voucher, FinanceVoucherDTO.class);
    }

    @Override
    @Transactional
    public FinanceVoucherDTO createFinanceVoucher(FinanceVoucherDTO financeVoucherDTO) {
        log.debug("Creating finance voucher: {}", financeVoucherDTO.getVoucherNumber());
        FinanceVoucher voucher = modelMapper.map(financeVoucherDTO, FinanceVoucher.class);
        FinanceVoucher savedVoucher = financeVoucherRepository.save(voucher);
        log.debug("Finance voucher created successfully with id: {}", savedVoucher.getId());
        return modelMapper.map(savedVoucher, FinanceVoucherDTO.class);
    }

    @Override
    @Transactional
    public FinanceVoucherDTO updateFinanceVoucher(Long id, FinanceVoucherDTO financeVoucherDTO) {
        log.debug("Updating finance voucher with id: {}", id);
        FinanceVoucher existingVoucher = financeVoucherRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Finance Voucher not found with id: {}", id);
                    return new RuntimeException("Finance Voucher not found with id: " + id);
                });
        modelMapper.map(financeVoucherDTO, existingVoucher);
        FinanceVoucher updatedVoucher = financeVoucherRepository.save(existingVoucher);
        log.debug("Finance voucher updated successfully with id: {}", updatedVoucher.getId());
        return modelMapper.map(updatedVoucher, FinanceVoucherDTO.class);
    }

    @Override
    @Transactional
    public void deleteFinanceVoucher(Long id) {
        log.debug("Deleting finance voucher with id: {}", id);
        financeVoucherRepository.deleteById(id);
        log.debug("Finance voucher deleted successfully with id: {}", id);
    }

}
