package com.real.backend.infra.s3;

import lombok.Data;

@Data
public class S3FileInfoResponse {
    private String fileName;
    private String contentType;
}
