package com.taskflow.project.mapper;

import com.taskflow.project.dto.response.*;
import com.taskflow.project.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "myRole", ignore = true)
    ProjectResponse toResponse(Project project);

    @Mapping(target = "lists", ignore = true)
    BoardResponse toResponse(Board board);

    ListResponse toResponse(BoardList list);

    SprintResponse toResponse(Sprint sprint);

    MemberResponse toResponse(ProjectMember m);

    default String map(Enum<?> e) {
        return e == null ? null : e.name();
    }
}
