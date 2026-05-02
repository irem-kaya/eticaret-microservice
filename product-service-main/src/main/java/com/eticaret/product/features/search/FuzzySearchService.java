package com.eticaret.product.features.search;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fuzzy Search Service - Backend implementasyonu
 * KullanıcıKaynağa%75+ eşleşme bulur
 */
@Service
public class FuzzySearchService {

    private static final int SIMILARITY_THRESHOLD = 75; // %75

    /**
     * Ürün adında fuzzy search
     */
    public int calculateSimilarity(String query, String text) {
        if (query == null || text == null) return 0;

        // Normal LIKE query'ye yüksek skor ver
        if (text.toLowerCase().contains(query.toLowerCase())) {
            return 100;
        }

        // Fuzzy match
        int tokenSetRatio = FuzzySearch.tokenSetRatio(query, text);
        int tokenSortRatio = FuzzySearch.tokenSortRatio(query, text);
        int ratio = FuzzySearch.ratio(query, text);

        // En yüksek skoru döndür
        return Math.max(ratio, Math.max(tokenSetRatio, tokenSortRatio));
    }

    /**
     * Eşik değerini aş mı kontrol et
     */
    public boolean isSimilarEnough(String query, String text) {
        return calculateSimilarity(query, text) >= SIMILARITY_THRESHOLD;
    }

    /**
     * Detaylı fuzzy search (tüm sonuçları dön)
     */
    public ExtractedResult extractBest(String query, List<String> choices) {
        return FuzzySearch.extractOne(query, choices);
    }
}

