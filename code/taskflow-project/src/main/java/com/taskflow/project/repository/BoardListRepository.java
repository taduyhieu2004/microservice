package com.taskflow.project.repository;

import com.taskflow.project.entity.BoardList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardListRepository extends JpaRepository<BoardList, Long> {

    Optional<BoardList> findByIdAndDeletedFalse(Long id);

    List<BoardList> findByBoardIdAndDeletedFalseOrderByPositionAsc(Long boardId);
}
