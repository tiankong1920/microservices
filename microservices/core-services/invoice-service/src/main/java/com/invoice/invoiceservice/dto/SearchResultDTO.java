package com.invoice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDTO<T> {

    private T data;
    private double matchScore;
    private String matchType;

    public static <T> SearchResultDTO<T> of(T data, double matchScore, String matchType) {
        return new SearchResultDTO<>(data, matchScore, matchType);
    }
}
