package com.exam.exam;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempt")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String learnerName;
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Long quizId;
    @Column(nullable = false)
    private String quizTitle;
    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private int totalQuestions;
    @Column(nullable = false)
    private double totalMarks;
    @Column(nullable = false)
    private double marksGot;
    @Column(nullable = false)
    private int correctAnswers;
    @Column(nullable = false)
    private int attemptedQuestions;
    @Column(nullable = false)
    private int unattemptedQuestions;
    @Column(nullable = false)
    private int wrongAnswers;
    @Column(nullable = false)
    private double percentage;
    @Column(nullable = false)
    private LocalDateTime completedAt;

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }
    public double getMarksGot() { return marksGot; }
    public void setMarksGot(double marksGot) { this.marksGot = marksGot; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public int getAttemptedQuestions() { return attemptedQuestions; }
    public void setAttemptedQuestions(int attemptedQuestions) { this.attemptedQuestions = attemptedQuestions; }
    public int getUnattemptedQuestions() { return unattemptedQuestions; }
    public void setUnattemptedQuestions(int unattemptedQuestions) { this.unattemptedQuestions = unattemptedQuestions; }
    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}