package com.exam.controller;

import com.exam.config.JwtUtils;
import com.exam.helper.UserNotFoundException;
import com.exam.model.JwtRequest;
import com.exam.model.JwtResponse;
import com.exam.model.User;
import com.exam.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin("*")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    // generate token

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken(@RequestBody JwtRequest jwtRequest) throws Exception {

        try{

            authenticate(jwtRequest.getUserName(), jwtRequest.getPassword());



        }catch (UserNotFoundException e){
            e.printStackTrace();
            throw new Exception("User not found !!");

        }

        // authenticate

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtRequest.getUserName());
        String token = jwtUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));

    }


    private void authenticate(String userName, String password) throws Exception {


        try{

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName,password));

        }catch (DisabledException e){
            throw new Exception("USER DISABLED "+ e.getMessage());

        }catch (BadCredentialsException e){
            throw new Exception("Invalid credentials "+e.getMessage());

        }
    }

    // returns the details of current user

    @GetMapping("/current-user")
    public User getCurrentUser(Principal principal){
        return((User)this.userDetailsService.loadUserByUsername(principal.getName()));
    }
}
