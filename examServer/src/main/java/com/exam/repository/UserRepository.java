package com.exam.repository;

import com.exam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
   public User findByUserName(String userName);
   public User findByEmailIgnoreCase(String email);
   List<User> findAllByEmailIgnoreCase(String email);
   boolean existsByUserName(String userName);
   boolean existsByEmailIgnoreCase(String email);
}
