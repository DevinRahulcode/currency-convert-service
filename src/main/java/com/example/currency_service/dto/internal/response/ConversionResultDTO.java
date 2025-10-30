package com.example.currency_service.dto.internal.response;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversionResultDTO {
    private BigDecimal originalAmount;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rateUsed;
    private BigDecimal convertedAmount;
    private LocalDateTime rateTimestamp;
    private String dataSource; // "DATABASE_CACHE" or "LIVE_API_FALLBACK"
}
