package com.eticaret.user.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakAdminService {

    @Value("${app.keycloak.server-url}")
    private String serverUrl;

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Value("${app.keycloak.username}")
    private String username;

    @Value("${app.keycloak.password}")
    private String password;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }

    public String createUser(String firstName, String lastName,
                             String username, String email, String rawPassword) {
        Keycloak keycloak = getKeycloakInstance();

        // Şifre ayarla
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(rawPassword);
        credential.setTemporary(false);

        // Kullanıcı oluştur
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() == 201) {
            // Keycloak'taki kullanıcının ID'sini al
            String location = response.getHeaderString("Location");
            return location.substring(location.lastIndexOf("/") + 1);
        } else if (response.getStatus() == 409) {
            throw new RuntimeException("Bu kullanici zaten Keycloak'ta mevcut");
        } else {
            throw new RuntimeException("Keycloak kullanici olusturulamadi: " + response.getStatus());
        }
    }
}