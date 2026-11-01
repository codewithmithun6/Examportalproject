package com.exam.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfilePictureStorageService {
    String store(MultipartFile image) throws IOException;
}