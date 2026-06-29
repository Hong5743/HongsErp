package com.hongs.hongs_erp.employee.application.port.in;

import com.hongs.hongs_erp.employee.application.dto.response.EmployeeResponse;

import java.util.List;

public interface ListEmployeesUseCase {
    List<EmployeeResponse> listEmployees();
}
