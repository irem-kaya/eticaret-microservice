package com.eticaret.product.domain;

import com.eticaret.common.PageResponse;
import com.eticaret.product.dto.*;
import com.eticaret.product.exception.CategoryNotFoundException;
import com.eticaret.product.exception.ProductNotFoundException;
import com.eticaret.product.features.authorization.StockAuthorizationService;
import com.eticaret.product.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @Mock StorageService storageService;
    @Mock StockAuthorizationService stockAuthorizationService;

    @InjectMocks ProductService productService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(productService, "lowStockKey", "low-stock");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Category buildCategory(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Product buildProduct(Long id, String name, BigDecimal price, int stock, Category category) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        return p;
    }

    private CreateProductRequest buildCreateRequest(String name, BigDecimal price, int stock, Long catId) {
        return new CreateProductRequest(name, "desc", price, stock, catId, null);
    }

    // ── createProduct ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("geçerli request → ürün kaydedilir ve event yayınlanır")
        void createProduct_validRequest_savesAndPublishesEvent() {
            Category cat = buildCategory(1L, "Elektronik");
            CreateProductRequest req = buildCreateRequest("Laptop", new BigDecimal("15000"), 10, 1L);
            Product saved = buildProduct(1L, "Laptop", new BigDecimal("15000"), 10, cat);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(productRepository.save(any())).thenReturn(saved);

            ProductResponse resp = productService.createProduct(req);

            assertThat(resp.name()).isEqualTo("Laptop");
            verify(productRepository).save(any(Product.class));
            verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("product.added"), anyMap());
        }

        @Test
        @DisplayName("bilinmeyen kategori → CategoryNotFoundException")
        void createProduct_unknownCategory_throws() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
            CreateProductRequest req = buildCreateRequest("X", BigDecimal.TEN, 1, 99L);

            assertThatThrownBy(() -> productService.createProduct(req))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("RabbitMQ hatası → exception fırlatılmaz (log warn)")
        void createProduct_rabbitFails_noExceptionPropagated() {
            Category cat = buildCategory(1L, "Elektronik");
            Product saved = buildProduct(1L, "Laptop", BigDecimal.TEN, 10, cat);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(productRepository.save(any())).thenReturn(saved);
            doThrow(new RuntimeException("rabbit down"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

            assertThatNoException().isThrownBy(
                    () -> productService.createProduct(buildCreateRequest("Laptop", BigDecimal.TEN, 10, 1L)));
        }
    }

    // ── getProductById ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("mevcut id → ProductResponse döner")
        void getProductById_exists_returnsResponse() {
            Category cat = buildCategory(1L, "Elektronik");
            Product p = buildProduct(1L, "Phone", new BigDecimal("5000"), 20, cat);
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductResponse resp = productService.getProductById(1L);

            assertThat(resp.id()).isEqualTo(1L);
            assertThat(resp.name()).isEqualTo("Phone");
        }

        @Test
        @DisplayName("bilinmeyen id → ProductNotFoundException")
        void getProductById_notFound_throws() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    // ── updateProduct ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("stok eşiğin altında → low-stock event gönderilir")
        void updateProduct_lowStock_publishesLowStockEvent() {
            Category cat = buildCategory(1L, "Ev");
            Product p = buildProduct(1L, "Kablo", new BigDecimal("50"), 10, cat);
            UpdateProductRequest req = new UpdateProductRequest("Kablo", "desc", new BigDecimal("50"), 3,"");

            when(productRepository.findById(1L)).thenReturn(Optional.of(p));
            when(productRepository.save(any())).thenReturn(p);

            productService.updateProduct(1L, req);

            verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("low-stock"), (Object) any());
        }

        @Test
        @DisplayName("stok eşiğin üstünde → low-stock event gönderilmez")
        void updateProduct_highStock_noLowStockEvent() {
            Category cat = buildCategory(1L, "Ev");
            Product p = buildProduct(1L, "Kablo", new BigDecimal("50"), 2, cat);
            UpdateProductRequest req = new UpdateProductRequest("Kablo", "desc", new BigDecimal("50"), 100,"");

            when(productRepository.findById(1L)).thenReturn(Optional.of(p));
            when(productRepository.save(any())).thenReturn(p);

            productService.updateProduct(1L, req);

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }

    // ── searchProducts ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchProducts()")
    class SearchProducts {

        @Test
        @DisplayName("keyword ile arama → searchByKeyword çağrılır")
        void searchProducts_withKeyword_callsKeywordRepo() {
            when(stockAuthorizationService.canViewStock()).thenReturn(true);
            Page<Product> empty = Page.empty();
            when(productRepository.searchByKeyword(eq("laptop"), any())).thenReturn(empty);

            PageResponse<ProductResponse> resp =
                    productService.searchProducts("laptop", null, null, null, 0, 10, "createdAt");

            verify(productRepository).searchByKeyword(eq("laptop"), any());
            assertThat(resp.content()).isEmpty();
        }

        @Test
        @DisplayName("kategori filtresi → findByCategoryId çağrılır")
        void searchProducts_withCategory_callsCategoryRepo() {
            when(stockAuthorizationService.canViewStock()).thenReturn(false);
            Page<Product> empty = Page.empty();
            when(productRepository.findByCategoryIdAndActiveTrue(eq(2L), any())).thenReturn(empty);

            productService.searchProducts(null, 2L, null, null, 0, 10, null);

            verify(productRepository).findByCategoryIdAndActiveTrue(eq(2L), any());
        }

        @Test
        @DisplayName("fiyat aralığı filtresi → findByPriceRange çağrılır")
        void searchProducts_withPriceRange_callsPriceRangeRepo() {
            when(stockAuthorizationService.canViewStock()).thenReturn(true);
            Page<Product> empty = Page.empty();
            when(productRepository.findByPriceRange(any(), any(), any())).thenReturn(empty);

            productService.searchProducts(null, null, BigDecimal.TEN, new BigDecimal("500"), 0, 10, "price");

            verify(productRepository).findByPriceRange(any(), any(), any());
        }
    }

    // ── bulkImport ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("bulkImport()")
    class BulkImport {

        @Test
        @DisplayName("tüm geçerli → successCount == total")
        void bulkImport_allValid_allSuccess() {
            Category cat = buildCategory(1L, "Elektronik");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(productRepository.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            List<CreateProductRequest> reqs = List.of(
                    buildCreateRequest("P1", BigDecimal.TEN, 5, 1L),
                    buildCreateRequest("P2", BigDecimal.TEN, 3, 1L)
            );

            BulkImportResponse resp = productService.bulkImport(reqs);

            assertThat(resp.successCount()).isEqualTo(2);
            assertThat(resp.failureCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("hatalı kategori → failureCount artar, diğerleri başarılı")
        void bulkImport_someInvalid_partialSuccess() {
            Category cat = buildCategory(1L, "Elektronik");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
            when(productRepository.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            List<CreateProductRequest> reqs = List.of(
                    buildCreateRequest("OK", BigDecimal.TEN, 5, 1L),
                    buildCreateRequest("FAIL", BigDecimal.TEN, 5, 99L)
            );

            BulkImportResponse resp = productService.bulkImport(reqs);

            assertThat(resp.successCount()).isEqualTo(1);
            assertThat(resp.failureCount()).isEqualTo(1);
        }
    }
}