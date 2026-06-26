package com.hongs.hongs_erp.employee.application.port.output;

import com.hongs.hongs_erp.employee.domain.User;

public interface UserRepository {
    User save(User user);   
}
