package com.hongs.hongs_erp.employee.application.dto.response;

import com.hongs.hongs_erp.employee.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeResponse(Long id, String email, String name, User.Role role, boolean locked, String department) {

    public static EmployeeResponse from(User user) {
        return new EmployeeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.isLocked(), user.getDepartment());
    }
}
