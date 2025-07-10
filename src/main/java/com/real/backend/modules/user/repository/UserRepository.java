package com.real.backend.modules.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

	Optional<User> findByNickname(String nickname);

    @Query("SELECT u.id FROM User u WHERE u.lastLoginAt >= :since")
    List<Long> findRecentlyActiveUserIds(@Param("since") LocalDateTime since);

    @Query("SELECT u.role FROM User u WHERE u.id = :id")
    Role findRoleById(@Param("id") Long id);
}
