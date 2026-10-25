package com.exam.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;


@Entity
public class UserRole {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userRoleId;

    // userRole has single user

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private User user;


    @ManyToOne
    @JsonIgnore
    private Role role;

    public UserRole() {
    }

    public Long getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(Long userRoleId) {
        this.userRoleId = userRoleId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
