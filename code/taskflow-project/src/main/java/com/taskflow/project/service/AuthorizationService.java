package com.taskflow.project.service;

import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.project.constant.enums.Role;
import com.taskflow.project.entity.ProjectMember;
import com.taskflow.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ProjectMemberRepository memberRepository;

    public Role getRole(Long projectId, Long userId) {
        return memberRepository.findByProjectIdAndUserIdAndDeletedFalse(projectId, userId)
                .map(ProjectMember::getRole)
                .orElse(null);
    }

    public void requireMember(Long projectId, Long userId) {
        if (getRole(projectId, userId) == null) {
            throw new ForbiddenException("not_a_member");
        }
    }

    public void requireRole(Long projectId, Long userId, Role minRole) {
        Role role = getRole(projectId, userId);
        if (role == null) {
            throw new ForbiddenException("not_a_member");
        }
        if (!role.isAtLeast(minRole)) {
            throw new ForbiddenException("insufficient_role");
        }
    }
}
