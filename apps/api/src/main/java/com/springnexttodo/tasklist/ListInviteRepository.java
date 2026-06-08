package com.springnexttodo.tasklist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListInviteRepository extends JpaRepository<ListInvite, Long> {
    Optional<ListInvite> findByToken(String token);
    void deleteByTaskList(TaskList taskList);
}
