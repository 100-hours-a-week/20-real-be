package com.real.backend.domain.oauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.oauth.domain.RefreshToken;
import com.real.backend.domain.user.domain.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByUser(User user);
}
