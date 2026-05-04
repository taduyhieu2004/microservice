package com.taskflow.project.service;

import com.taskflow.project.constant.enums.Role;
import com.taskflow.project.dto.response.MemberResponse;

import java.util.List;

public interface MemberService {
    List<MemberResponse> list(Long callerId, Long projectId);
    MemberResponse add(Long callerId, Long projectId, Long userId, Role role);
    MemberResponse changeRole(Long callerId, Long projectId, Long userId, Role newRole);
    void remove(Long callerId, Long projectId, Long userId);
}
