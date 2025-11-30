package com.team.rpd_project.service;

import com.team.rpd_project.service.validator.UploadValidatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class UploadService {
    private final Path fileStorageLocation;
    private final UploadValidatorService uploadValidatorService;

    public UploadService(@Value("${app.upload-service.file-path}") Path fileStorageLocation, UploadValidatorService uploadValidatorService) {
        this.fileStorageLocation = fileStorageLocation;
        this.uploadValidatorService = uploadValidatorService;
    }

    public void uploadFile(MultipartFile file) {
        //TODO: валидация на пустоту, формат(excel)...
        uploadValidatorService.validateFileNotEmpty(file);

        try {

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            //TODO: сервис выбрасывающий исключение
            throw new RuntimeException();
        }

    }


}
