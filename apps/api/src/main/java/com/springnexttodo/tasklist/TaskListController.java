package com.springnexttodo.tasklist;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.common.BaseController;
import com.springnexttodo.tasklist.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class TaskListController extends BaseController {

    private final TaskListService taskListService;
    private final MemberService memberService;

    public TaskListController(AuthService authService,
                              TaskListService taskListService,
                              MemberService memberService) {
        super(authService);
        this.taskListService = taskListService;
        this.memberService = memberService;
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

    @GetMapping("/{id}/members")
    public List<MemberResponse> listMembers(@PathVariable Long id, Authentication auth) {
        return memberService.listMembers(id, currentUser(auth));
    }

    @PostMapping("/{id}/members")
    public MemberResponse addMember(@PathVariable Long id,
                                    @Valid @RequestBody MemberRequest req,
                                    Authentication auth) {
        return memberService.addMember(id, req, currentUser(auth));
    }

    @PatchMapping("/{id}/members/{userId}")
    public MemberResponse updateRole(@PathVariable Long id,
                                     @PathVariable Long userId,
                                     @Valid @RequestBody RoleUpdateRequest req,
                                     Authentication auth) {
        return memberService.updateRole(id, userId, req, currentUser(auth));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long id,
                             @PathVariable Long userId,
                             Authentication auth) {
        memberService.removeMember(id, userId, currentUser(auth));
    }
}
