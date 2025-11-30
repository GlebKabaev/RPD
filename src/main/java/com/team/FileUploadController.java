package com.team;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;
import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequestMapping
public class FileUploadController {
    private final Path fileStorageLocation;
    public FileUploadController() throws IOException {
        this.fileStorageLocation= Paths.get("upload").toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        }
        catch (Exception ex){
            throw new RuntimeException("Не удалось создать директорию",ex);
        }
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file){
        try{
            if(file.isEmpty()){
                return ResponseEntity.badRequest().body("Файл пустой");
            }
            String fileName= StringUtils.cleanPath(file.getOriginalFilename());
            if(fileName.contains("..")){
                return ResponseEntity.badRequest().body("Неверное расширение файла");
            }
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation,StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok().body("Файл успешно загружен");
        }
        catch(IOException ex){
            return ResponseEntity.internalServerError().body("Ошибка при загрузке файла");
        }
    }
}
