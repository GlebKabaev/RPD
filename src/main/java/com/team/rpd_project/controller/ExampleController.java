package com.team.rpd_project.controller;

import com.team.rpd_project.dto.RtdTableDto;
import com.team.rpd_project.service.UploadService;
import com.team.rpd_project.service.WordTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class ExampleController {
    private final WordTableService wordTableService;

    @PostMapping("/fill")
    public ResponseEntity<String> uploadFile(@RequestBody RtdTableDto dto) {
        try {
            wordTableService.fillAndSave(dto, "src/main/resources/template/2.1.docx", "output/filled_2.1_" + System.currentTimeMillis() + ".docx");
        } catch (IOException ioException) {
            return new ResponseEntity<>(ioException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Получилось");
    }
}
