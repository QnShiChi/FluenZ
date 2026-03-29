package com.fluenz.api.service;

import com.fluenz.api.dto.response.UploadedMediaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalMediaService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public UploadedMediaResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        try {
            Path targetFolder = Paths.get(uploadDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(targetFolder);

            String originalName = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
            String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = UUID.randomUUID() + "_" + sanitizedName;
            Path targetFile = targetFolder.resolve(fileName);

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return UploadedMediaResponse.builder()
                    .fileName(fileName)
                    .url(appBaseUrl + "/uploads/" + folder + "/" + fileName)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
