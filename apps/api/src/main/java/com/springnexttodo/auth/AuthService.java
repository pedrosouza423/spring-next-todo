package com.springnexttodo.auth;

import com.springnexttodo.auth.dto.AuthResponse;
import com.springnexttodo.auth.dto.RegisterRequest;
import com.springnexttodo.tasklist.ListRole;
import com.springnexttodo.tasklist.TaskList;
import com.springnexttodo.tasklist.TaskListMember;
import com.springnexttodo.tasklist.TaskListMemberRepository;
import com.springnexttodo.tasklist.TaskListRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskListRepository taskListRepository;
    private final TaskListMemberRepository taskListMemberRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TaskListRepository taskListRepository,
                       TaskListMemberRepository taskListMemberRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskListRepository = taskListRepository;
        this.taskListMemberRepository = taskListMemberRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyUsedException(req.email());
        }
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(user);

        TaskList defaultList = new TaskList();
        defaultList.setName("Minhas Tarefas");
        defaultList.setOwner(user);
        defaultList.setDefault(true);
        taskListRepository.save(defaultList);

        TaskListMember membership = new TaskListMember();
        membership.setTaskList(defaultList);
        membership.setUser(user);
        membership.setRole(ListRole.OWNER);
        taskListMemberRepository.save(membership);

        return AuthResponse.from(user);
    }

    public AuthResponse findByEmail(String email) {
        return AuthResponse.from(getUser(email));
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + email));
    }
}
