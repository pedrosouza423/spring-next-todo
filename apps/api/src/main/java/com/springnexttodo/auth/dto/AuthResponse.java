package com.springnexttodo.auth.dto;

import com.springnexttodo.auth.User;

public record AuthResponse(Long id, String name, String email) {

    public static AuthResponse from(User user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail());
    }
}
