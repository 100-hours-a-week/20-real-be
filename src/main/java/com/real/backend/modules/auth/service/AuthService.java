package com.real.backend.modules.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.auth.dto.KakaoProfileDTO;
import com.real.backend.modules.auth.dto.KakaoTokenDTO;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.auth.kakao.KakaoUtil;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final KakaoUtil kakaoUtil;

    @Transactional
    public TokenDTO oAuthLogin(String accessCode) {
        KakaoTokenDTO oauthToken = kakaoUtil.getAccessToken(accessCode);
        KakaoProfileDTO kakaoProfile = kakaoUtil.getKakaoProfile(oauthToken);

        String email = kakaoProfile.getKakao_account().getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> kakaoService.createKakaoUser(kakaoProfile));

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new TokenDTO(accessToken, refreshToken);
    }
}
