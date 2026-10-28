package com.exam.service;

import com.exam.exam.Question;
import com.exam.exam.Quiz;
import com.exam.exam.QuizAttempt;
import com.exam.model.User;
import com.exam.repository.QuizAttemptRepository;
import com.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuizAttemptService {
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepository;

    public QuizAttemptService(QuestionService questionService,
                              UserRepository userRepository,
                              QuizAttemptRepository attemptRepository) {
        this.questionService = questionService;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional
    public Map<String, Object> evaluateAndSave(List<Question> submittedQuestions, String authenticatedUserName) {
        if (submittedQuestions == null || submittedQuestions.isEmpty()) {
            throw new IllegalArgumentException("The quiz has no submitted questions");
        }

        User user = userRepository.findByUserName(authenticatedUserName);
        if (user == null) {
            throw new IllegalArgumentException("Authenticated user account was not found");
        }

        Set<Long> questionIds = new HashSet<>();
        Quiz quiz = null;
        int correctAnswers = 0;
        int attemptedQuestions = 0;

        for (Question submitted : submittedQuestions) {
            if (submitted.getQuesId() == null || !questionIds.add(submitted.getQuesId())) {
                throw new IllegalArgumentException("Submitted quiz contains an invalid or duplicate question");
            }

            Question storedQuestion = questionService.get(submitted.getQuesId());
            if (storedQuestion == null || storedQuestion.getQuiz() == null) {
                throw new IllegalArgumentException("A submitted question could not be found");
            }
            if (quiz == null) {
                quiz = storedQuestion.getQuiz();
            } else if (!quiz.getqId().equals(storedQuestion.getQuiz().getqId())) {
                throw new IllegalArgumentException("Submitted questions must belong to one quiz");
            }

            String givenAnswer = submitted.getGivenAnswer();
            boolean answered = givenAnswer != null && !givenAnswer.trim().isEmpty();
            if (answered) {
                attemptedQuestions++;
                if (storedQuestion.getAnswer() != null && storedQuestion.getAnswer().equals(givenAnswer)) {
                    correctAnswers++;
                }
            }
        }

        double totalMarks;
        try {
            totalMarks = Double.parseDouble(quiz.getMaxMarks());
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Quiz maximum marks are not configured correctly");
        }
        if (!Double.isFinite(totalMarks) || totalMarks <= 0) {
            throw new IllegalArgumentException("Quiz maximum marks must be greater than zero");
        }

        int totalQuestions = submittedQuestions.size();
        int unattemptedQuestions = totalQuestions - attemptedQuestions;
        int wrongAnswers = attemptedQuestions - correctAnswers;
        double marksGot = totalMarks * correctAnswers / totalQuestions;
        double percentage = marksGot * 100.0 / totalMarks;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(user.getId());
        attempt.setUserName(user.getUserName());
        attempt.setLearnerName((safe(user.getFirstName()) + " " + safe(user.getLastName())).trim());
        attempt.setEmail(safe(user.getEmail()));
        attempt.setQuizId(quiz.getqId());
        attempt.setQuizTitle(safe(quiz.getTitle()));
        attempt.setSubjectName(quiz.getCategory() == null ? "Uncategorized" : safe(quiz.getCategory().getTitle()));
        attempt.setTotalQuestions(totalQuestions);
        attempt.setTotalMarks(round(totalMarks));
        attempt.setMarksGot(round(marksGot));
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setAttemptedQuestions(attemptedQuestions);
        attempt.setUnattemptedQuestions(unattemptedQuestions);
        attempt.setWrongAnswers(wrongAnswers);
        attempt.setPercentage(round(percentage));
        attempt.setCompletedAt(LocalDateTime.now());

        QuizAttempt saved = attemptRepository.save(attempt);
        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", saved.getAttemptId());
        result.put("marksGot", saved.getMarksGot());
        result.put("totalMarks", saved.getTotalMarks());
        result.put("correctAnswers", saved.getCorrectAnswers());
        result.put("attempted", saved.getAttemptedQuestions());
        result.put("attemptedQuestions", saved.getAttemptedQuestions());
        result.put("unattemptedQuestions", saved.getUnattemptedQuestions());
        result.put("wrongAnswers", saved.getWrongAnswers());
        result.put("percentage", saved.getPercentage());
        return result;
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getUserAttempts(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new IllegalArgumentException("Authenticated user account was not found");
        }
        return attemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getAllAttempts() {
        return attemptRepository.findAllByOrderByCompletedAtDesc();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}