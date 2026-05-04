package com.taskflow.project.repository;

import com.taskflow.project.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByIdAndDeletedFalse(Long id);

    List<Board> findByProjectIdAndDeletedFalseOrderByPositionAsc(Long projectId);
}
