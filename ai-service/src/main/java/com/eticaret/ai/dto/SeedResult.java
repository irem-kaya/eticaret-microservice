package com.eticaret.ai.dto;

import java.util.List;

public record SeedResult(
    int categoriesCreated,
    int productsCreated,
    int productsFailed,
    List<String> messages
) {}