package com.springnexttodo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seed data placeholder. Tasks are now user-scoped and require an authenticated
 * user to be created — pre-seeding without a real user is no longer applicable.
 */
@Component
public class SeedData implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // No-op: tasks belong to users; seed data is inserted via the auth/task API.
    }
}
