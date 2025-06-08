package com.real.backend.modules.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.notice.component.NoticeFinder;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.dto.NoticeLikeResponseDTO;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.infra.redis.PostRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeLikeService {
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;
    private final PostRedisService postRedisService;

    @Transactional
    public NoticeLikeResponseDTO editNoticeLike(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        User user = userFinder.getUser(userId);

        boolean liked = postRedisService.toggleLikeInRedis("notice", userId, noticeId);

        return NoticeLikeResponseDTO.of(noticeId, !liked);
    }
}
