package com.taskflow.collab.controller;

import com.taskflow.collab.dto.response.AttachmentResponse;
import com.taskflow.collab.service.AttachmentService;
import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService service;

    @PostMapping(value = "/api/v1/tasks/{taskId}/attachments",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttachmentResponse> upload(@PathVariable Long taskId,
                                                  @RequestPart("file") MultipartFile file) {
        return ApiResponse.created("Uploaded",
                service.upload(SecurityHeaderUtils.currentUserId(), taskId, file));
    }

    @GetMapping("/api/v1/tasks/{taskId}/attachments")
    public ApiResponse<List<AttachmentResponse>> list(@PathVariable Long taskId) {
        return ApiResponse.ok(service.list(SecurityHeaderUtils.currentUserId(), taskId));
    }

    @GetMapping("/api/v1/attachments/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        AttachmentService.DownloadResult r = service.download(SecurityHeaderUtils.currentUserId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + r.fileName() + "\"")
                .contentType(r.mimeType() == null ? MediaType.APPLICATION_OCTET_STREAM
                                                  : MediaType.parseMediaType(r.mimeType()))
                .contentLength(r.sizeBytes())
                .body(new InputStreamResource(r.stream()));
    }

    @DeleteMapping("/api/v1/attachments/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Attachment deleted", null);
    }
}
