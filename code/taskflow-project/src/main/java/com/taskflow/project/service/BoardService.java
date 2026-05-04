package com.taskflow.project.service;

import com.taskflow.project.dto.request.BoardRequest;
import com.taskflow.project.dto.request.ListRequest;
import com.taskflow.project.dto.request.ReorderListsRequest;
import com.taskflow.project.dto.response.BoardResponse;
import com.taskflow.project.dto.response.ListResponse;

import java.util.List;

public interface BoardService {
    BoardResponse create(Long callerId, Long projectId, BoardRequest req);
    List<BoardResponse> listForProject(Long callerId, Long projectId);
    BoardResponse get(Long callerId, Long boardId);
    BoardResponse update(Long callerId, Long boardId, BoardRequest req);
    void delete(Long callerId, Long boardId);

    ListResponse createList(Long callerId, Long boardId, ListRequest req);
    ListResponse updateList(Long callerId, Long listId, ListRequest req);
    void deleteList(Long callerId, Long listId);
    List<ListResponse> reorderLists(Long callerId, Long boardId, ReorderListsRequest req);
}
