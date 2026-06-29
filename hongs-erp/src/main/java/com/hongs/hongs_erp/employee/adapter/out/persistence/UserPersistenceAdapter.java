package com.hongs.hongs_erp.employee.adapter.out.persistence;

import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserJpaEntity.fromDomain(user)).toDomain();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void update(User user) {
        userJpaRepository.findById(user.getId())
                .ifPresent(entity -> entity.applyFrom(user));
    }

    @Override
    @Transactional
    public int incrementFailCountAndGet(Long userId) {
        userJpaRepository.incrementFailCount(userId);
        Integer count = userJpaRepository.findFailCountById(userId);
        return count != null ? count : 0;
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }
}
