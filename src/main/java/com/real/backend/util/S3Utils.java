package com.real.backend.util;

import com.real.backend.exception.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
public class S3Utils {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Utils(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                   @Value("${cloud.aws.credentials.secret-key}") String secretKey,
                   @Value("${cloud.aws.region.static}") String region,
                   @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder().region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }

    //s3버킷/prefix 디렉토리 내부에 있는 이미지 탐색 후 리턴
    public String getRandomProfileImageUrl(String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName)
                .prefix(prefix.endsWith("/") ? prefix : prefix + "/").build();

            var response = s3Client.listObjectsV2(request);

            List<String> imageKeys = response.contents().stream().map(S3Object::key).filter(key -> !key.endsWith("/"))
                .toList();

            if (imageKeys.isEmpty()) {
                throw new IOException("[ERROR] S3에 등록된 기본 프로필 이미지가 존재하지 않습니다.");
            }

            String randomKey = imageKeys.get(new Random().nextInt(imageKeys.size()));
            return "https://" + bucketName + ".s3.amazonaws.com/" + randomKey;
        } catch (S3Exception e) {
            throw new IOException(
                "[ERROR] S3에서 프로필 이미지를 가져오는 중 오류가 발생했습니다: "
                    + e.awsErrorDetails().errorMessage());
        }
    }

    public String upload(MultipartFile file, String dirName) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = dirName + "/" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName)
                .contentType(file.getContentType()).build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));

            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
        } catch (java.io.IOException | S3Exception e) {
            throw new IOException("[ERROR] S3에 파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
