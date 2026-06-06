package com.springnexttodo.config;

import com.springnexttodo.auth.User;
import com.springnexttodo.auth.UserRepository;
import com.springnexttodo.category.Category;
import com.springnexttodo.category.CategoryRepository;
import com.springnexttodo.task.Priority;
import com.springnexttodo.task.Task;
import com.springnexttodo.task.TaskRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeedData implements ApplicationRunner {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(TaskRepository taskRepository, UserRepository userRepository,
                    CategoryRepository categoryRepository, PasswordEncoder passwordEncoder) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
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

        Category trabalho = new Category();
        trabalho.setName("Trabalho");
        trabalho.setColor("#3b82f6");
        trabalho.setUser(seed);
        categoryRepository.save(trabalho);

        Category estudo = new Category();
        estudo.setName("Estudo");
        estudo.setColor("#10b981");
        estudo.setUser(seed);
        categoryRepository.save(estudo);

        Category pessoal = new Category();
        pessoal.setName("Pessoal");
        pessoal.setColor("#f59e0b");
        pessoal.setUser(seed);
        categoryRepository.save(pessoal);

        Task t1 = new Task();
        t1.setTitle("Estudar Spring Boot");
        t1.setDescription("Entender camadas Controller → Service → Repository");
        t1.setUser(seed);
        t1.setCategory(estudo);
        t1.setDueDate(LocalDate.now().plusDays(5));
        t1.setPriority(Priority.HIGH);

        Task t2 = new Task();
        t2.setTitle("Configurar Next.js com shadcn/ui");
        t2.setUser(seed);
        t2.setCategory(trabalho);
        t2.setDueDate(LocalDate.now().minusDays(2));
        t2.setPriority(Priority.MEDIUM);

        Task t3 = new Task();
        t3.setTitle("Conectar front ao back via fetch");
        t3.setCompleted(true);
        t3.setUser(seed);
        t3.setPriority(Priority.LOW);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
    }
}
