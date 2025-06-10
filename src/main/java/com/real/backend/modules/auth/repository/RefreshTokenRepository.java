package com.real.backend.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.auth.domain.RefreshToken;
import com.real.backend.modules.user.domain.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByUser(User user);

    void deleteByToken(String token);

    Optional<RefreshToken> findByUser(User user);
}
