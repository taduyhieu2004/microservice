package com.taskflow.collab.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AttachmentResponse {
    private Long id;
    private Long taskId;
    private Long projectId;
    private Long uploaderId;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
    private Long createdAt;
}
