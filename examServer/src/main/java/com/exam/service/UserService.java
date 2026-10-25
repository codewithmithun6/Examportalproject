package com.exam.service;

import com.exam.model.User;
import com.exam.model.UserRole;

import java.security.Principal;
import java.util.Set;

public interface UserService {

    // creating user

    public User createUser(User user, Set<UserRole> userRoles) throws Exception;


    // get user by username
    public User getUser(String userName);

    // delete user by userid

    public void deleteUser(Long userId);



    User updateUser(User user);

    User getUserById(Long userId);

    public void changePassword(String username, String oldPassword, String newPassword);


}
