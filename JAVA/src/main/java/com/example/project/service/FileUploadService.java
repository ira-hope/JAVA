package com.example.project.service;

import com.example.project.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

	FileUploadResponse upload(MultipartFile file);
}
