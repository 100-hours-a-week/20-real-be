package com.real.backend.domain.notice.tmp;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.response.StatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeTmpController {

    private final NoticeTmpService noticeTmpService;

    @PostMapping("/v1/notices/tmp")
    public StatusResponse createNoticeTmp(
        @RequestPart("notice") NoticeCreateRequestTmpDTO noticeCreateRequestDTO,
        @RequestPart(value = "images", required = false) List<MultipartFile> images,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws JsonProcessingException {
        noticeTmpService.createNoticeTmp(noticeCreateRequestDTO, images, files);
        return StatusResponse.of(201, "공지가 성공적으로 생성되었습니다.");
    }
}
