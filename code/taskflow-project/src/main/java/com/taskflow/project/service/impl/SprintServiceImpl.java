package com.taskflow.project.service.impl;

import com.taskflow.common.exception.NotFoundException;
import com.taskflow.project.constant.enums.Role;
import com.taskflow.project.constant.enums.SprintStatus;
import com.taskflow.project.dto.request.SprintRequest;
import com.taskflow.project.dto.response.SprintResponse;
import com.taskflow.project.entity.Sprint;
import com.taskflow.project.mapper.ProjectMapper;
import com.taskflow.project.repository.SprintRepository;
import com.taskflow.project.service.AuthorizationService;
import com.taskflow.project.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectMapper mapper;
    private final AuthorizationService authz;

    @Override
    @Transactional
    public SprintResponse create(Long callerId, Long projectId, SprintRequest req) {
        authz.requireRole(projectId, callerId, Role.ADMIN);

        Sprint s = new Sprint();
        s.setProjectId(projectId);
        apply(s, req);
        if (s.getStatus() == null) s.setStatus(SprintStatus.PLANNING);
        return mapper.toResponse(sprintRepository.save(s));
    }

    @Override
    public List<SprintResponse> listForProject(Long callerId, Long projectId) {
        authz.requireMember(projectId, callerId);
        return sprintRepository.findByProjectIdAndDeletedFalseOrderByStartDateDesc(projectId).stream()
                .map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public SprintResponse update(Long callerId, Long sprintId, SprintRequest req) {
        Sprint s = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> NotFoundException.of("Sprint", sprintId));
        authz.requireRole(s.getProjectId(), callerId, Role.ADMIN);
        apply(s, req);
        return mapper.toResponse(sprintRepository.save(s));
    }

    private void apply(Sprint s, SprintRequest req) {
        if (req.getName() != null) s.setName(req.getName());
        if (req.getGoal() != null) s.setGoal(req.getGoal());
        if (req.getStartDate() != null) s.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) s.setEndDate(req.getEndDate());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
    }
}
