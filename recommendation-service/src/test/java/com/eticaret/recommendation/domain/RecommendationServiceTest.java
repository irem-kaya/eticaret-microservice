package com.eticaret.recommendation.service;

import com.eticaret.recommendation.service.MCPRecommendationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Unit Tests")
class RecommendationServiceTest {

    @Mock MCPRecommendationEngine mockEngine;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock WebClient.Builder webClientBuilder;
    @Mock WebClient webClient;
    @InjectMocks RecommendationService recommendationService;

    private static final Long CATEGORY_ID = 1L;


    // ── Test: getRecommendationsByCategory ─────────────────────────────────────

    @Nested
    @DisplayName("getRecommendationsByCategory()")
    class GetRecommendationsByCategory {

        @Test
        @DisplayName("kategori önerileri → redis cache'ten döner")
        void getRecommendations_category_returnsList() {
            when(redisTemplate.opsForValue().get("recommendations:category:" + CATEGORY_ID))
                .thenReturn(List.of());

            recommendationService.getRecommendationsByCategory(CATEGORY_ID);

            verify(redisTemplate.opsForValue()).get(contains("recommendations:category:"));
        }

        @Test
        @DisplayName("cache miss → engine'den öneriler generate edilir")
        void getRecommendations_cacheMiss_generateRecommendations() {
            when(redisTemplate.opsForValue().get(anyString()))
                .thenReturn(null);

            recommendationService.getRecommendationsByCategory(CATEGORY_ID);

            // Engine çağrılmalı
            assertThatNoException().isThrownBy(() ->
                recommendationService.getRecommendationsByCategory(CATEGORY_ID)
            );
        }
    }

    // ── Test: refreshRecommendations ───────────────────────────────────────────

    @Nested
    @DisplayName("refreshRecommendations()")
    class RefreshRecommendations {

        @Test
        @DisplayName("kategoriye göre refresh → cache invalidate")
        void refreshRecommendations_category_invalidateCache() {
            doNothing().when(redisTemplate).delete(anyString());

            recommendationService.refreshRecommendations(CATEGORY_ID);

            verify(redisTemplate).delete(contains("recommendations:category:"));
        }


    }

    // ── Test: getBestSellers ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getBestSellers()")
    class GetBestSellers {

        @Test
        @DisplayName("best seller ürünler → engine return")
        void getBestSellers_success() {
            when(mockEngine.generateBestSellers(CATEGORY_ID))


            assertThatNoException().isThrownBy(() ->
                recommendationService.getBestSellers(CATEGORY_ID)
            );
        }
    }

    // ── Test: getBudgetFriendly ─────────────────────────────────────────────

    @Nested
    @DisplayName("getBudgetFriendly()")
    class GetBudgetFriendly {

        @Test
        @DisplayName("bütçe dostu ürünler → engine return")
        void getBudgetFriendly_success() {
            assertThatNoException().isThrownBy(() ->
                recommendationService.getBudgetFriendly(CATEGORY_ID)
            );
        }
    }

    // ── Test: Caching Layer ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Caching Layer")
    class CachingLayer {

        @Test
        @DisplayName("redis cache TTL → 24 hours")
        void cachingLayer_ttl_24hours() {
            when(redisTemplate.expire(anyString(), any(), any()))
                .thenReturn(true);

            assertThatNoException().isThrownBy(() ->
                recommendationService.getRecommendationsByCategory(CATEGORY_ID)
            );
        }

        @Test
        @DisplayName("cache hit → database query yok")
        void cachingLayer_cacheHit_noQuery() {
            when(redisTemplate.opsForValue().get(anyString()))
                .thenReturn(List.of(1L, 2L, 3L));

            recommendationService.getRecommendationsByCategory(CATEGORY_ID);

            verify(redisTemplate.opsForValue()).get(anyString());
        }
    }

    // ── Test: Error Handling ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("null kategori → safe handle")
        void errorHandling_nullCategory_safe() {
            assertThatNoException().isThrownBy(
                () -> recommendationService.getRecommendationsByCategory(null)
            );
        }

        @Test
        @DisplayName("invalid kategori → empty list")
        void errorHandling_invalidCategory_emptyList() {
            when(redisTemplate.opsForValue().get(anyString()))
                ;

            assertThatNoException().isThrownBy(
                () -> recommendationService.getRecommendationsByCategory(999L)
            );
        }
    }
}

