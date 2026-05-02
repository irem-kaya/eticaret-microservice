package com.eticaret.product.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            return UPLOAD_DIR + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Dosya yüklenemedi: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Dosya silinemedi");
        }
    }

    @Override
    public String getUrl(String fileName) {
        return "http://localhost:8082/" + fileName;
    }
}