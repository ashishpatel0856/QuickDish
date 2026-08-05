package com.ashish.QuickDish.utils;

import com.ashish.QuickDish.Entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {
    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }


        throw new RuntimeException("User not found in security context");
    }
}
