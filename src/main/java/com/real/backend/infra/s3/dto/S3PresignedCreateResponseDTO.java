package com.real.backend.infra.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class S3PresignedCreateResponseDTO {
    private String presignedUrl;
    private String cloudFrontUrl;

    public static S3PresignedCreateResponseDTO of(String presignedUrl, String cloudFrontUrl) {
        return S3PresignedCreateResponseDTO.builder()
            .presignedUrl(presignedUrl)
            .cloudFrontUrl(cloudFrontUrl)
            .build();
    }
}
