package com.auca.library.domain;

import com.auca.library.domain.enums.RoleType;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends Person {

    @Column(name = "user_name", unique = true)
    private String userName;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleType role;

    @ManyToOne
    @JoinColumn(name = "village_id")
    private Location location;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
}
