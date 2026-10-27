package com.exam.service.impl;

import com.exam.model.User;
import com.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new UsernameNotFoundException("Invalid username or email");
        }

        String identifier = usernameOrEmail.trim();
        User user = this.userRepository.findByUserName(identifier);
        if (user != null) {
            return user;
        }

        List<User> matchingEmails = this.userRepository.findAllByEmailIgnoreCase(identifier);
        if (matchingEmails.size() != 1) {
            throw new UsernameNotFoundException("Invalid username or email");
        }
        return matchingEmails.get(0);
    }
}
