package com.real.backend.modules.notice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.notice.domain.NoticeLike;

@Repository
public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {

    Optional<NoticeLike> findByUserIdAndNoticeId(Long userId, Long noticeId);

    @Modifying
    @Query(value = """
    UPDATE NoticeLike n
    SET n.isActivated = :isActivated
    WHERE n.notice.id = :noticeId AND n.user.id = :userId
""")
    void updateIsActivated(@Param("userId") Long userId, @Param("noticeId") Long noticeId, @Param("isActivated") Boolean isActivated);

}
