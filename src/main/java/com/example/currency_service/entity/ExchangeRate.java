package com.example.currency_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(indexes = {
        // Add an index for faster lookups
        @Index(name = "idx_currency_pair", columnList = "baseCurrency, targetCurrency")
})
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baseCurrency;  // e.g., "USD"
    private String targetCurrency; // e.g., "EUR"

    // Use BigDecimal for precise financial calculations
    private BigDecimal rate;

    private LocalDateTime lastUpdated;
}