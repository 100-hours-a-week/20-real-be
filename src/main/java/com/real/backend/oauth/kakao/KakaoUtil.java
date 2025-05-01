package com.real.backend.oauth.kakao;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.oauth.dto.KakaoProfileDTO;
import com.real.backend.oauth.dto.KakaoTokenDTO;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class KakaoUtil {

    @Value("${kakao.api_key}")
    private String apiKey;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    public KakaoTokenDTO getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", apiKey);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoTokenRequest,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoTokenDTO kakaoTokenDTO = null;

        try {
            kakaoTokenDTO = objectMapper.readValue(response.getBody(), KakaoTokenDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return kakaoTokenDTO;
    }

    public KakaoProfileDTO getKakaoProfile(KakaoTokenDTO accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + accessToken.getAccess_token());
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            kakaoProfileRequest,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfileDTO kakaoProfile = null;

        try {
            kakaoProfile = objectMapper.readValue(response.getBody(), KakaoProfileDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoProfile;
    }

    public String redirectToKakaoLogin(){
        return "https://kauth.kakao.com/oauth/authorize?"
            + "response_type=code"
            + "&client_id=" + apiKey
            + "&redirect_uri=" + redirectUri;

    }


}
