package com.springnexttodo.common;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.auth.User;
import org.springframework.security.core.Authentication;

public abstract class BaseController {

    protected final AuthService authService;

    protected BaseController(AuthService authService) {
        this.authService = authService;
    }

    protected User currentUser(Authentication auth) {
        return authService.getUser(auth.getName());
    }
}
