package com.springnexttodo.tasklist;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.common.BaseController;
import com.springnexttodo.tasklist.dto.InviteRequest;
import com.springnexttodo.tasklist.dto.InviteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class InviteController extends BaseController {

    private final InviteService inviteService;

    public InviteController(AuthService authService, InviteService inviteService) {
        super(authService);
        this.inviteService = inviteService;
    }

    @PostMapping("/lists/{id}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@PathVariable Long id,
                                       @Valid @RequestBody InviteRequest req,
                                       Authentication auth) {
        return inviteService.createInvite(id, req, currentUser(auth));
    }

    @GetMapping("/invites/{token}")
    public InviteResponse previewInvite(@PathVariable String token) {
        return inviteService.previewInvite(token);
    }

    @PostMapping("/invites/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvite(@PathVariable String token, Authentication auth) {
        inviteService.acceptInvite(token, currentUser(auth));
    }
}
