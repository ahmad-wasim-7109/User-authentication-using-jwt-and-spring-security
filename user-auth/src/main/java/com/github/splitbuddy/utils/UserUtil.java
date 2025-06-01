package com.github.splitbuddy.utils;

import com.github.splitbuddy.entity.User;
import com.github.splitbuddy.exception.InvalidDataException;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {

    public static User getCurrentUser() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        throw new InvalidDataException("No authenticated user found");
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getUsername();
    }
}
