package com.real.backend.domain.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeLike;
import com.real.backend.domain.notice.dto.NoticeLikeResponseDTO;
import com.real.backend.domain.notice.repository.NoticeLikeRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeLikeService {
    private final NoticeLikeRepository noticeLikeRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;

    @Transactional
    public NoticeLikeResponseDTO editNoticeLike(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userFinder.getUser(userId);

        noticeLikeRepository.insertOrToggle(userId, noticeId);
        NoticeLike noticeLike = noticeLikeRepository.findByNoticeAndUser(notice, user).orElseThrow(() -> new NotFoundException("not found"));

        return NoticeLikeResponseDTO.from(noticeLike);
    }

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
