package com.taskflow.task.service;

import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.task.dto.request.LabelRequest;
import com.taskflow.task.dto.response.LabelResponse;
import com.taskflow.task.entity.Label;
import com.taskflow.task.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final AuthorizationService authz;

    public List<LabelResponse> list(Long callerId, Long projectId) {
        authz.requireMember(projectId, callerId);
        return labelRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public LabelResponse create(Long callerId, Long projectId, LabelRequest req) {
        authz.requireRole(projectId, callerId, "ADMIN");
        if (labelRepository.existsByProjectIdAndNameAndDeletedFalse(projectId, req.getName())) {
            throw new ConflictException("label_name_taken");
        }
        Label l = new Label();
        l.setProjectId(projectId);
        l.setName(req.getName());
        l.setColor(req.getColor());
        return toResponse(labelRepository.save(l));
    }

    @Transactional
    public LabelResponse update(Long callerId, Long labelId, LabelRequest req) {
        Label l = labelRepository.findByIdAndDeletedFalse(labelId)
                .orElseThrow(() -> NotFoundException.of("Label", labelId));
        authz.requireRole(l.getProjectId(), callerId, "ADMIN");
        l.setName(req.getName());
        l.setColor(req.getColor());
        return toResponse(labelRepository.save(l));
    }

    @Transactional
    public void delete(Long callerId, Long labelId) {
        Label l = labelRepository.findByIdAndDeletedFalse(labelId)
                .orElseThrow(() -> NotFoundException.of("Label", labelId));
        authz.requireRole(l.getProjectId(), callerId, "ADMIN");
        l.setDeleted(true);
        labelRepository.save(l);
    }

    private LabelResponse toResponse(Label l) {
        return LabelResponse.builder()
                .id(l.getId()).projectId(l.getProjectId())
                .name(l.getName()).color(l.getColor()).build();
    }
}
