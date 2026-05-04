package com.taskflow.project.service.impl;

import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.events.RoutingKeys;
import com.taskflow.events.dto.ProjectEvents;
import com.taskflow.project.constant.enums.Role;
import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.dto.request.UpdateProjectRequest;
import com.taskflow.project.dto.response.ProjectResponse;
import com.taskflow.project.entity.*;
import com.taskflow.project.mapper.ProjectMapper;
import com.taskflow.project.messaging.ProjectEventPublisher;
import com.taskflow.project.repository.*;
import com.taskflow.project.service.AuthorizationService;
import com.taskflow.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final BoardListRepository listRepository;
    private final ProjectMapper mapper;
    private final ProjectEventPublisher publisher;
    private final AuthorizationService authz;

    @Override
    @Transactional
    public ProjectResponse create(Long callerId, CreateProjectRequest req) {
        if (projectRepository.existsByKeyAndDeletedFalse(req.getKey())) {
            throw new ConflictException("project_key_taken");
        }

        long now = Instant.now().toEpochMilli();

        Project project = new Project();
        project.setName(req.getName());
        project.setKey(req.getKey());
        project.setDescription(req.getDescription());
        project.setType(req.getType());
        project.setOwnerId(callerId);
        project = projectRepository.save(project);

        // Owner làm OWNER member
        ProjectMember owner = new ProjectMember();
        owner.setProjectId(project.getId());
        owner.setUserId(callerId);
        owner.setRole(Role.OWNER);
        owner.setJoinedAt(now);
        memberRepository.save(owner);

        // Default board "Main Board"
        Board board = new Board();
        board.setProjectId(project.getId());
        board.setName("Main Board");
        board.setDescription("Default board auto-created with project");
        board.setColor("#0079BF");
        board.setPosition(0);
        board = boardRepository.save(board);

        // 3 default lists
        createList(board.getId(), "To Do", 0);
        createList(board.getId(), "In Progress", 1);
        createList(board.getId(), "Done", 2);

        // Publish events
        publisher.publish(RoutingKeys.PROJECT_CREATED, callerId,
                ProjectEvents.ProjectCreated.builder()
                        .projectId(project.getId())
                        .key(project.getKey())
                        .name(project.getName())
                        .ownerId(callerId)
                        .type(project.getType().name())
                        .build());

        publisher.publish(RoutingKeys.BOARD_CREATED, callerId,
                ProjectEvents.BoardCreated.builder()
                        .boardId(board.getId())
                        .projectId(project.getId())
                        .name(board.getName())
                        .color(board.getColor())
                        .build());

        ProjectResponse resp = mapper.toResponse(project);
        resp.setMyRole(Role.OWNER.name());
        return resp;
    }

    private void createList(Long boardId, String name, int position) {
        BoardList list = new BoardList();
        list.setBoardId(boardId);
        list.setName(name);
        list.setPosition(position);
        listRepository.save(list);
    }

    @Override
    public ProjectResponse get(Long callerId, Long projectId) {
        Role role = authz.getRole(projectId, callerId);
        if (role == null) {
            throw new com.taskflow.common.exception.ForbiddenException("not_a_member");
        }
        Project p = loadProject(projectId);
        ProjectResponse resp = mapper.toResponse(p);
        resp.setMyRole(role.name());
        return resp;
    }

    @Override
    public List<ProjectResponse> listMine(Long callerId) {
        return projectRepository.findAllForUser(callerId).stream()
                .map(p -> {
                    ProjectResponse r = mapper.toResponse(p);
                    Role role = authz.getRole(p.getId(), callerId);
                    if (role != null) r.setMyRole(role.name());
                    return r;
                })
                .toList();
    }

    @Override
    @Transactional
    public ProjectResponse update(Long callerId, Long projectId, UpdateProjectRequest req) {
        authz.requireRole(projectId, callerId, Role.ADMIN);
        Project p = loadProject(projectId);
        if (req.getName() != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getType() != null) p.setType(req.getType());
        p = projectRepository.save(p);
        return mapper.toResponse(p);
    }

    @Override
    @Transactional
    public void delete(Long callerId, Long projectId) {
        authz.requireRole(projectId, callerId, Role.OWNER);
        Project p = loadProject(projectId);
        p.setDeleted(true);
        projectRepository.save(p);

        publisher.publish(RoutingKeys.PROJECT_DELETED, callerId,
                com.taskflow.events.dto.ProjectEvents.ProjectDeleted.builder()
                        .projectId(p.getId())
                        .build());
    }

    private Project loadProject(Long id) {
        return projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("Project", id));
    }
}
