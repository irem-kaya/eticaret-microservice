package com.eticaret.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Kullanıcı bulunamadı: " + id);
    }
    public UserNotFoundException(String email) {
        super("Kullanıcı bulunamadı: " + email);
    }
}