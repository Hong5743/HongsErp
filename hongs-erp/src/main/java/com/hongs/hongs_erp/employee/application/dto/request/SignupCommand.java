package com.hongs.hongs_erp.employee.application.dto.request;

import com.hongs.hongs_erp.employee.domain.User;
import jakarta.validation.constraints.NotNull;

public record SignupCommand(String email, String password, String name, @NotNull User.Role role, String department) {}
