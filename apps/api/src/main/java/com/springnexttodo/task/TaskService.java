package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.CategoryService;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
import com.springnexttodo.tasklist.ListAccessService;
import com.springnexttodo.tasklist.ListRole;
import com.springnexttodo.tasklist.TaskList;
import com.springnexttodo.tasklist.TaskListRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;
    private final CategoryService categoryService;
    private final TaskListRepository listRepository;
    private final ListAccessService accessService;

    public TaskService(TaskRepository repository,
                       CategoryService categoryService,
                       TaskListRepository listRepository,
                       ListAccessService accessService) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.listRepository = listRepository;
        this.accessService = accessService;
    }

    public List<TaskResponse> findAll(User user, Long listId, Long categoryId, Priority priority, Boolean completed, String q) {
        String query = (q != null && !q.isBlank()) ? escapeLike(q.trim()) : null;
        return repository.findFiltered(user, listId, categoryId, priority, completed, query)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    private static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    public TaskResponse findById(Long id, User user) {
        return TaskResponse.from(getOrThrow(id, user, ListRole.VIEWER));
    }

    @Transactional
    public TaskResponse create(TaskRequest req, User user) {
        TaskList list = resolveList(req.listId(), user);
        accessService.requireRole(list, user, ListRole.EDITOR);

        var task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setUser(user);
        task.setTaskList(list);
        if (req.categoryId() != null) {
            task.setCategory(categoryService.getEntityById(req.categoryId(), user));
        }
        task.setDueDate(req.dueDate());
        task.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req, User user) {
        var task = getOrThrow(id, user, ListRole.EDITOR);
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setCategory(req.categoryId() != null
                ? categoryService.getEntityById(req.categoryId(), user)
                : null);
        task.setDueDate(req.dueDate());
        task.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse toggle(Long id, User user) {
        var task = getOrThrow(id, user, ListRole.EDITOR);
        task.setCompleted(!task.isCompleted());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public void delete(Long id, User user) {
        var task = getOrThrow(id, user, ListRole.EDITOR);
        repository.deleteById(task.getId());
    }

    private Task getOrThrow(Long id, User user, ListRole minRole) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
        accessService.requireRole(task.getTaskList(), user, minRole);
        return task;
    }

    private TaskList resolveList(Long listId, User user) {
        if (listId != null) {
            return listRepository.findById(listId)
                    .orElseThrow(() -> new EntityNotFoundException("Task list not found: " + listId));
        }
        return listRepository.findByOwnerAndIsDefault(user, true)
                .orElseThrow(() -> new EntityNotFoundException("Default list not found for user"));
    }
}
