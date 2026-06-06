package com.springnexttodo.task;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.common.BaseController;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController extends BaseController {

    private final TaskService service;

    public TaskController(TaskService service, AuthService authService) {
        super(authService);
        this.service = service;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) Long categoryId, Authentication auth) {
        return service.findAll(currentUser(auth), categoryId);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, Authentication auth) {
        return service.findById(id, currentUser(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest req, Authentication auth) {
        return service.create(req, currentUser(auth));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest req, Authentication auth) {
        return service.update(id, req, currentUser(auth));
    }

    @PatchMapping("/{id}/toggle")
    public TaskResponse toggle(@PathVariable Long id, Authentication auth) {
        return service.toggle(id, currentUser(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, currentUser(auth));
    }
}
