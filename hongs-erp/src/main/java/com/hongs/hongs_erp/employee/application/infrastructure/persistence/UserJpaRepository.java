package com.hongs.hongs_erp.employee.application.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {
}
