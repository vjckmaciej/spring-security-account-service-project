package account.utils;

import account.config.Role;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class RoleUtils {
    private static final Set<Role> ADMIN_GROUP = Set.of(Role.ROLE_ADMINISTRATOR);
    private static final Set<Role> BUSINESS_GROUP = Set.of(Role.ROLE_USER, Role.ROLE_ACCOUNTANT, Role.ROLE_AUDITOR);

    public static boolean isAdminGroup(Role role) {
        return ADMIN_GROUP.contains(role);
    }

    public static boolean isBusinessGroup(Role role) {
        return BUSINESS_GROUP.contains(role);
    }

    public static boolean isAdminGroup(Set<Role> roles) {
        return roles.stream().anyMatch(ADMIN_GROUP::contains);
    }

    public static boolean isBusinessGroup(Set<Role> roles) {
        return roles.stream().anyMatch(BUSINESS_GROUP::contains);
    }

    public static boolean isMixGroup(Role role) {
        return isAdminGroup(role) || isBusinessGroup(role);
    }

    // maps string -> Role (accepts "USER" and "ROLE_USER")
    public static Optional<Role> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        try {
            return Optional.of(Role.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
