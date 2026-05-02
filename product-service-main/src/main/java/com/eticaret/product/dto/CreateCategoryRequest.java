package com.eticaret.product.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Kategori adı boş olamaz")
        String name,

        String description
) {}