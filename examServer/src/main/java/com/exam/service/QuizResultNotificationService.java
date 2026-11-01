package com.exam.service;

import com.exam.exam.QuizAttempt;

public interface QuizResultNotificationService {
    void sendResult(QuizAttempt attempt);
}