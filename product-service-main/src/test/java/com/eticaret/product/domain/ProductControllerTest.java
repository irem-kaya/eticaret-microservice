package com.eticaret.product.domain;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.eticaret.common.PageResponse;
import com.eticaret.product.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ProductService productService;

    // ProductResponse(id, name, description, price, stock, imageUrl, categoryName, createdAt)
    private ProductResponse sampleResponse(Long id, String name) {
        return new ProductResponse(id, name, "desc", new BigDecimal("100"), 10, null, true, "Elektronik", null);
    }

    // CreateProductRequest(name, description, price, stock, categoryId, imageUrl)
    private CreateProductRequest buildCreateRequest(String name, BigDecimal price, int stock, Long catId) {
        return new CreateProductRequest(name, "desc", price, stock, catId, null);
    }

    // ── POST /api/products ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/products")
    class CreateProduct {

        @Test
        @DisplayName("geçerli body → 201 Created")
        void createProduct_valid_returns201() throws Exception {
            CreateProductRequest req = buildCreateRequest("Laptop", new BigDecimal("15000"), 10, 1L);
            ProductResponse resp = sampleResponse(1L, "Laptop");
            when(productService.createProduct(any())).thenReturn(resp);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("Laptop"));
        }

        @Test
        @DisplayName("eksik alan → 400 Bad Request")
        void createProduct_missingName_returns400() throws Exception {
            String invalidJson = """
                    { "description": "desc", "price": 100, "stock": 1, "categoryId": 1 }
                    """;
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/products ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/products")
    class GetProducts {

        @Test
        @DisplayName("parametresiz → 200, boş liste")
        void getProducts_noParams_returns200() throws Exception {
            PageResponse<ProductResponse> page = new PageResponse<>(List.of(), 0, 20, 0, 0, true);
            when(productService.searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("keyword parametresi → service'e iletilir")
        void getProducts_withKeyword_callsService() throws Exception {
            PageResponse<ProductResponse> page = new PageResponse<>(List.of(), 0, 20, 0, 0, true);
            when(productService.searchProducts(eq("laptop"), any(), any(), any(), anyInt(), anyInt(), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/products").param("keyword", "laptop"))
                    .andExpect(status().isOk());

            verify(productService).searchProducts(eq("laptop"), any(), any(), any(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("fiyat aralığı parametresi → service'e iletilir")
        void getProducts_withPriceRange_callsService() throws Exception {
            PageResponse<ProductResponse> page = new PageResponse<>(List.of(), 0, 20, 0, 0, true);
            when(productService.searchProducts(any(), any(),
                    eq(new BigDecimal("100")), eq(new BigDecimal("500")),
                    anyInt(), anyInt(), any())).thenReturn(page);

            mockMvc.perform(get("/api/products")
                            .param("minPrice", "100")
                            .param("maxPrice", "500"))
                    .andExpect(status().isOk());
        }
    }

    // ── GET /api/products/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("mevcut id → 200, doğru ürün")
        void getById_exists_returns200() throws Exception {
            when(productService.getProductById(1L)).thenReturn(sampleResponse(1L, "Phone"));

            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Phone"));
        }
    }

    // ── PUT /api/products/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("geçerli güncelleme → 200")
        void updateProduct_valid_returns200() throws Exception {
            UpdateProductRequest req = new UpdateProductRequest("Updated", "desc", new BigDecimal("200"), 5,"url");
            when(productService.updateProduct(eq(1L), any())).thenReturn(sampleResponse(1L, "Updated"));

            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Updated"));
        }
    }

    // ── POST /api/products/bulk-import ────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/products/bulk-import")
    class BulkImport {

        @Test
        @DisplayName("geçerli liste → 201, successCount doğru")
        void bulkImport_valid_returns201() throws Exception {
            List<CreateProductRequest> reqs = List.of(
                    buildCreateRequest("P1", BigDecimal.TEN, 1, 1L)
            );
            BulkImportResponse bulkResp = new BulkImportResponse(1, 0, 1, List.of());
            when(productService.bulkImport(any())).thenReturn(bulkResp);

            mockMvc.perform(post("/api/products/bulk-import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqs)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.successCount").value(1));
        }
    }
}