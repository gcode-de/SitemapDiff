package de.samuelgesang.backend.auth;

import de.samuelgesang.backend.auth.domain.GoogleUserProfile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public GoogleUserProfile getMe(@AuthenticationPrincipal OAuth2User user) {
        Map<String, Object> returnValue = user.getAttributes();

        return new GoogleUserProfile(
                returnValue.get("sub").toString(),
                returnValue.get("email").toString(),
                returnValue.get("picture").toString(),
                returnValue.get("given_name").toString()
        );
    }
}
