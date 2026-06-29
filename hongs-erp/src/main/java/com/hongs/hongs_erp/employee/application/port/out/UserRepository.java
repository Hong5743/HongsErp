package com.hongs.hongs_erp.employee.application.port.out;

import com.hongs.hongs_erp.employee.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    void update(User user);
    int incrementFailCountAndGet(Long userId);
}
