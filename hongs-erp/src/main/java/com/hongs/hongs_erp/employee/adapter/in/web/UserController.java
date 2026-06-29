package com.hongs.hongs_erp.employee.adapter.in.web;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.SignupResponse;
import com.hongs.hongs_erp.employee.application.port.in.SignupUseCase;
import com.hongs.hongs_erp.employee.domain.User;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SignupUseCase signupUseCase;

    public UserController(SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping
    public SignupResponse signup(@RequestBody @Valid SignupCommand signupCommand) {
        return signupUseCase.signup(signupCommand);
    }
}
