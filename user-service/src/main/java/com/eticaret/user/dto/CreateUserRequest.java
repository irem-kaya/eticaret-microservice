package com.eticaret.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "Ad boş olamaz")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        String lastName,

        @Email(message = "Geçerli email giriniz")
        @NotBlank(message = "Email boş olamaz")
        String email,

        @NotBlank(message = "Keycloak ID boş olamaz")
        String keycloakId
) {}