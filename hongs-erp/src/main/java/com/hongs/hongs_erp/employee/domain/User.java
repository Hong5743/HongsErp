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
    private String department;

    public enum Role {
        ADMIN, EMPLOYEE, EDITOR
    }

    public User(Long id, String email, String password, String name, Role role, boolean locked, int failCount, String department) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.locked = locked;
        this.failCount = failCount;
        this.department = department;
    }

    public static User create(String email, String encodedPassword, String name, Role role) {
        return new User(null, email, encodedPassword, name, role, false, 0, null);
    }

    public static User create(String email, String encodedPassword, String name, Role role, String department) {
        return new User(null, email, encodedPassword, name, role, false, 0, department);
    }

    public User incrementFailCount() {
        return new User(id, email, password, name, role, locked, failCount + 1, department);
    }

    public User lock() {
        return new User(id, email, password, name, role, true, failCount, department);
    }

    public User unlock() {
        return new User(id, email, password, name, role, false, 0, department);
    }

    public User resetFailCount() {
        return new User(id, email, password, name, role, locked, 0, department);
    }
}
