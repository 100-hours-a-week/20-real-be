package com.real.backend.domain.notice.tmp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeFile;
import com.real.backend.domain.notice.repository.NoticeFileRepository;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.exception.ServerException;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.NoticeAiService;
import com.real.backend.util.S3Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeTmpService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final S3Utils s3Utils;
    private final NoticeFileRepository noticeFileRepository;
    private final NoticeAiService noticeAiService;

    @Transactional
    public void createNoticeTmp(NoticeCreateRequestTmpDTO noticeCreateRequestDTO, List<MultipartFile> images,
        List<MultipartFile> files) throws JsonProcessingException {

        String userName = noticeCreateRequestDTO.userName();
        User user = userRepository.findByNickname(userName);

        // ai에 summary 요청 로직
        NoticeSummaryRequestDTO noticeSummaryRequestDTO = new NoticeSummaryRequestDTO(noticeCreateRequestDTO.content(),
            noticeCreateRequestDTO.title());
        NoticeSummaryResponseDTO noticeSummaryResponseDTO = null;
        for (int i = 0; i < 3; i++) {
            noticeSummaryResponseDTO = noticeAiService.makeSummary(noticeSummaryRequestDTO);
            if (noticeSummaryResponseDTO.isCompleted())
                break;
        }
        if (!noticeSummaryResponseDTO.isCompleted()){
            throw new ServerException("ai가 응답을 주지 못했습니다.");
        }

        Notice notice = noticeRepository.save(Notice.builder()
            .user(user)
            .title(noticeCreateRequestDTO.title())
            .content(noticeCreateRequestDTO.content())
            .summary(noticeSummaryResponseDTO.summary())
            .platform(noticeCreateRequestDTO.platform())
            .tag(noticeCreateRequestDTO.tag())
            .originalUrl(noticeCreateRequestDTO.originalUrl())
            .totalViewCount(0L)
            .commentCount(0L)
            .likeCount(0L)
            .build());

        int i = 0;
        if (images == null)
            images = new ArrayList<>();
        if (files == null)
            files = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Utils.upload(file, "static/notice/files");
            String name = file.getOriginalFilename();
            String type = "";
            if (name != null && name.contains(".")) {
                type = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
            }
            i++;
            Integer fileSeqNo = i;
            noticeFileRepository.save(NoticeFile.builder()
                .notice(notice)
                .fileSeqNo(fileSeqNo)
                .fileUrl(url)
                .type(type)
                .build());

        }
        i = 0;
        for (MultipartFile file : images) {
            String url = s3Utils.upload(file, "static/notice/images");
            String name = file.getOriginalFilename();
            String type = "";
            if (name != null && name.contains(".")) {
                type = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
            }
            Integer fileSeqNo = i;
            i++;
            noticeFileRepository.save(NoticeFile.builder()
                .notice(notice)
                .fileSeqNo(fileSeqNo)
                .fileUrl(url)
                .type(type)
                .build());

        }
    }
}
