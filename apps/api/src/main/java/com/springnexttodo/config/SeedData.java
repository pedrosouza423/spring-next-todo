package com.springnexttodo.config;

import com.springnexttodo.task.Task;
import com.springnexttodo.task.TaskRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements ApplicationRunner {

    private final TaskRepository repository;

    public SeedData(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;

        var t1 = new Task();
        t1.setTitle("Estudar Spring Boot");
        t1.setDescription("Entender camadas Controller → Service → Repository");

        var t2 = new Task();
        t2.setTitle("Configurar Next.js com shadcn/ui");

        var t3 = new Task();
        t3.setTitle("Conectar front ao back via fetch");
        t3.setCompleted(true);

        repository.save(t1);
        repository.save(t2);
        repository.save(t3);
    }
}