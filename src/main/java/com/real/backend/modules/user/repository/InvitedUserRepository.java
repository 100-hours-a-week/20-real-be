package com.real.backend.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.user.domain.InvitedUser;

@Repository
public interface InvitedUserRepository extends JpaRepository<InvitedUser, Long> {
    Optional<InvitedUser> findByEmail(String email);
}
