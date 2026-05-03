package com.eticaret.user.domain;

import com.eticaret.common.ApiResponse;
import com.eticaret.user.dto.CreateUserRequest;
import com.eticaret.user.dto.RegisterRequest;
import com.eticaret.user.dto.UpdateUserRequest;
import com.eticaret.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Kullanıcı yönetimi")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Yeni kullanıcı oluştur")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kullanıcı oluşturuldu", userService.createUser(request)));
    }

    @PostMapping("/register")
    @Operation(summary = "Keycloak ile kullanici kaydet")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kullanici olusturuldu", userService.register(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile kullanıcı getir")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping
    @Operation(summary = "Tüm kullanıcıları listele")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kullanıcı güncelle")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Kullanıcı güncellendi",
                userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kullanıcı sil (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Kullanıcı silindi", null));
    }
}