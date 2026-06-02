package com.springnexttodo.task;

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

    public List<TaskResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id) {
        return TaskResponse.from(getOrThrow(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest req) {
        var task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req) {
        var task = getOrThrow(id);
        task.setTitle(req.title());
        task.setDescription(req.description());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse toggle(Long id) {
        var task = getOrThrow(id);
        task.setCompleted(!task.isCompleted());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Task not found: " + id);
        repository.deleteById(id);
    }

    private Task getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }
}