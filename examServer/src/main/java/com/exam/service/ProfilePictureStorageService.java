package com.exam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfilePictureStorageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp");

    @Value("${exam.upload-dir:uploads}")
    private String uploadDirectory;

    public String store(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Select a profile picture");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Profile picture must be 5 MB or smaller");
        }

        String extension = IMAGE_EXTENSIONS.get(image.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Choose a JPG, PNG, GIF, or WEBP image");
        }

        String filename = UUID.randomUUID() + extension;
        Path directory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Files.copy(image.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }
}