package com.real.backend.domain.wiki.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.wiki.domain.Wiki;

@Repository
public interface WikiRepository extends JpaRepository<Wiki, Long> {
    Optional<Wiki> findByTitle(String title);

    @Query("""
SELECT id
FROM Wiki
""")
    List<Long> getAllId();

    @Query("""
        SELECT w FROM Wiki w
        WHERE  w.deletedAt IS NULL
        AND w.title LIKE %:keyword%
        ORDER  BY w.updatedAt DESC, w.id DESC
        """)
    Slice<Wiki> fetchLatestFirst(Pageable pg, @Param("keyword") String keyword);   // 첫 페이지

    @Query("""
        SELECT w FROM Wiki w
        WHERE  w.deletedAt IS NULL
        AND w.title LIKE %:keyword%
        AND w.id < :id
        ORDER BY w.updatedAt DESC, w.id DESC
        """)
    Slice<Wiki> fetchLatest(
        @Param("id") Long id,
        Pageable pg,
        @Param("keyword") String keyword);

    /* 인기순 ------------------------------------------------------------ */
    @Query("""
        SELECT w FROM Wiki w
        WHERE  w.deletedAt IS NULL
        AND w.title LIKE %:keyword%
        ORDER BY w.title ASC
        """)
    Slice<Wiki> fetchTitleFirst(Pageable pg, @Param("keyword") String keyword);

    @Query("""
        SELECT w FROM Wiki w
        WHERE w.title LIKE %:keyword%
        AND w.id > :id
        AND w.deletedAt IS NULL
        ORDER BY w.title ASC
        """)
    Slice<Wiki> fetchTitle(@Param("keyword") String keyword,
        @Param("id")    Long id,
        Pageable pg);

    @Query("""
    SELECT w.id FROM Wiki w
    WHERE w.updatedAt >= :start AND w.updatedAt < :end
""")
    List<Long> findAllIdByUpdatedBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

}
