package com.team.rpd_project.exception;

public class FileException extends BusinessException {
    public FileException(String message) {
        super("File", message);
    }
}
