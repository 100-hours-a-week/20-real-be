package com.real.backend.infra.s3;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.real.backend.infra.s3.dto.S3PresignedCreateRequestDTO;
import com.real.backend.infra.s3.dto.S3PresignedCreateResponseDTO;
import com.real.backend.util.S3Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Utils s3Utils;

    public S3PresignedCreateResponseDTO createPresignedUrl(S3PresignedCreateRequestDTO s3PresignedCreateRequestDTO, String directoryName) {
        String key = s3Utils.generateKey(directoryName, s3PresignedCreateRequestDTO.getFileName());

        String presignedUrl = s3Utils.generatePresignedUrl(key, Duration.ofMinutes(5), s3PresignedCreateRequestDTO.getContentType());
        String cloudFrontUrl = s3Utils.buildCloudFrontUrl(presignedUrl);

        return S3PresignedCreateResponseDTO.of(presignedUrl, cloudFrontUrl);
    }
}
