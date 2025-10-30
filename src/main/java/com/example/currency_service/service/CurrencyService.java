package com.example.currency_service.service;

import com.example.currency_service.dto.external.FrankfurterDTO;
import com.example.currency_service.dto.internal.request.ConversionRequestDTO;
import com.example.currency_service.dto.internal.response.ConversionResultDTO;
import com.example.currency_service.entity.ExchangeRate;
import com.example.currency_service.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.frankfurter.url}")
    private String frankfurterApiUrl;

    // We'll consider a rate "stale" after 1 hour (3600 seconds)
    private static final long STALE_RATE_SECONDS = 3600;

    public ConversionResultDTO convertCurrency(ConversionRequestDTO request) {
        String from = request.getFrom().toUpperCase();
        String to = request.getTo().toUpperCase();
        BigDecimal amount = request.getAmount();

        // 1. Try to get rate from database
        Optional<ExchangeRate> cachedRate = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrency(from, to);

        String dataSource;
        ExchangeRate rateToUse;

        if (cachedRate.isPresent() && isRateRecent(cachedRate.get())) {
            // 2. Use recent rate from DB
            rateToUse = cachedRate.get();
            dataSource = "DATABASE_CACHE";
        } else {
            // 3. Fetch from API and update DB
            rateToUse = fetchAndCacheRate(from, to);
            dataSource = "LIVE_API_FALLBACK";
        }

        // 4. Perform calculation
        BigDecimal convertedAmount = amount.multiply(rateToUse.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Build and return the internal DTO
        return ConversionResultDTO.builder()
                .originalAmount(amount)
                .fromCurrency(from)
                .toCurrency(to)
                .rateUsed(rateToUse.getRate())
                .convertedAmount(convertedAmount)
                .rateTimestamp(rateToUse.getLastUpdated())
                .dataSource(dataSource)
                .build();
    }

    private boolean isRateRecent(ExchangeRate rate) {
        return rate.getLastUpdated().isAfter(LocalDateTime.now().minusSeconds(STALE_RATE_SECONDS));
    }

    private ExchangeRate fetchAndCacheRate(String from, String to) {
        // 1. Build 3rd-party API URL
        String url = UriComponentsBuilder.fromHttpUrl(frankfurterApiUrl)
                .path("/latest")
                .queryParam("from", from)
                .queryParam("to", to)
                .toUriString();

        // 2. Call 3rd-party API
        FrankfurterDTO response = restTemplate.getForObject(url, FrankfurterDTO.class);

        if (response == null || response.getRates() == null || !response.getRates().containsKey(to)) {
            throw new RuntimeException("Could not fetch exchange rate from external API.");
        }

        // 3. Get the specific rate
        BigDecimal rateValue = response.getRates().get(to);

        // 4. Find or create the rate in our DB
        ExchangeRate rateEntity = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrency(from, to)
                .orElse(new ExchangeRate()); // Create new if it doesn't exist

        // 5. Update and save to DB (cache it)
        rateEntity.setBaseCurrency(from);
        rateEntity.setTargetCurrency(to);
        rateEntity.setRate(rateValue);
        rateEntity.setLastUpdated(LocalDateTime.now());

        return exchangeRateRepository.save(rateEntity);
    }
}