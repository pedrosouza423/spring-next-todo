package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "task_list_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_list_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class TaskListMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_list_id", nullable = false)
    private TaskList taskList;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListRole role;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public TaskList getTaskList() { return taskList; }
    public void setTaskList(TaskList taskList) { this.taskList = taskList; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ListRole getRole() { return role; }
    public void setRole(ListRole role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
}
