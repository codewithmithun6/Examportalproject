package com.exam.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface QuestionImportService {
    int importQuestions(Long quizId, MultipartFile file) throws IOException;
}