package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.dto.NoticePasteRequestDTO;
import com.real.backend.domain.notice.repository.NoticeFileRepository;
import com.real.backend.domain.user.domain.UserNoticeRead;
import com.real.backend.domain.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.domain.notice.dto.NoticeFileGroups;
import com.real.backend.domain.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.repository.UserNoticeReadRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.exception.NotFoundException;
import com.real.backend.exception.ServerException;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.NoticeAiService;
import com.real.backend.util.CursorUtils;
import com.real.backend.util.S3Utils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeLikeService noticeLikeService;
    private final NoticeFileService noticeFileService;
    private final NoticeAiService noticeAiService;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final UserNoticeReadRepository userNoticeReadRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;
    private final S3Utils s3Utils;

    @Transactional(readOnly = true)
    public SliceDTO<NoticeListResponseDTO> getNoticeListByCursor(Long cursorId, int limit, String cursorStandard, Long userId) {

        Pageable pg = buildPageable(limit);

        Slice<Notice> slice = (cursorId == null || cursorStandard == null)
            ? noticeRepository.fetchLatestFirst(pg)
            : noticeRepository.fetchLatest(LocalDateTime.parse(cursorStandard), cursorId, pg);

        return CursorUtils.toCursorDto(
            slice,
            limit,
            notice -> {
                Boolean userRead = getUserRead(userId, notice.getId());
                return NoticeListResponseDTO.from(
                    notice,
                    userRead,
                    notice.getUser().getNickname()
                );
            },
            notice -> notice.getCreatedAt().toString(),
            Notice::getId,
            SliceDTO::new
        );
    }

    @Transactional(readOnly = true)
    public Boolean getUserRead(Long userId, Long noticeId) {
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);

        UserNoticeRead userNoticeRead = userNoticeReadRepository.findByUserAndNotice(user, notice).orElse(null);
        return userNoticeRead != null;

    }

    @Transactional
    public void createNotice(Long userId, NoticeCreateRequestDTO noticeCreateRequestDTO) throws
        JsonProcessingException {
        User user = userFinder.getUser(userId);

        NoticeSummaryRequestDTO noticeSummaryRequestDTO = new NoticeSummaryRequestDTO(noticeCreateRequestDTO.getContent(),
            noticeCreateRequestDTO.getTitle());
        NoticeSummaryResponseDTO noticeSummaryResponseDTO = noticeAiService.makeSummary(noticeSummaryRequestDTO);

        noticeRepository.save(Notice.builder()
            .user(user)
            .title(noticeCreateRequestDTO.getTitle())
            .content(noticeCreateRequestDTO.getContent())
            .summary(noticeSummaryResponseDTO.summary())
            .tag(noticeCreateRequestDTO.getTag())
            .originalUrl(noticeCreateRequestDTO.getOriginalUrl())
            .totalViewCount(0L)
            .commentCount(0L)
            .likeCount(0L)
            .build());
    }

    @Transactional(readOnly = true)
    public NoticeInfoResponseDTO getNoticeById(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        NoticeFileGroups noticeFileGroups = noticeFileService.getNoticeFileGroups(notice);
        return NoticeInfoResponseDTO.from(notice, noticeLikeService.userIsLiked(noticeId, userId), noticeFileGroups.files(), noticeFileGroups.images());
    }

    @Transactional
    public void increaseViewCounts(Long noticeId) {
        Notice notice = noticeFinder.getNotice(noticeId);

        notice.increaseTotalViewCount();
        noticeRepository.save(notice);
    }

    @Transactional
    public void userReadNotice(Long noticeId, Long userId) {
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);

        if (!userNoticeReadRepository.findByUserAndNotice(user, notice).isPresent()) {
            userNoticeReadRepository.save(UserNoticeRead.builder().user(user).notice(notice).build());
        }
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        noticeRepository.delete(notice);
    }

    @Transactional
    public void editNotice(Long noticeId, NoticePasteRequestDTO noticePasteRequestDTO) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userRepository.findById(notice.getUser().getId()).orElseThrow(() -> new NotFoundException("User not found"));

        notice.updateNotice(noticePasteRequestDTO, user);
        noticeRepository.save(notice);
    }

    @Transactional
    public void pasteNoticeTmp(NoticePasteRequestDTO noticeCreateRequestDTO, List<MultipartFile> images,
        List<MultipartFile> files) throws JsonProcessingException {


        String userName = noticeCreateRequestDTO.getUserName();
        User user = userRepository.findByNickname(userName);

        // ai에 summary 요청 로직
        NoticeSummaryRequestDTO noticeSummaryRequestDTO = new NoticeSummaryRequestDTO(noticeCreateRequestDTO.getContent(),
            noticeCreateRequestDTO.getTitle());
        NoticeSummaryResponseDTO noticeSummaryResponseDTO = null;
        for (int i = 0; i < 3; i++) {
            noticeSummaryResponseDTO = noticeAiService.makeSummary(noticeSummaryRequestDTO);
            if (noticeSummaryResponseDTO.isCompleted())
                break;
        }
        if (!noticeSummaryResponseDTO.isCompleted()){
            throw new ServerException("ai가 응답을 주지 못했습니다.");
        }

        LocalDateTime createTime = LocalDateTime.parse(noticeCreateRequestDTO.getCreatedAt());

        Notice notice = noticeRepository.save(Notice.builder()
            .user(user)
            .title(noticeCreateRequestDTO.getTitle())
            .content(noticeCreateRequestDTO.getContent())
            .summary(noticeSummaryResponseDTO.summary())
            .platform(noticeCreateRequestDTO.getPlatform())
            .tag(noticeCreateRequestDTO.getTag())
            .originalUrl(noticeCreateRequestDTO.getOriginalUrl())
            .totalViewCount(0L)
            .commentCount(0L)
            .likeCount(0L)
            .createdAt(createTime)
            .build());

        noticeFileService.uploadFilesToS3(images, notice, true);
        noticeFileService.uploadFilesToS3(files, notice, false);
    }
}
