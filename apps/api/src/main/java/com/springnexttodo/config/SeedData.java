package com.springnexttodo.config;

import com.springnexttodo.auth.User;
import com.springnexttodo.auth.UserRepository;
import com.springnexttodo.task.Task;
import com.springnexttodo.task.TaskRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements ApplicationRunner {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(TaskRepository taskRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        User seed = new User();
        seed.setName("Seed User");
        seed.setEmail("seed@todo.dev");
        seed.setPasswordHash(passwordEncoder.encode("seed123"));
        userRepository.save(seed);

        Task t1 = new Task();
        t1.setTitle("Estudar Spring Boot");
        t1.setDescription("Entender camadas Controller → Service → Repository");
        t1.setUser(seed);

        Task t2 = new Task();
        t2.setTitle("Configurar Next.js com shadcn/ui");
        t2.setUser(seed);

        Task t3 = new Task();
        t3.setTitle("Conectar front ao back via fetch");
        t3.setCompleted(true);
        t3.setUser(seed);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
    }
}
