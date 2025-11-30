package com.team.rpd_project.service.validator;

import com.team.rpd_project.exception.FileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadValidatorService {
    public void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileException("Файл пуст");
        }
    }

}
