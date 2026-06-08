package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.tasklist.dto.TaskListRequest;
import com.springnexttodo.tasklist.dto.TaskListResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskListService {

    private final TaskListRepository listRepository;
    private final TaskListMemberRepository memberRepository;
    private final ListAccessService accessService;

    public TaskListService(TaskListRepository listRepository,
                           TaskListMemberRepository memberRepository,
                           ListAccessService accessService) {
        this.listRepository = listRepository;
        this.memberRepository = memberRepository;
        this.accessService = accessService;
    }

    public List<TaskListResponse> findAll(User user) {
        return memberRepository.findByUser(user).stream()
                .map(m -> toResponse(m.getTaskList(), m.getRole()))
                .toList();
    }

    @Transactional
    public TaskListResponse create(TaskListRequest req, User user) {
        var list = new TaskList();
        list.setName(req.name());
        list.setOwner(user);
        listRepository.save(list);

        var membership = new TaskListMember();
        membership.setTaskList(list);
        membership.setUser(user);
        membership.setRole(ListRole.OWNER);
        memberRepository.save(membership);

        return toResponse(list, ListRole.OWNER);
    }

    @Transactional
    public TaskListResponse rename(Long id, TaskListRequest req, User user) {
        TaskList list = getOrThrow(id);
        accessService.requireRole(list, user, ListRole.OWNER);
        list.setName(req.name());
        listRepository.save(list);
        return toResponse(list, ListRole.OWNER);
    }

    @Transactional
    public void delete(Long id, User user) {
        TaskList list = getOrThrow(id);
        accessService.requireRole(list, user, ListRole.OWNER);
        listRepository.delete(list);
    }

    TaskList getOrThrow(Long id) {
        return listRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task list not found: " + id));
    }

    private TaskListResponse toResponse(TaskList list, ListRole role) {
        int memberCount = memberRepository.findByTaskList(list).size();
        return TaskListResponse.from(list, role, memberCount);
    }
}
