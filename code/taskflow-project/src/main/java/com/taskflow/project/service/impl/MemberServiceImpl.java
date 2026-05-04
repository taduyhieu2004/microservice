package com.taskflow.project.service.impl;

import com.taskflow.common.exception.BadRequestException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.events.RoutingKeys;
import com.taskflow.events.dto.ProjectEvents;
import com.taskflow.project.client.UserServiceClient;
import com.taskflow.project.constant.enums.Role;
import com.taskflow.project.dto.response.MemberResponse;
import com.taskflow.project.entity.ProjectMember;
import com.taskflow.project.mapper.ProjectMapper;
import com.taskflow.project.messaging.ProjectEventPublisher;
import com.taskflow.project.repository.ProjectMemberRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.project.service.AuthorizationService;
import com.taskflow.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserServiceClient userClient;
    private final ProjectMapper mapper;
    private final ProjectEventPublisher publisher;
    private final AuthorizationService authz;

    @Override
    public List<MemberResponse> list(Long callerId, Long projectId) {
        authz.requireMember(projectId, callerId);
        return memberRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public MemberResponse add(Long callerId, Long projectId, Long userId, Role role) {
        authz.requireRole(projectId, callerId, Role.ADMIN);

        // ADMIN không được set role OWNER hoặc ADMIN
        Role callerRole = authz.getRole(projectId, callerId);
        if (callerRole != Role.OWNER && (role == Role.OWNER || role == Role.ADMIN)) {
            throw new ForbiddenException("only_owner_can_assign_admin_or_owner");
        }

        if (memberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, userId)) {
            throw new ConflictException("user_already_member");
        }

        // Verify user tồn tại qua User Service
        Map<Long, Boolean> exists = userClient.verifyExists(List.of(userId));
        if (!Boolean.TRUE.equals(exists.get(userId))) {
            throw new BadRequestException("user_not_found");
        }

        if (!projectRepository.existsByKeyAndDeletedFalse(projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId)).getKey())) {
            throw NotFoundException.of("Project", projectId);
        }

        ProjectMember m = new ProjectMember();
        m.setProjectId(projectId);
        m.setUserId(userId);
        m.setRole(role);
        m.setJoinedAt(Instant.now().toEpochMilli());
        m = memberRepository.save(m);

        publisher.publish(RoutingKeys.PROJECT_MEMBER_ADDED, callerId,
                ProjectEvents.MemberAdded.builder()
                        .projectId(projectId).userId(userId)
                        .role(role.name()).invitedBy(callerId).build());

        return mapper.toResponse(m);
    }

    @Override
    @Transactional
    public MemberResponse changeRole(Long callerId, Long projectId, Long userId, Role newRole) {
        authz.requireRole(projectId, callerId, Role.ADMIN);

        ProjectMember target = memberRepository.findByProjectIdAndUserIdAndDeletedFalse(projectId, userId)
                .orElseThrow(() -> NotFoundException.of("Member", userId));

        // Cannot change role of OWNER (use transfer-ownership)
        if (target.getRole() == Role.OWNER) {
            throw new ForbiddenException("cannot_change_owner_role");
        }

        Role callerRole = authz.getRole(projectId, callerId);
        if (callerRole != Role.OWNER && (newRole == Role.OWNER || newRole == Role.ADMIN || target.getRole() == Role.ADMIN)) {
            throw new ForbiddenException("only_owner_can_modify_admin");
        }

        Role oldRole = target.getRole();
        target.setRole(newRole);
        target = memberRepository.save(target);

        publisher.publish(RoutingKeys.PROJECT_MEMBER_ROLE_CHANGED, callerId,
                ProjectEvents.MemberRoleChanged.builder()
                        .projectId(projectId).userId(userId)
                        .oldRole(oldRole.name()).newRole(newRole.name())
                        .changedBy(callerId).build());

        return mapper.toResponse(target);
    }

    @Override
    @Transactional
    public void remove(Long callerId, Long projectId, Long userId) {
        authz.requireRole(projectId, callerId, Role.ADMIN);

        ProjectMember target = memberRepository.findByProjectIdAndUserIdAndDeletedFalse(projectId, userId)
                .orElseThrow(() -> NotFoundException.of("Member", userId));

        if (target.getRole() == Role.OWNER) {
            throw new ForbiddenException("cannot_remove_owner");
        }
        Role callerRole = authz.getRole(projectId, callerId);
        if (callerRole != Role.OWNER && target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("only_owner_can_remove_admin");
        }

        target.setDeleted(true);
        memberRepository.save(target);

        publisher.publish(RoutingKeys.PROJECT_MEMBER_REMOVED, callerId,
                ProjectEvents.MemberRemoved.builder()
                        .projectId(projectId).userId(userId).removedBy(callerId).build());
    }
}
