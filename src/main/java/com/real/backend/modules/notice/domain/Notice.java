package com.real.backend.modules.notice.domain;

import java.util.List;

import com.real.backend.modules.notice.dto.NoticePasteRequestDTO;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.domain.UserNoticeRead;
import com.real.backend.common.base.Post;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class Notice extends Post {
    private String originalUrl;
    private String platform;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // TODO cascade 전략 수정하기
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeComment> noticeComments;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeLike> noticeLikes;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeFile> noticeFiles;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserNoticeRead> userNoticeReads;

    public void updateNotice(NoticePasteRequestDTO noticePasteRequestDTO, User user) {
        this.originalUrl = noticePasteRequestDTO.getOriginalUrl();
        this.platform = noticePasteRequestDTO.getPlatform();
        this.user = user;
        this.updatePost(
            noticePasteRequestDTO.getTitle(),
            noticePasteRequestDTO.getContent(),
            noticePasteRequestDTO.getTag()
            );
    }
}
