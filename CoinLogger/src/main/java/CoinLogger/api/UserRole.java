package CoinLogger.api;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("ROLE_USER"),
    DENIED("ROLE_DENIED"),
    ADMIN("ROLE_ADMIN");

    private String value;

    UserRole(String value) {
        this.value = value;
    }
}