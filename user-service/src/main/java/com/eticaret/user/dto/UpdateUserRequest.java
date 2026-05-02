package com.eticaret.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Ad boş olamaz")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        String lastName
) {}