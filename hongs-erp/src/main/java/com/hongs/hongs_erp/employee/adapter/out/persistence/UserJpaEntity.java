package com.hongs.hongs_erp.employee.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false)
    private String userName;

    public UserJpaEntity(final String id, final String password, final String userName) {
        this.id = id;
        this.password = password;
        this.userName = userName;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getUserName() { return userName; }
}
