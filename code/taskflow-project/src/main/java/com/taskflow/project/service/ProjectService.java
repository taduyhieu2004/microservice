package com.taskflow.project.service;

import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.dto.request.UpdateProjectRequest;
import com.taskflow.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse create(Long callerId, CreateProjectRequest req);
    ProjectResponse get(Long callerId, Long projectId);
    List<ProjectResponse> listMine(Long callerId);
    ProjectResponse update(Long callerId, Long projectId, UpdateProjectRequest req);
    void delete(Long callerId, Long projectId);
}
