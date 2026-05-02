package com.eticaret.product.features.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Stock Authorization Service - Stok görünürlüğünü kontrol et
 */
@Service
public class StockAuthorizationService {

    /**
     * Stok bilgisi gösterilmeli mi? (Admin/Seller için göster)
     */
    public boolean canViewStock() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return false; // Kimlik doğrulanmamış kullanıcılar stok göremez
        }

        // ROLE_ADMIN veya ROLE_SELLER için stok göster
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuth ->
                        grantedAuth.getAuthority().equals("ROLE_ADMIN") ||
                        grantedAuth.getAuthority().equals("ROLE_SELLER")
                );
    }
}

