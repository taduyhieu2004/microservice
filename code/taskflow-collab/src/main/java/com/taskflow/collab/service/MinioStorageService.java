package com.taskflow.collab.service;

import com.taskflow.collab.config.MinioConfig;
import com.taskflow.common.exception.InternalServerException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minio;
    private final MinioConfig cfg;

    public void upload(String storageKey, MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            minio.putObject(PutObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(storageKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("Upload to MinIO failed: {}", e.getMessage(), e);
            throw new InternalServerException();
        }
    }

    public InputStream download(String storageKey) {
        try {
            return minio.getObject(GetObjectArgs.builder()
                    .bucket(cfg.getBucket()).object(storageKey).build());
        } catch (Exception e) {
            throw new InternalServerException();
        }
    }

    public void delete(String storageKey) {
        try {
            minio.removeObject(RemoveObjectArgs.builder()
                    .bucket(cfg.getBucket()).object(storageKey).build());
        } catch (Exception e) {
            log.warn("Failed to delete MinIO object {}: {}", storageKey, e.getMessage());
        }
    }
}
