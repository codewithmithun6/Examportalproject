package com.exam.service.impl;

import com.exam.helper.UserFoundException;
import com.exam.model.User;
import com.exam.model.UserRole;
import com.exam.repository.RoleRepository;
import com.exam.repository.UserRepository;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // creating user
    @Override
    public User createUser(User user, Set<UserRole> userRoles) throws Exception {
        User local = this.userRepository.findByUserName(user.getUserName());
        if(local!=null){
            System.out.println("User is already there!! ");
            throw new UserFoundException();
        }else{
            // user create
            for (UserRole ur: userRoles){
                roleRepository.save(ur.getRole());
            }
            user.getUserRole().addAll(userRoles);
            local = this.userRepository.save(user);
        }
        return local;
    }

    @Override
    public User getUser(String userName) {
        return this.userRepository.findByUserName(userName);
    }

    @Override
    public void deleteUser(Long userId) {
        this.userRepository.deleteById(userId);
    }


    @Override
    public User updateUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return this.userRepository.findById(userId).get();
    }

    // change password method
    @Override
    public void changePassword(String username, String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword == null || oldPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("All password fields are required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from the old password");
        }

        User user = userRepository.findByUserName(username);
        if (user == null || !bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }


}



