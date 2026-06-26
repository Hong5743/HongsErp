package com.hongs.hongs_erp.employee.domain;

import lombok.Getter;

@Getter
public class User {
    private String id;
    private String password;
    private String userName;

    public User(final String id, final String password, final String userName) {
        this.id = id;
        this.password = password;
        this.userName = userName;
    }
}
