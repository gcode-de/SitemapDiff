package de.samuelgesang.backend.auth.domain;

public record GoogleUserProfile(
        String id,
        String email,
        String picture,
        String name
) {
}
