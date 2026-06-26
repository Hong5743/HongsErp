package com.hongs.hongs_erp.employee.application.port.out;

import com.hongs.hongs_erp.employee.domain.User;

public interface UserRepository {
    User save(User user);
}
