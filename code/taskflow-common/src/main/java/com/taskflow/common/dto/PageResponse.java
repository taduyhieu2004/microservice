package com.taskflow.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int page;
    private int size;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    public static <T> PageResponse<T> of(List<T> content, long total, int page, int size) {
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(total)
                .page(page)
                .size(size)
                .build();
    }
}
