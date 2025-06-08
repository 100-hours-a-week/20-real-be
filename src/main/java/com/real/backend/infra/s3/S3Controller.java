package com.real.backend.infra.s3;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.infra.s3.dto.S3PresignedCreateRequestDTO;
import com.real.backend.infra.s3.dto.S3PresignedCreateResponseDTO;
import com.real.backend.common.response.DataResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class S3Controller {

    private final S3Service s3Service;
    private final String DIR_NAME = "static/wiki/images";

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("/v1/presigned")
    public DataResponse<S3PresignedCreateResponseDTO> createPresignedUrl(
        @RequestBody S3PresignedCreateRequestDTO s3PresignedCreateRequestDTO
    ) {
        S3PresignedCreateResponseDTO s3PresignedCreateResponseDTO = s3Service.createPresignedUrl(s3PresignedCreateRequestDTO, DIR_NAME);

        return DataResponse.of(s3PresignedCreateResponseDTO);
    }
}
