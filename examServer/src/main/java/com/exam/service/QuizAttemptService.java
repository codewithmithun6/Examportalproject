package com.exam.service;

import com.exam.exam.Question;
import com.exam.exam.QuizAttempt;

import java.util.List;
import java.util.Map;

public interface QuizAttemptService {
    Map<String, Object> evaluateAndSave(List<Question> submittedQuestions, String authenticatedUserName);

    List<QuizAttempt> getUserAttempts(String username);

    List<QuizAttempt> getAllAttempts();
}