package com.taskflow.user.repository;

import com.taskflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.deleted = false
              AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<User> search(@Param("q") String q, Pageable pageable);
}
