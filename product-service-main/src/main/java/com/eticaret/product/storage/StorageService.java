package com.eticaret.product.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile file, String fileName);
    void delete(String fileName);
    String getUrl(String fileName);
}