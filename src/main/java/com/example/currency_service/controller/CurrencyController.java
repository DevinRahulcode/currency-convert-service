package com.example.currency_service.controller;

import com.example.currency_service.dto.internal.request.ConversionRequestDTO;
import com.example.currency_service.dto.internal.response.ConversionResultDTO;
import com.example.currency_service.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/converter")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @PostMapping("/convert")
    public ResponseEntity<ConversionResultDTO> convert(
            @RequestBody ConversionRequestDTO requestDTO) {

        ConversionResultDTO result = currencyService.convertCurrency(requestDTO);
        return ResponseEntity.ok(result);
    }
}
