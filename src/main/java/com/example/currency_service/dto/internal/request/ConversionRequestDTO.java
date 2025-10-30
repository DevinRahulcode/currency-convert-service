package com.example.currency_service.dto.internal.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ConversionRequestDTO {
    private String from; // e.g., "USD"
    private String to;   // e.g., "EUR"
    private BigDecimal amount;
}