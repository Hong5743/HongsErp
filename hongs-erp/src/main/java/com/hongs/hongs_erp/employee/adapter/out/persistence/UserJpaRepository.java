package com.hongs.hongs_erp.employee.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.failCount = u.failCount + 1 WHERE u.id = :id")
    void incrementFailCount(@Param("id") Long id);

    @Query("SELECT u.failCount FROM UserJpaEntity u WHERE u.id = :id")
    int findFailCountById(@Param("id") Long id);
}
