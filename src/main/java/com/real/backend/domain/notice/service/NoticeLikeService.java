package com.real.backend.domain.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.dto.NoticeLikeResponseDTO;
import com.real.backend.domain.notice.repository.NoticeLikeRepository;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.infra.redis.PostRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeLikeService {
    private final NoticeLikeRepository noticeLikeRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;
    private final PostRedisService postRedisService;

    @Transactional
    public NoticeLikeResponseDTO editNoticeLike(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userFinder.getUser(userId);

        boolean liked = postRedisService.userLiked("notice", userId, noticeId);
        postRedisService.createUserLike("notice", userId, noticeId, liked);

        return NoticeLikeResponseDTO.of(noticeId, !liked);
    }
}
