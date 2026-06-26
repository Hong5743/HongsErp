package com.hongs.hongs_erp.employee.application.infrastructure.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.SignupResponse;
import com.hongs.hongs_erp.employee.application.port.input.SignupUseCase;

@RestController
@RequestMapping("/users")
public class UserController {
    private final SignupUseCase signupUseCase;

    public UserController(final SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping
    public SignupResponse signup(@RequestBody final SignupCommand signupCommand) {
        return signupUseCase.signup(signupCommand);
    }
}
