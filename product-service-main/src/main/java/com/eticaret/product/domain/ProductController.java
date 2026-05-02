package com.eticaret.product.domain;

import com.eticaret.common.ApiResponse;
import com.eticaret.common.PageResponse;
import com.eticaret.product.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "ÃœrÃ¼n yÃ¶netimi")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Yeni Ã¼rÃ¼n oluÅŸtur")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ÃœrÃ¼n oluÅŸturuldu", productService.createProduct(request)));
    }

    @GetMapping
    @Operation(summary = "ÃœrÃ¼nleri listele â€” sayfalama ve filtre")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, categoryId, minPrice, maxPrice, page, size, sortBy)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile Ã¼rÃ¼n getir")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "ÃœrÃ¼n gÃ¼ncelle")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("ÃœrÃ¼n gÃ¼ncellendi",
                productService.updateProduct(id, request)));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "ÃœrÃ¼n gÃ¶rseli yÃ¼kle")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("GÃ¶rsel yÃ¼klendi",
                productService.uploadImage(id, file)));
    }

    @PostMapping("/bulk-import")
    @Operation(summary = "Toplu Ã¼rÃ¼n import et (AI servis tarafÄ±ndan)")
    public ResponseEntity<ApiResponse<BulkImportResponse>> bulkImport(
            @RequestBody List<CreateProductRequest> requests) {
        BulkImportResponse result = productService.bulkImport(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        result.successCount() + " Ã¼rÃ¼n baÅŸarÄ±yla oluÅŸturuldu, " +
                                result.failureCount() + " hata",
                        result
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Ürün silindi", null));
    }
}
