package com.user.auth.utils;

import com.user.auth.entity.User;
import com.user.auth.exception.InvalidDataException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
