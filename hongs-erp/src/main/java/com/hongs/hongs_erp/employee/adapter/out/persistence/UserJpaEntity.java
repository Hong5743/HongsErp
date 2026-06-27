package com.hongs.hongs_erp.employee.adapter.out.persistence;

import com.hongs.hongs_erp.employee.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.Role role;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    public UserJpaEntity(String email, String password, String name, User.Role role, boolean locked, int failCount) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.locked = locked;
        this.failCount = failCount;
    }

    public User toDomain() {
        return new User(id, email, password, name, role, locked, failCount);
    }

    static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail(), user.getPassword(), user.getName(),
                user.getRole(), user.isLocked(), user.getFailCount()
        );
        entity.id = user.getId();
        return entity;
    }

    Long getId() { return id; }
}
