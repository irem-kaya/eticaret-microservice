package com.eticaret.product.dto;

import java.util.List;

/**
 * Toplu ürün importu yanıtı
 */
public record BulkImportResponse(
        int successCount,
        int failureCount,
        int totalCount,
        List<String> failedItems
) {}

