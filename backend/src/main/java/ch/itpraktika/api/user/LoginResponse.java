package ch.itpraktika.api.user;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String email,
        String role
) {
}
