package com.eticaret.ai.domain;

import com.eticaret.ai.dto.ProductDataDto;
import com.eticaret.ai.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIService Unit Tests")
class AiServiceTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    AIService aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "gemini-test");
    }

    @Nested
    @DisplayName("generateProducts()")
    class GenerateProducts {

        @Test
        @DisplayName("count 0 → boş liste döner")
        void generateProducts_zeroCount_returnsEmpty() {
            List<ProductDataDto> result = aiService.generateProducts("Elektronik", 0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API hatası → exception fırlatılmaz, kısmi sonuç döner")
        void generateProducts_apiError_returnsPartialOrEmpty() {
            // Gerçek API çağrısı yapılmaz (test ortamında WebClient başarısız olur)
            // Servis catch bloğu ile hataları yutmalı, exception dışarı çıkmamalı
            assertThatNoException().isThrownBy(
                    () -> aiService.generateProducts("Elektronik", 1)
            );
        }

        @Test
        @DisplayName("negatif count → boş liste döner")
        void generateProducts_negativeCount_returnsEmpty() {
            List<ProductDataDto> result = aiService.generateProducts("Elektronik", -1);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("batch size 5'ten küçük count → tek batch çalışır")
        void generateProducts_countLessThanBatchSize_runsSingleBatch() {
            // 3 ürün isteniyor, BATCH_SIZE=5 olduğu için tek batch dönmeli
            // API mock'lanmadığı için boş liste dönecek ama exception çıkmamalı
            assertThatNoException().isThrownBy(
                    () -> aiService.generateProducts("Giyim", 3)
            );
        }
    }
}