package com.eticaret.user.domain;

import com.eticaret.user.dto.CreateUserRequest;
import com.eticaret.user.dto.UpdateUserRequest;
import com.eticaret.user.dto.UserResponse;
import com.eticaret.user.event.UserRegisteredEvent;
import com.eticaret.user.exception.UserAlreadyExistsException;
import com.eticaret.user.exception.UserNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredKey;

    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setKeycloakId(request.keycloakId());
        user.setRole(UserRole.ROLE_USER);

        User saved = userRepository.save(user);

        UserRegisteredEvent event = UserRegisteredEvent.of(
                saved.getId(), saved.getEmail(), saved.getFirstName()
        );
        try {
            rabbitTemplate.convertAndSend(exchange, userRegisteredKey, event);
        } catch (Exception e) {
            System.out.println("RabbitMQ event gonderilemedi: " + e.getMessage());
        }

        return UserResponse.from(saved);
    }


    @Transactional
    public UserResponse register(com.eticaret.user.dto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        // Keycloak admin token al
        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "admin-cli");
            body.add("username", "admin");
            body.add("password", "admin123");
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> tokenReq =
                    new org.springframework.http.HttpEntity<>(body, headers);
            java.util.Map tokenRes = rt.postForObject(
                    "http://localhost:8180/realms/master/protocol/openid-connect/token",
                    tokenReq, java.util.Map.class);
            String adminToken = (String) tokenRes.get("access_token");

            // Kullanici olustur
            org.springframework.http.HttpHeaders userHeaders = new org.springframework.http.HttpHeaders();
            userHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            userHeaders.setBearerAuth(adminToken);
            java.util.Map<String, Object> userBody = new java.util.HashMap<>();
            userBody.put("username", request.username());
            userBody.put("email", request.email());
            userBody.put("firstName", request.firstName());
            userBody.put("lastName", request.lastName());
            userBody.put("enabled", true);
            userBody.put("emailVerified", true);
            userBody.put("credentials", java.util.List.of(java.util.Map.of(
                    "type", "password", "value", request.password(), "temporary", false)));
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> userReq =
                    new org.springframework.http.HttpEntity<>(userBody, userHeaders);
            org.springframework.http.ResponseEntity<Void> userRes = rt.postForEntity(
                    "http://localhost:8180/admin/realms/eticaret/users",
                    userReq, Void.class);

            String location = userRes.getHeaders().getLocation().toString();
            String keycloakId = location.substring(location.lastIndexOf("/") + 1);

            User user = new User();
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmail(request.email());
            user.setKeycloakId(keycloakId);
            user.setRole(UserRole.ROLE_USER);
            User saved = userRepository.save(user);
            UserRegisteredEvent event = UserRegisteredEvent.of(saved.getId(), saved.getEmail(), saved.getFirstName());
            try {
                rabbitTemplate.convertAndSend(exchange, userRegisteredKey, event);
            } catch (Exception e) {
                System.out.println("RabbitMQ event gonderilemedi: " + e.getMessage());
            }
            return UserResponse.from(saved);
        } catch (Exception e) {
            throw new RuntimeException("Kayit basarisiz: " + e.getMessage());
        }
    }
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.from(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
    }
}