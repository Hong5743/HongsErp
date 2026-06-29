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

    @Version
    @Column(nullable = false)
    private Long version;

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

    @Column(name = "department")
    private String department;

    public UserJpaEntity(String email, String password, String name, User.Role role, boolean locked, int failCount, String department) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.locked = locked;
        this.failCount = failCount;
        this.department = department;
    }

    public User toDomain() {
        return new User(id, email, password, name, role, locked, failCount, department);
    }

    static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail(), user.getPassword(), user.getName(),
                user.getRole(), user.isLocked(), user.getFailCount(), user.getDepartment()
        );
        entity.id = user.getId();
        return entity;
    }

    void applyFrom(User user) {
        this.name = user.getName();
        this.role = user.getRole();
        this.locked = user.isLocked();
        this.failCount = user.getFailCount();
        this.department = user.getDepartment();
        this.password = user.getPassword();
    }

    Long getId() { return id; }
}
