package com.taskflow.project.service;

import com.taskflow.project.dto.request.SprintRequest;
import com.taskflow.project.dto.response.SprintResponse;

import java.util.List;

public interface SprintService {
    SprintResponse create(Long callerId, Long projectId, SprintRequest req);
    List<SprintResponse> listForProject(Long callerId, Long projectId);
    SprintResponse update(Long callerId, Long sprintId, SprintRequest req);
}
