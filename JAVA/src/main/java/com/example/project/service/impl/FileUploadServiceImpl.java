package com.example.project.service.impl;

import com.example.project.dto.response.FileUploadResponse;
import com.example.project.service.FileUploadService;
import com.example.project.upload.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

	private final FileUtil fileUtil;

	@Value("${app.upload.dir}")
	private String uploadDir;

	@Override
	public FileUploadResponse upload(MultipartFile file) {
		try {
			String filename = fileUtil.store(file);
			return FileUploadResponse.builder()
					.filename(filename)
					.url("/" + uploadDir + "/" + filename)
					.build();
		} catch (IOException ex) {
			throw new com.example.project.exception.BadRequestException("Failed to store file: " + ex.getMessage());
		}
	}
}
