package com.eticaret.product.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Kategori bulunamadı: " + id);
    }
}