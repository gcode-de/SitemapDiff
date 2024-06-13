package de.samuelgesang.backend.auth.domain;

public record GitHubUserProfile(
        String id,
        String login,
        String avatar_url,
        String name
) {
}
