package com.team.rpd_project.controller;

import com.team.rpd_project.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class FileUploadController {
    private final UploadService uploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        uploadService.uploadFile(file);
        return ResponseEntity.ok().body("Файл успешно загружен");
    }
}
