package com.exam.controller;


import com.exam.helper.UserFoundException;
import com.exam.model.Role;
import com.exam.model.User;
import com.exam.model.UserRole;
import com.exam.passwordhandler.ChangePasswordRequest;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/home")
    public String home(){
        return "Welcome to exam portal";
    }

    // creating user

    @PostMapping("/")
    public User createUser(@RequestBody User user) throws Exception {

        user.setProfile("default.png");

        // encoding password with bCryptPasswordEncoder
        user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));

        Set<UserRole> roles = new HashSet<>();

        Role role = new Role();
        role.setRoleId(45L);
        role.setRoleName("NORMAL");

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        roles.add(userRole);

        return this.userService.createUser(user,roles);


    }

    // get user by username

    @GetMapping("/{userName}")
    public User getUser(@PathVariable("userName") String userName){

        return this.userService.getUser(userName);
    }

    //delete user by userid

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
        this.userService.deleteUser(userId);
    }

    // update user
    @PutMapping("/")
    public ResponseEntity<User> updateUser(@RequestBody User user){
        return ResponseEntity.ok(this.userService.updateUser(user));
    }

    // get single user

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable("userId") Long userId){

        return this.userService.getUserById(userId);
    }


    //change password handler
//    @PutMapping("/change-password")
//    public void changePassword(@RequestBody User user) {
//        System.out.println(user.getPassword());
//
//        this.userService.changePassword(user);
//
//    }

//Change user password
@PostMapping("/change-password")
public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
    // You may want to perform additional validation and security checks here

    userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());

    return ResponseEntity.ok("Password changed successfully");
}


    @ExceptionHandler(UserFoundException.class)
    public ResponseEntity<?> exceptionHandler(UserFoundException ex) {
        return ResponseEntity.ok(ex.getMessage());
    }
}
