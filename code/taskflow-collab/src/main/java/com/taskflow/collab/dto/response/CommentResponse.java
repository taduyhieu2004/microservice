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
public class CommentResponse {
    private Long id;
    private Long taskId;
    private Long projectId;
    private Long authorId;
    private String content;
    private Long parentId;
    private Long createdAt;
    private Long lastUpdatedAt;
}
