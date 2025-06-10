package com.real.backend.modules.wiki.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.wiki.domain.Wiki;

@Repository
public interface WikiRepository extends JpaRepository<Wiki, Long> {
    Optional<Wiki> findByTitle(String title);

    @Query("""
SELECT w.id
FROM Wiki w
WHERE w.deletedAt IS NULL
""")
    List<Long> getAllId();

    @Query("""
        SELECT w FROM Wiki w
        WHERE  w.deletedAt IS NULL
        ORDER  BY w.updatedAt DESC, w.id DESC
        """)
    Slice<Wiki> fetchLatestFirst(Pageable pg);   // 첫 페이지

    @Query("""
        SELECT w FROM Wiki w
        WHERE  w.deletedAt IS NULL
        AND ( w.updatedAt < :uAt
                OR (w.updatedAt = :uAt AND w.id < :id) )
        ORDER BY w.updatedAt DESC, w.id DESC
        """)
    Slice<Wiki> fetchLatest(
        @Param("id") Long id,
        Pageable pg,
        @Param("uAt") LocalDateTime uAt);

    /* 이름순 ------------------------------------------------------------ */
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

    @Query(value = """
    SELECT id FROM wiki
    ORDER BY updated_at DESC
    LIMIT :limit
""", nativeQuery = true)
    List<Long> getAllIdOrderByUpdatedAtLimit(@Param("limit") Integer limit);

    @Query("SELECT w.id FROM Wiki w WHERE w.title = :title AND w.deletedAt IS NULL")
	Long getWikiIdByTitle(@Param("title") String title);
}
