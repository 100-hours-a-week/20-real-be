package com.real.backend.user.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.real.backend.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime lastLoginAt;
    @CreatedDate
    private LocalDateTime signupAt;
    private LocalDateTime withdrawAt;
}
