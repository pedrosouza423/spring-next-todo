package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    Optional<TaskList> findByIdAndOwner(Long id, User owner);

    @Query("""
        SELECT DISTINCT m.taskList FROM TaskListMember m
        WHERE m.user = :user
        ORDER BY m.taskList.createdAt ASC
    """)
    List<TaskList> findAllAccessibleByUser(@Param("user") User user);

    Optional<TaskList> findByOwnerAndNameIgnoreCase(User owner, String name);
}
