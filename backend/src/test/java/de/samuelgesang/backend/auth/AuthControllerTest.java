package de.samuelgesang.backend.auth;

import de.samuelgesang.backend.auth.domain.GoogleUserProfile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void testGetMe() {
        // Arrange
        OAuth2User mockUser = Mockito.mock(OAuth2User.class);
        Map<String, Object> attributes = Map.of(
                "sub", "12345",
                "email", "test@example.com",
                "picture", "http://example.com/picture.jpg",
                "given_name", "Test"
        );

        when(mockUser.getAttributes()).thenReturn(attributes);

        AuthController authController = new AuthController();

        // Act
        GoogleUserProfile profile = authController.getMe(mockUser);

        // Assert
        assertEquals("12345", profile.id());
        assertEquals("test@example.com", profile.email());
        assertEquals("http://example.com/picture.jpg", profile.picture());
        assertEquals("Test", profile.name());
    }
}
