package com.real.backend.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.auth.domain.RefreshToken;
import com.real.backend.domain.user.domain.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByUser(User user);

    void deleteByToken(String token);

    Optional<RefreshToken> findByUser(User user);
}
