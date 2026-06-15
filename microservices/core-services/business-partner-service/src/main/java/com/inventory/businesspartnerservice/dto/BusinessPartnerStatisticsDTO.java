package com.inventory.businesspartnerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessPartnerStatisticsDTO {

    private long totalPartners;
    private long activePartners;
    private long inactivePartners;
    private Map<String, Long> partnersByType;
    private Map<String, Long> partnersByCountry;
    private Map<String, Long> partnersByCity;
}
