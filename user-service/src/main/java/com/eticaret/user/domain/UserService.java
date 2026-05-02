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
        rabbitTemplate.convertAndSend(exchange, userRegisteredKey, event);

        return UserResponse.from(saved);
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