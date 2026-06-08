package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskListMemberRepository extends JpaRepository<TaskListMember, Long> {
    Optional<TaskListMember> findByTaskListAndUser(TaskList taskList, User user);
    List<TaskListMember> findByTaskList(TaskList taskList);
    List<TaskListMember> findByUser(User user);
    boolean existsByTaskListAndUser(TaskList taskList, User user);
    void deleteByTaskList(TaskList taskList);
}
