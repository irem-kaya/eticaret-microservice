package com.eticaret.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDataDto(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("price") BigDecimal price,
    @JsonProperty("stock") Integer stock,
    @JsonProperty("imageKeyword") String imageKeyword
) {}