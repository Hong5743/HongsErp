package com.hongs.hongs_erp.employee.domain;

import lombok.Getter;

@Getter
public class User {

    private Long id;
    private String email;
    private String password;
    private String name;
    private Role role;
    private boolean locked;
    private int failCount;

    public enum Role {
        ADMIN, EMPLOYEE
    }

    public User(Long id, String email, String password, String name, Role role, boolean locked, int failCount) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.locked = locked;
        this.failCount = failCount;
    }

    public static User create(String email, String encodedPassword, String name, Role role) {
        return new User(null, email, encodedPassword, name, role, false, 0);
    }

    public User incrementFailCount() {
        return new User(id, email, password, name, role, locked, failCount + 1);
    }

    public User lock() {
        return new User(id, email, password, name, role, true, failCount);
    }

    public User unlock() {
        return new User(id, email, password, name, role, false, 0);
    }

    public User resetFailCount() {
        return new User(id, email, password, name, role, locked, 0);
    }
}
