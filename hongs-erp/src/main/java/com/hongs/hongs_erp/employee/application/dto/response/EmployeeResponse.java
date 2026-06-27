package com.hongs.hongs_erp.employee.application.dto.response;

import com.hongs.hongs_erp.employee.domain.User;

public record EmployeeResponse(Long id, String email, String name, User.Role role, boolean locked) {

    public static EmployeeResponse from(User user) {
        return new EmployeeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.isLocked());
    }
}
