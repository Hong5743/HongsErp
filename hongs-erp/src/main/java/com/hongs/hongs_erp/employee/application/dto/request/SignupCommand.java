package com.hongs.hongs_erp.employee.application.dto.request;

import com.hongs.hongs_erp.employee.domain.User;

public record SignupCommand(String email, String password, String name, User.Role role) {}
