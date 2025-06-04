package com.real.backend.util;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.real.backend.exception.IOException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3Utils {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    /**
     * MultipartFile을 지정된 S3 디렉토리에 업로드하고, CloudFront를 통해 접근 가능한 URL을 반환한다.
     *
     * @param file    Multipart 업로드 대상 파일
     * @param dirName S3 내 디렉토리 이름 (예: images, images/profile 등)
     * @return CloudFront 경유 이미지 접근 URL
     */
    public String upload(MultipartFile file, String dirName) {
        String key = generateKey(dirName, file.getOriginalFilename());
        putObjectToS3(file, key);
        return buildCloudFrontUrl(key);
    }

    /**
     * S3의 기본 프로필 이미지 경로(images/default-profiles/)에서 랜덤으로 하나를 선택하여 CloudFront URL로 반환한다.
     *
     * @return CloudFront URL 형식의 랜덤 프로필 이미지 경로
     */
    public String getRandomDefaultProfileUrl() {
        List<String> keys = listS3Keys("static/images/default_profiles/");
        validateNonEmpty(keys);
        String randomKey = pickRandomKey(keys);
        return buildCloudFrontUrl(randomKey);
    }

    public String generatePresignedUrl(String key, Duration validDuration, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder().putObjectRequest(objectRequest)
            .signatureDuration(validDuration).build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    /**
     * UUID 기반 고유 파일명을 생성하여 S3 key를 구성한다.
     *
     * @param dirName          S3 디렉토리 이름
     * @param originalFilename 원본 파일 이름
     * @return S3에 저장할 고유한 key
     */
    public String generateKey(String dirName, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        return dirName + "/" + uuid + "_" + originalFilename;
    }

    /**
     * 지정된 key로 S3에 파일을 업로드한다.
     *
     * @param file 업로드할 파일
     * @param key  S3에 저장할 key
     * @throws IOException 사용자 정의 예외
     */
    private void putObjectToS3(MultipartFile file, String key) {
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key)
                .contentType(file.getContentType()).build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (java.io.IOException | S3Exception e) {
            throw new IOException("[ERROR] S3 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * CloudFront를 통해 접근 가능한 전체 URL을 생성한다.
     *
     * @param key S3 객체 key
     * @return CloudFront 경유 접근 URL
     */
    public String buildCloudFrontUrl(String key) {
        String normalizedDomain = normalizeDomain(cloudFrontDomain);
        String normalizedKey = normalizeKey(key);
        return normalizedDomain + "/" + normalizedKey;
    }

    /**
     * 지정된 prefix로 시작하는 S3 객체들의 key 목록을 반환한다.
     *
     * @param prefix 예: "images/default-profiles/"
     * @return 객체 key 리스트
     */
    private List<String> listS3Keys(String prefix) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

        return response.contents().stream().map(S3Object::key).filter(key -> !key.endsWith("/")) // 폴더 객체 제외
            .toList();
    }

    /**
     * 리스트가 비어있으면 IllegalStateException을 던진다.
     *
     * @param list 확인 대상 리스트
     */
    private void validateNonEmpty(List<String> list) {
        if (list.isEmpty()) {
            throw new IllegalStateException("기본 프로필 이미지가 존재하지 않습니다.");
        }
    }

    /**
     * 리스트에서 무작위로 하나의 key를 선택한다.
     *
     * @param keys S3 객체 key 리스트
     * @return 선택된 key
     */
    private String pickRandomKey(List<String> keys) {
        return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
    }

    private String normalizeDomain(String domain) {
        return domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
    }

    private String normalizeKey(String key) {
        return key.startsWith("/") ? key.substring(1) : key;
    }
}
