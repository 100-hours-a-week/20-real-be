package com.real.backend.modules.notice.service;

import static com.real.backend.common.util.CursorUtils.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.modules.notice.component.NoticeFinder;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.modules.notice.dto.NoticeFileGroups;
import com.real.backend.modules.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.modules.notice.dto.NoticeListResponseDTO;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.notification.dto.NoticeCreatedEvent;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeFileService noticeFileService;
    private final NoticeAiService noticeAiService;
    private final PostRedisService postRedisService;
    private final NoticeRedisService noticeRedisService;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;
    private final ApplicationEventPublisher publisher;

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
                Boolean userRead = noticeRedisService.getUserRead(userId, notice.getId());
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
    public NoticeInfoResponseDTO getNoticeById(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        userFinder.getUser(userId);

        postRedisService.initCount("notice", "totalView", noticeId, notice.getTotalViewCount());
        postRedisService.initCount("notice", "like", noticeId, notice.getLikeCount());
        postRedisService.initCount("notice", "comment", noticeId, notice.getCommentCount());

        postRedisService.increment("notice", "totalView", noticeId);
        long likeCount = postRedisService.getCount("notice", "like", noticeId);
        long commentCount = postRedisService.getCount("notice", "comment", noticeId);

        NoticeFileGroups noticeFileGroups = noticeFileService.getNoticeFileGroups(notice);
        boolean liked = postRedisService.userLiked("notice", userId, noticeId);
        return NoticeInfoResponseDTO.from(notice, liked, likeCount, commentCount, noticeFileGroups.files(), noticeFileGroups.images());
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        notice.delete();
        noticeRepository.save(notice);
    }

    @Transactional
    public void editNotice(Long noticeId, NoticeCreateRequestDTO noticeCreateRequestDTO) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userRepository.findById(notice.getUser().getId()).orElseThrow(() -> new NotFoundException("User not found"));

        notice.updateNotice(noticeCreateRequestDTO, user);
        notice.updateCreatedAt(noticeCreateRequestDTO.getCreatedAt());
        noticeRepository.save(notice);
    }

    @Transactional
    public void createNotice(NoticeCreateRequestDTO noticeCreateRequestDTO, List<MultipartFile> images,
        List<MultipartFile> files) throws JsonProcessingException {

        String userName = noticeCreateRequestDTO.getUserName();
        User user = userRepository.findByNickname(userName).orElseThrow(() -> new NotFoundException("해당 이름을 가진 사용자가 없습니다."));

        NoticeSummaryRequestDTO noticeSummaryRequestDTO = new NoticeSummaryRequestDTO(noticeCreateRequestDTO.getContent(),
            noticeCreateRequestDTO.getTitle());
        NoticeSummaryResponseDTO noticeSummaryResponseDTO = noticeAiService.makeSummary(noticeSummaryRequestDTO);

        Notice notice = Notice.builder()
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
            .build();

        noticeRepository.save(notice);
        notice.updateCreatedAt(noticeCreateRequestDTO.getCreatedAt());

        noticeFileService.uploadFilesToS3(images, notice, true);
        noticeFileService.uploadFilesToS3(files, notice, false);
        noticeRepository.save(notice);

        publisher.publishEvent(new NoticeCreatedEvent(notice));
    }
}
