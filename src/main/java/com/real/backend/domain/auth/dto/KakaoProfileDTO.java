package com.real.backend.domain.auth.dto;

import lombok.Getter;

@Getter

public class KakaoProfileDTO {
    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    @Getter
    public class Properties {
        private String nickname;
    }

    @Getter
    public class KakaoAccount {
        private String email;
        private Boolean is_email_verified;
        private Boolean has_email;
        private Boolean profile_nickname_needs_agreement;
        private Boolean email_needs_agreement;
        private Boolean is_email_valid;
        private Profile profile;

        @Getter
        public class Profile {
            private String nickname;
            private Boolean is_default_nickname;
        }
    }
}
