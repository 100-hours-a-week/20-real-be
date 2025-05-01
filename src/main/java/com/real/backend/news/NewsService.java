package com.real.backend.news;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.real.backend.exception.BadRequestException;
import com.real.backend.news.domain.News;
import com.real.backend.news.dto.NewsListResponseDTO;
import com.real.backend.news.dto.NewsSliceDTO;
import com.real.backend.news.repository.NewsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsSliceDTO getAllNews(Long cursorId, int limit, String sort, String cursorStandard) {

        String order = (sort == null || sort.isBlank()) ? "latest" : sort.toLowerCase();

        if (!order.equals("latest") && !order.equals("popular")) {
            throw new BadRequestException("sort 파라미터는 latest 또는 popular 이어야 합니다.");
        }

        Pageable pg = PageRequest.of(0, limit + 1);
        boolean firstPage = (cursorId == null || cursorStandard == null);
        Slice<News> slice;

        if (order.equals("latest")) {

            if (firstPage) {
                slice = newsRepository.fetchLatestFirst(pg);
            } else {
                LocalDateTime cAt = LocalDateTime.parse(cursorStandard);
                slice = newsRepository.fetchLatest(cAt, cursorId, pg);
            }

        } else {

            if (firstPage) {
                slice = newsRepository.fetchPopularFirst(pg);
            } else {
                Long views = Long.parseLong(cursorStandard);
                slice = newsRepository.fetchPopular(views, cursorId, pg);
            }
        }

        List<News> content = slice.getContent();
        boolean hasNext = slice.hasNext();
        List<News> pageItems = content.size() > limit ? content.subList(0, limit) : content;

        // 다음 커서 계산
        String nextCursor = null;
        Long nextCursorId = null;
        if (hasNext) {
            News last = pageItems.get(pageItems.size() - 1);
            nextCursor = order.equals("latest")
                    ? last.getCreatedAt().toString()
                    : String.valueOf(last.getTodayViewCount());
            nextCursorId = last.getId();
        }

        List<NewsListResponseDTO> dtoList = pageItems.stream()
            .map(NewsListResponseDTO::of)
            .toList();

        return new NewsSliceDTO(dtoList, nextCursor, nextCursorId, hasNext);
    }
}
