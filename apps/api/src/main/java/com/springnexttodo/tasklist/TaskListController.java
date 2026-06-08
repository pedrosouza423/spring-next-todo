package com.springnexttodo.tasklist;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.common.BaseController;
import com.springnexttodo.tasklist.dto.TaskListRequest;
import com.springnexttodo.tasklist.dto.TaskListResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class TaskListController extends BaseController {

    private final TaskListService taskListService;

    public TaskListController(AuthService authService, TaskListService taskListService) {
        super(authService);
        this.taskListService = taskListService;
    }

    @GetMapping
    public List<TaskListResponse> list(Authentication auth) {
        return taskListService.findAll(currentUser(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskListResponse create(@Valid @RequestBody TaskListRequest req, Authentication auth) {
        return taskListService.create(req, currentUser(auth));
    }

    @PutMapping("/{id}")
    public TaskListResponse rename(@PathVariable Long id,
                                   @Valid @RequestBody TaskListRequest req,
                                   Authentication auth) {
        return taskListService.rename(id, req, currentUser(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        taskListService.delete(id, currentUser(auth));
    }
}
