package com.hongs.hongs_erp.employee.application.port.in;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.SignupResponse;

public interface SignupUseCase {
    SignupResponse signup(SignupCommand command);
}
