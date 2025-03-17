package com.example.luckydaybackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${FILE_UPLOAD_DIR:/app/uploads}")
    private String uploadDir;

    public String saveImage(MultipartFile file, String fileName) {
        try {
            // 저장할 폴더가 없으면 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 폴더 없으면 생성
            }

            // 저장할 파일 경로 설정
            Path filePath = Paths.get(uploadDir + File.separator + fileName);
            Files.write(filePath, file.getBytes());

            return "/uploads/" + fileName; // 저장된 이미지의 URL 반환
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패!", e);
        }
    }
}