package com.hongs.hongs_erp.employee.application.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.hongs.hongs_erp.employee.application.port.output.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(final UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(final User user) {
        final UserJpaEntity entity = new UserJpaEntity(user.getId(), user.getPassword(), user.getUserName());
        final UserJpaEntity saved = userJpaRepository.save(entity);
        return new User(saved.getId(), saved.getPassword(), saved.getUserName());
    }
}
