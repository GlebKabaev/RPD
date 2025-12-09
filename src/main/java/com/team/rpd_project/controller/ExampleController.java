package com.team.rpd_project.controller;

import com.team.rpd_project.service.RpdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class ExampleController {
    private final RpdService rpdService;

    @PostMapping("/fill")
    public ResponseEntity<String> uploadFile(@RequestBody String index) {
        rpdService.createRpd(index);
        return ResponseEntity.ok("Создан");
    }
}
