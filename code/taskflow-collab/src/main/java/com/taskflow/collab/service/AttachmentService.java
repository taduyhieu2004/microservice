package com.taskflow.collab.service;

import com.taskflow.collab.client.TaskServiceClient;
import com.taskflow.collab.dto.response.AttachmentResponse;
import com.taskflow.collab.entity.Attachment;
import com.taskflow.collab.messaging.CollabEventPublisher;
import com.taskflow.collab.repository.AttachmentRepository;
import com.taskflow.common.exception.BadRequestException;
import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.events.RoutingKeys;
import com.taskflow.events.dto.CollaborationEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_SIZE = 25L * 1024 * 1024;
    private static final List<String> ALLOWED_MIME = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "text/plain", "text/csv",
            "application/zip",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword", "application/vnd.ms-excel"
    );

    private final AttachmentRepository attachmentRepository;
    private final TaskServiceClient taskClient;
    private final AuthorizationService authz;
    private final MinioStorageService storage;
    private final CollabEventPublisher publisher;

    @Transactional
    public AttachmentResponse upload(Long callerId, Long taskId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("file_required");
        if (file.getSize() > MAX_SIZE) throw new BadRequestException("file_too_large");
        String mime = file.getContentType();
        if (mime != null && !ALLOWED_MIME.contains(mime)) {
            throw new BadRequestException("unsupported_media_type");
        }

        TaskServiceClient.TaskInfo info = taskClient.verify(taskId);
        if (!info.exists()) throw NotFoundException.of("Task", taskId);
        authz.requireRole(info.projectId(), callerId, "EDITOR");

        String storageKey = "tasks/" + taskId + "/" + UUID.randomUUID() + "_" + safe(file.getOriginalFilename());
        storage.upload(storageKey, file);

        Attachment a = new Attachment();
        a.setTaskId(taskId);
        a.setProjectId(info.projectId());
        a.setUploaderId(callerId);
        a.setFileName(file.getOriginalFilename());
        a.setMimeType(mime);
        a.setSizeBytes(file.getSize());
        a.setStorageKey(storageKey);
        a = attachmentRepository.save(a);

        publisher.publish(RoutingKeys.ATTACHMENT_UPLOADED, callerId,
                CollaborationEvents.AttachmentUploaded.builder()
                        .attachmentId(a.getId()).taskId(taskId).projectId(info.projectId())
                        .uploaderId(callerId).fileName(a.getFileName())
                        .sizeBytes(a.getSizeBytes()).mimeType(a.getMimeType()).build());

        return toResponse(a);
    }

    public List<AttachmentResponse> list(Long callerId, Long taskId) {
        TaskServiceClient.TaskInfo info = taskClient.verify(taskId);
        if (!info.exists()) throw NotFoundException.of("Task", taskId);
        authz.requireMember(info.projectId(), callerId);

        return attachmentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtDesc(taskId).stream()
                .map(this::toResponse).toList();
    }

    public DownloadResult download(Long callerId, Long attachmentId) {
        Attachment a = attachmentRepository.findByIdAndDeletedFalse(attachmentId)
                .orElseThrow(() -> NotFoundException.of("Attachment", attachmentId));
        authz.requireMember(a.getProjectId(), callerId);
        InputStream in = storage.download(a.getStorageKey());
        return new DownloadResult(a.getFileName(), a.getMimeType(), a.getSizeBytes(), in);
    }

    public record DownloadResult(String fileName, String mimeType, Long sizeBytes, InputStream stream) {}

    @Transactional
    public void delete(Long callerId, Long attachmentId) {
        Attachment a = attachmentRepository.findByIdAndDeletedFalse(attachmentId)
                .orElseThrow(() -> NotFoundException.of("Attachment", attachmentId));
        if (!a.getUploaderId().equals(callerId)) {
            authz.requireRole(a.getProjectId(), callerId, "ADMIN");
        } else {
            authz.requireMember(a.getProjectId(), callerId);
        }
        a.setDeleted(true);
        attachmentRepository.save(a);
        storage.delete(a.getStorageKey());
    }

    private static String safe(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private AttachmentResponse toResponse(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId()).taskId(a.getTaskId()).projectId(a.getProjectId())
                .uploaderId(a.getUploaderId()).fileName(a.getFileName())
                .mimeType(a.getMimeType()).sizeBytes(a.getSizeBytes())
                .createdAt(a.getCreatedAt()).build();
    }
}
