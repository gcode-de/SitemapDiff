package de.samuelgesang.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2User mockUser;

    @Test
    void testGetMe() throws Exception {
        // Arrange
        Map<String, Object> attributes = Map.of(
                "sub", "12345",
                "email", "test@example.com",
                "picture", "http://example.com/picture.jpg",
                "given_name", "Test"
        );

        when(mockUser.getAttributes()).thenReturn(attributes);

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(mockUser, null, "google");

        // Set the security context with the authentication token
        SecurityContext securityContext = new SecurityContextImpl(authToken);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.picture").value("http://example.com/picture.jpg"))
                .andExpect(jsonPath("$.name").value("Test"));
    }
}
