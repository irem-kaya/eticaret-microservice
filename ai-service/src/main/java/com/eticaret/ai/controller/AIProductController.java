// AI Service - src/main/java/com/eticaret/ai/controller/AIProductController.java
package com.eticaret.ai.controller;

import com.eticaret.ai.dto.RawProductRequest;
import com.eticaret.ai.service.AIProductProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIProductController {

    private final AIProductProcessor processor;


    public AIProductController(AIProductProcessor processor) {
        this.processor = processor;
    }

    @PostMapping("/process-scraped")
    public ResponseEntity<String> processScrapedProducts(@RequestBody List<RawProductRequest> requests) {
        // İşlemi asenkron başlatıyoruz ki client hemen 202 Accepted yanıtı alabilsin
        processor.processAndSync(requests);
        return ResponseEntity.accepted().body("Ürünler işleme kuyruğuna alındı. AI zenginleştirme başlıyor...");
    }
}