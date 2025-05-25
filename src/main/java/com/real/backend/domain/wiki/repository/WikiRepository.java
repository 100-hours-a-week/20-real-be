package com.real.backend.domain.wiki.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.wiki.domain.Wiki;

@Repository
public interface WikiRepository extends JpaRepository<Wiki, Long> {
}
