package com.eticaret.product.domain;

import com.eticaret.common.PageResponse;
import com.eticaret.product.dto.*;
import com.eticaret.product.event.LowStockEvent;
import com.eticaret.product.exception.*;
import com.eticaret.product.features.authorization.StockAuthorizationService;
import com.eticaret.product.storage.StorageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final StorageService storageService;
    private final StockAuthorizationService stockAuthorizationService;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.low-stock}")
    private String lowStockKey;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          RabbitTemplate rabbitTemplate,
                          StorageService storageService,
                          StockAuthorizationService stockAuthorizationService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.storageService = storageService;
        this.stockAuthorizationService = stockAuthorizationService;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        publishProductAddedEvent(saved, category);
        return ProductResponse.from(saved);
    }

    private void publishProductAddedEvent(Product product, Category category) {
        try {
            Map<String, Object> event = Map.of(
                    "productId", product.getId(),
                    "productName", product.getName(),
                    "categoryId", category.getId(),
                    "categoryName", category.getName(),
                    "timestamp", System.currentTimeMillis()
            );
            rabbitTemplate.convertAndSend(exchange, "product.added", event);
        } catch (Exception e) {
            log.warn("Product added event publish basarisiz: {}", e.getMessage());
        }
    }

    @Cacheable(value = "products", key = "#pageable")
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findByActiveTrue(pageable);
        return toPageResponse(page);
    }

    public PageResponse<ProductResponse> searchProducts(
            String keyword, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy) {

        Sort sort;
        if (sortBy == null || sortBy.equals("createdAt")) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if (sortBy.equals("price")) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if (sortBy.equals("name_asc")) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if (sortBy.equals("name_desc")) {
            sort = Sort.by(Sort.Direction.DESC, "name");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> result;

        if (keyword != null && !keyword.isBlank()) {
            result = productRepository.searchByKeyword(keyword, pageable);
        } else if (categoryId != null) {
            result = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            result = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        } else {
            result = productRepository.findByActiveTrue(pageable);
        }

        return toPageResponse(result);
    }

    public ProductResponse getProductById(Long id) {
        return ProductResponse.from(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            product.setImageUrl(request.imageUrl());
        }

        if (product.getStock() <= LOW_STOCK_THRESHOLD) {
            rabbitTemplate.convertAndSend(exchange, lowStockKey,
                    LowStockEvent.of(product.getId(), product.getName(), product.getStock()));
        }

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
        log.info("Urun silindi: {}", id);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public String uploadImage(Long id, MultipartFile file) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String url = storageService.upload(file, fileName);
        product.setImageUrl(url);
        productRepository.save(product);
        return url;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public BulkImportResponse bulkImport(List<CreateProductRequest> requests) {
        List<String> failedItems = new ArrayList<>();
        int successCount = 0;

        for (CreateProductRequest request : requests) {
            try {
                Category category = categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

                Product product = new Product();
                product.setName(request.name());
                product.setDescription(request.description());
                product.setPrice(request.price());
                product.setStock(request.stock());
                product.setImageUrl(request.imageUrl());
                product.setCategory(category);

                productRepository.save(product);
                successCount++;
            } catch (Exception e) {
                failedItems.add(request.name() + ": " + e.getMessage());
            }
        }

        return new BulkImportResponse(successCount, failedItems.size(), requests.size(), failedItems);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void saveFromScrapedEvent(String name, String description, BigDecimal price, String imageUrl, Long categoryId) {
        if (productRepository.existsByName(name)) {
            log.info("Urun zaten mevcut, atlaniyor: {}", name);
            return;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(10);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setActive(true);

        Product saved = productRepository.save(product);
        log.info("AI urun kaydedildi: {}", name);
        publishProductAddedEvent(saved, category);
    }

    private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
        boolean hideStock = !stockAuthorizationService.canViewStock();
        return new PageResponse<>(
                page.getContent().stream()
                        .map(product -> ProductResponse.from(product, hideStock))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}