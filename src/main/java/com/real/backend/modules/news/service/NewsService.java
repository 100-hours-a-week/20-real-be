package com.real.backend.modules.news.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.util.S3Utils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.ai.dto.NewsAiRequestDTO;
import com.real.backend.infra.ai.dto.NewsAiResponseDTO;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.modules.news.component.NewsFinder;
import com.real.backend.modules.news.domain.News;
import com.real.backend.modules.news.dto.NewsCreateRequestDTO;
import com.real.backend.modules.news.dto.NewsListResponseDTO;
import com.real.backend.modules.news.dto.NewsResponseDTO;
import com.real.backend.modules.news.repository.NewsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final NewsRepository newsRepository;
    private final NewsAiService newsAiService;
    private final PostRedisService postRedisService;
    private final NewsFinder newsFinder;
    private final S3Utils s3Utils;

    @Transactional(readOnly = true)
    public SliceDTO<NewsListResponseDTO> getNewsListByCursor(Long cursorId, int limit, String sort, String cursorStandard) {

        String order = (sort == null || sort.isBlank()) ? "latest" : sort.toLowerCase();

        if (!order.equals("latest") && !order.equals("popular")) {
            throw new BadRequestException("sort 파라미터는 latest 또는 popular 이어야 합니다.");
        }

        Pageable pg = PageRequest.of(0, limit);
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

        Map<Long, Long> todayViewCount = bulkGetCount(content, "news:todayView:");
        Map<Long, Long> commentCount = bulkGetCount(content, "news:comment:");

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
            .map(n -> NewsListResponseDTO.of(
                n,
                todayViewCount.getOrDefault(n.getId(), n.getTodayViewCount()),   // fallback
                commentCount.getOrDefault(n.getId(), n.getCommentCount())
            ))
            .toList();

        return new SliceDTO<>(dtoList, nextCursor, nextCursorId, hasNext);
    }

    private Map<Long, Long> bulkGetCount(List<News> list, String keyPrefix) {
        List<String> keys = list.stream()
            .map(n -> keyPrefix + n.getId())
            .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Long> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Object val = values.get(i);
            if (val != null) {
                result.put(list.get(i).getId(), Long.parseLong(val.toString()));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public NewsResponseDTO getNewsWithUserLiked(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);

        postRedisService.initCount("news", "totalView", newsId, news.getTotalViewCount());
        postRedisService.initCount("news", "todayView", newsId, news.getTodayViewCount());
        postRedisService.initCount("news", "like", newsId, news.getLikeCount());
        postRedisService.initCount("news", "comment", newsId, news.getCommentCount());

        postRedisService.increment("news", "todayView", newsId);
        long totalViewCount = postRedisService.increment("news", "totalView", newsId);
        long likeCount = postRedisService.getCount("news", "like", newsId);
        long commentCount = postRedisService.getCount("news", "comment", newsId);

        boolean liked = postRedisService.userLiked("news", userId, newsId);
        return NewsResponseDTO.from(news, liked, totalViewCount, likeCount, commentCount);
    }


    @Transactional
    public void createNews(NewsCreateRequestDTO newsCreateRequestDTO, MultipartFile image) throws
        JsonProcessingException {

        String url = "";
        if (image != null) { url = s3Utils.upload(image, "static/news/images");}

        NewsAiResponseDTO newsAiResponseDTO = newsAiService.makeTitleAndSummary(
            new NewsAiRequestDTO(
                newsCreateRequestDTO.getContent(),
                newsCreateRequestDTO.getTitle()));

        newsRepository.save(News.of(newsAiResponseDTO, newsCreateRequestDTO.getContent(), url));
    }

    @Transactional
    public void deleteNews(Long newsId) {
        News news = newsFinder.getNews(newsId);
        news.delete();
    }
}
