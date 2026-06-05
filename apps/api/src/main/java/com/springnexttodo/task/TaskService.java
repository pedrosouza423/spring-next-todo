package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<TaskResponse> findAll(User user) {
        return repository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id, User user) {
        return TaskResponse.from(getOrThrow(id, user));
    }

    @Transactional
    public TaskResponse create(TaskRequest req, User user) {
        var task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setUser(user);
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req, User user) {
        var task = getOrThrow(id, user);
        task.setTitle(req.title());
        task.setDescription(req.description());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse toggle(Long id, User user) {
        var task = getOrThrow(id, user);
        task.setCompleted(!task.isCompleted());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public void delete(Long id, User user) {
        var task = getOrThrow(id, user);
        repository.deleteById(task.getId());
    }

    private Task getOrThrow(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }
}
