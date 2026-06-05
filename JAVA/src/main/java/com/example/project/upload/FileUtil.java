package com.example.project.upload;

/**
 * Helper methods for validating and storing uploaded files.
 */

import com.example.project.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileUtil {

	private final Path uploadDir;
	private final List<String> allowedTypes;

	public FileUtil(
			@Value("${app.upload.dir}") String uploadDir,
			@Value("${app.upload.allowed-types}") String allowedTypes) {
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
		this.allowedTypes = Arrays.asList(allowedTypes.split(","));
	}

	public String store(MultipartFile file) throws IOException {
		validate(file);
		Files.createDirectories(uploadDir);

		String extension = getExtension(file.getOriginalFilename());
		String filename = UUID.randomUUID() + extension;
		Path target = uploadDir.resolve(filename);
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return filename;
	}

	public void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("File must not be empty");
		}
		String contentType = file.getContentType();
		if (contentType == null || !allowedTypes.contains(contentType)) {
			throw new BadRequestException("File type not allowed: " + contentType);
		}
	}

	public Path resolve(String filename) {
		if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
			throw new BadRequestException("Invalid filename");
		}
		return uploadDir.resolve(filename).normalize();
	}

	public boolean exists(String filename) {
		return Files.exists(resolve(filename));
	}

	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf('.'));
	}
}
