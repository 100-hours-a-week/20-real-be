package com.real.backend.modules.wiki.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query(value = """
    SELECT id FROM wiki
    ORDER BY updated_at DESC
    LIMIT :limit
""", nativeQuery = true)
    List<Long> getAllIdOrderByUpdatedAtLimit(@Param("limit") Integer limit);

    @Query("SELECT w.id FROM Wiki w WHERE w.title = :title AND w.deletedAt IS NULL")
	Long getWikiIdByTitle(@Param("title") String title);

    @Query("SELECT w.title FROM Wiki w WHERE w.id = :id")
    String getWikiTitleById(@Param("id") Long id);

    @Query(value = "SELECT * from Wiki WHERE deleted_at IS NULL",nativeQuery = true)
    List<Wiki> findAllWithoutDeleted();

    @Query(value = "SELECT w.updatedAt FROM Wiki w WHERE w.id = :id")
    LocalDateTime getWikiUpdatedAtById(@Param("id") Long id);
}
