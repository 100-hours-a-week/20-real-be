package com.real.backend.infra.s3.dto;

import lombok.Data;

@Data
public class S3PresignedCreateRequestDTO {
    private String fileName;
    private String contentType;
}
