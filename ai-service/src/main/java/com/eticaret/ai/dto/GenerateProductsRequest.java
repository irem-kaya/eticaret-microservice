package com.eticaret.ai.dto;

public record GenerateProductsRequest(
    String category,
    Long categoryId,
    int count
) {}