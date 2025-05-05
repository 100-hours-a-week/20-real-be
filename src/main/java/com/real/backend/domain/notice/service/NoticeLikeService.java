package com.real.backend.domain.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeLike;
import com.real.backend.domain.notice.repository.NoticeLikeRepository;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeLikeService {
    private final NoticeLikeRepository noticeLikeRepository;
    private final UserService userService;
    private final NoticeRepository noticeRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;

    // @Transactional
    // public NoticeLikeResponseDTO editNoticeLike(Long noticeId, Long userId) {
    //     Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new NotFoundException("해당 id를 가진 뉴스가 존재하지 않습니다."));
    //     User user = userService.getUser(userId);
    //     NoticeLike noticeLike = noticeLikeRepository.findByNoticeAndUser(notice, user).orElse(null);
    //
    //     if (noticeLike == null) {
    //         noticeLike = NoticeLike.builder()
    //             .isActivated(false)
    //             .user(user)
    //             .notice(notice)
    //             .build();
    //     }
    //
    //     noticeLike.updateIsActivated();
    //
    //     if (noticeLike.getIsActivated()) notice.increaseLikesCount();
    //     else notice.decreaseLikesCount();
    //
    //     noticeRepository.save(notice);
    //     return NoticeLikeResponseDTO.from(noticeLikeRepository.save(noticeLike));
    //
    // }

    @Transactional(readOnly = true)
    public boolean userIsLiked(Long noticeId, Long userId) {
        NoticeLike noticeLike = getNoticeLike(noticeId, userId);
        if (noticeLike == null) return false;
        return noticeLike.getIsActivated();

    }

    @Transactional(readOnly = true)
    public NoticeLike getNoticeLike(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userFinder.getUser(userId);

        return noticeLikeRepository.findByNoticeAndUser(notice, user).orElse(null);
    }

}
