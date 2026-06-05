package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(User user);
    Optional<Task> findByIdAndUser(Long id, User user);
}
