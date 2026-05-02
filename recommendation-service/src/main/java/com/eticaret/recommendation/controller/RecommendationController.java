package com.eticaret.recommendation.controller;

import com.eticaret.common.ApiResponse;
import com.eticaret.recommendation.service.MCPRecommendationEngine.RecommendationGroup;
import com.eticaret.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "MCP Önerisi API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Kategori için önerileri getir")
    public ResponseEntity<ApiResponse<List<RecommendationGroup>>> getRecommendations(
            @PathVariable Long categoryId) {
        List<RecommendationGroup> recommendations = recommendationService.getRecommendations(categoryId);

        if (recommendations.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Bu kategori için henüz yeterli öneri bulunmuyor",
                    recommendations
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                recommendations.size() + " öneri grubu bulundu",
                recommendations
        ));
    }

    @PostMapping("/refresh/{categoryId}")
    @Operation(summary = "Kategori önerilerini yenile (admin)")
    public ResponseEntity<ApiResponse<String>> refreshRecommendations(
            @PathVariable Long categoryId) {
        try {
            recommendationService.updateRecommendations(categoryId);
            return ResponseEntity.ok(ApiResponse.success(
                    "Kategori " + categoryId + " için öneriler yenilendi",
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Yenileme başarısız: " + e.getMessage()));
        }
    }

    @DeleteMapping("/category/{categoryId}")
    @Operation(summary = "Kategori önerilerini sil (admin)")
    public ResponseEntity<ApiResponse<Void>> clearRecommendations(
            @PathVariable Long categoryId) {
        recommendationService.clearRecommendations(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Öneriler silindi", null));
    }
}

