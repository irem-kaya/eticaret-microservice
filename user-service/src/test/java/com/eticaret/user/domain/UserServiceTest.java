package com.eticaret.user.domain;

import com.eticaret.user.dto.*;
import com.eticaret.user.event.UserRegisteredEvent;
import com.eticaret.user.exception.UserAlreadyExistsException;
import com.eticaret.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(userService, "userRegisteredKey", "user.registered");
    }

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Test");
        user.setLastName("Kullanıcı");
        user.setEmail(email);
        user.setKeycloakId("keycloak-" + id);
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);
        return user;
    }

    private CreateUserRequest buildCreateRequest(String email) {
        return new CreateUserRequest("Test", "Kullanıcı", email, "keycloak-123");
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("yeni email → kullanıcı kaydedilir ve event yayınlanır")
        void createUser_newEmail_savesAndPublishesEvent() {
            CreateUserRequest req = buildCreateRequest("test@example.com");
            User saved = buildUser(1L, "test@example.com");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(saved);

            UserResponse resp = userService.createUser(req);

            assertThat(resp.email()).isEqualTo("test@example.com");
            verify(userRepository).save(any(User.class));
            verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("user.registered"), any(UserRegisteredEvent.class));
        }

        @Test
        @DisplayName("mevcut email → UserAlreadyExistsException")
        void createUser_existingEmail_throws() {
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(buildCreateRequest("existing@example.com")))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        @DisplayName("RabbitMQ hatası → exception fırlatılmaz")
        void createUser_rabbitFails_noExceptionPropagated() {
            CreateUserRequest req = buildCreateRequest("test@example.com");
            User saved = buildUser(1L, "test@example.com");

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(saved);
            doThrow(new RuntimeException("rabbit down"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserRegisteredEvent.class));

            assertThatNoException().isThrownBy(() -> userService.createUser(req));
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("mevcut id → UserResponse döner")
        void getUserById_exists_returnsResponse() {
            User user = buildUser(1L, "test@example.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponse resp = userService.getUserById(1L);

            assertThat(resp.id()).isEqualTo(1L);
            assertThat(resp.email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("bilinmeyen id → UserNotFoundException")
        void getUserById_notFound_throws() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserByEmail()")
    class GetUserByEmail {

        @Test
        @DisplayName("mevcut email → UserResponse döner")
        void getUserByEmail_exists_returnsResponse() {
            User user = buildUser(1L, "test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            UserResponse resp = userService.getUserByEmail("test@example.com");

            assertThat(resp.email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("bilinmeyen email → UserNotFoundException")
        void getUserByEmail_notFound_throws() {
            when(userRepository.findByEmail("yok@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByEmail("yok@example.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("geçerli id → kullanıcı güncellenir")
        void updateUser_validId_updatesUser() {
            User user = buildUser(1L, "test@example.com");
            UpdateUserRequest req = new UpdateUserRequest("Yeni", "İsim");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);

            UserResponse resp = userService.updateUser(1L, req);

            assertThat(resp).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("bilinmeyen id → UserNotFoundException")
        void updateUser_notFound_throws() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, new UpdateUserRequest("A", "B")))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("geçerli id → kullanıcı pasife alınır")
        void deleteUser_validId_setsInactive() {
            User user = buildUser(1L, "test@example.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);

            userService.deleteUser(1L);

            assertThat(user.isActive()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("bilinmeyen id → UserNotFoundException")
        void deleteUser_notFound_throws() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("kullanıcılar var → liste döner")
        void getAllUsers_returnsAll() {
            when(userRepository.findAll()).thenReturn(List.of(
                    buildUser(1L, "a@example.com"),
                    buildUser(2L, "b@example.com")
            ));

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("kullanıcı yok → boş liste")
        void getAllUsers_empty_returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.getAllUsers()).isEmpty();
        }
    }
}