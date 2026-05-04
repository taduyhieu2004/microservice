package com.taskflow.collab.service;

import com.taskflow.collab.client.TaskServiceClient;
import com.taskflow.collab.dto.request.CreateCommentRequest;
import com.taskflow.collab.dto.request.UpdateCommentRequest;
import com.taskflow.collab.dto.response.CommentResponse;
import com.taskflow.collab.entity.Comment;
import com.taskflow.collab.messaging.CollabEventPublisher;
import com.taskflow.collab.repository.CommentRepository;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.events.RoutingKeys;
import com.taskflow.events.dto.CollaborationEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@user(\\d+)");

    private final CommentRepository commentRepository;
    private final TaskServiceClient taskClient;
    private final AuthorizationService authz;
    private final CollabEventPublisher publisher;

    @Transactional
    public CommentResponse create(Long callerId, Long taskId, CreateCommentRequest req) {
        TaskServiceClient.TaskInfo info = taskClient.verify(taskId);
        if (!info.exists()) throw NotFoundException.of("Task", taskId);
        authz.requireRole(info.projectId(), callerId, "COMMENTER");

        Comment c = new Comment();
        c.setTaskId(taskId);
        c.setProjectId(info.projectId());
        c.setAuthorId(callerId);
        c.setContent(req.getContent());
        c.setParentId(req.getParentId());
        c = commentRepository.save(c);

        List<Long> mentions = extractMentions(req.getContent());
        publisher.publish(RoutingKeys.COMMENT_ADDED, callerId,
                CollaborationEvents.CommentAdded.builder()
                        .commentId(c.getId()).taskId(taskId).projectId(info.projectId())
                        .authorId(callerId)
                        .contentPreview(preview(req.getContent()))
                        .mentionedUserIds(mentions).build());

        return toResponse(c);
    }

    public PageResponse<CommentResponse> list(Long callerId, Long taskId, int page, int size) {
        TaskServiceClient.TaskInfo info = taskClient.verify(taskId);
        if (!info.exists()) throw NotFoundException.of("Task", taskId);
        authz.requireMember(info.projectId(), callerId);

        Page<Comment> p = commentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(
                taskId, PageRequest.of(page, size));
        return PageResponse.of(p.map(this::toResponse));
    }

    @Transactional
    public CommentResponse update(Long callerId, Long commentId, UpdateCommentRequest req) {
        Comment c = loadComment(commentId);
        authorizeMutate(callerId, c);
        c.setContent(req.getContent());
        return toResponse(commentRepository.save(c));
    }

    @Transactional
    public void delete(Long callerId, Long commentId) {
        Comment c = loadComment(commentId);
        authorizeMutate(callerId, c);
        c.setDeleted(true);
        commentRepository.save(c);
    }

    private void authorizeMutate(Long callerId, Comment c) {
        if (!c.getAuthorId().equals(callerId)) {
            authz.requireRole(c.getProjectId(), callerId, "ADMIN");
        } else {
            authz.requireMember(c.getProjectId(), callerId);
        }
    }

    private Comment loadComment(Long id) {
        return commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("Comment", id));
    }

    private static String preview(String s) {
        if (s == null) return "";
        return s.length() <= 200 ? s : s.substring(0, 200) + "…";
    }

    private static List<Long> extractMentions(String content) {
        List<Long> ids = new ArrayList<>();
        if (content == null) return ids;
        Matcher m = MENTION_PATTERN.matcher(content);
        while (m.find()) {
            try { ids.add(Long.parseLong(m.group(1))); } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId()).taskId(c.getTaskId()).projectId(c.getProjectId())
                .authorId(c.getAuthorId()).content(c.getContent()).parentId(c.getParentId())
                .createdAt(c.getCreatedAt()).lastUpdatedAt(c.getLastUpdatedAt())
                .build();
    }
}
