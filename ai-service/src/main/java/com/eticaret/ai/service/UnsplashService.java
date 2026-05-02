package com.eticaret.ai.service;


import org.springframework.stereotype.Service;

@Service
public class UnsplashService {

    public String searchImage(String query) {
        // Şimdilik placeholder dönelim, derleme hatası çözülsün.
        return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=1000&auto=format&fit=crop";
    }
}