package com.exam.repository;

import com.exam.exam.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);
    List<QuizAttempt> findAllByOrderByCompletedAtDesc();
}