package com.taskflow.events.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public final class CollaborationEvents {

    private CollaborationEvents() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CommentAdded {
        private Long commentId;
        private Long taskId;
        private Long projectId;
        private Long authorId;
        private String contentPreview;
        private List<Long> mentionedUserIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AttachmentUploaded {
        private Long attachmentId;
        private Long taskId;
        private Long projectId;
        private Long uploaderId;
        private String fileName;
        private Long sizeBytes;
        private String mimeType;
    }
}
