package com.taskflow.task.mapper;

import com.taskflow.task.dto.response.TaskResponse;
import com.taskflow.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "labelIds", ignore = true)
    TaskResponse toResponse(Task t);

    default String map(Enum<?> e) { return e == null ? null : e.name(); }
}
