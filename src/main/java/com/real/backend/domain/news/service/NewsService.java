package com.real.backend.domain.news.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.news.component.NewsFinder;
import com.real.backend.domain.news.dto.NewsCreateRequestDTO;
import com.real.backend.exception.BadRequestException;
import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.dto.NewsListResponseDTO;
import com.real.backend.domain.news.dto.NewsResponseDTO;
import com.real.backend.domain.news.repository.NewsRepository;
import com.real.backend.exception.ServerException;
import com.real.backend.infra.ai.dto.NewsAiRequestDTO;
import com.real.backend.infra.ai.dto.NewsAiResponseDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.NewsAiService;
import com.real.backend.util.S3Utils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsLikeService newsLikeService;
    private final NewsFinder newsFinder;
    private final S3Utils s3Utils;
    private final NewsAiService newsAiService;

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

        return new SliceDTO<>(dtoList, nextCursor, nextCursorId, hasNext);
    }

    @Transactional(readOnly = true)
    public NewsResponseDTO getNewsWithUserLiked(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);
        return NewsResponseDTO.from(news, newsLikeService.userIsLiked(newsId, userId));
    }

    @Transactional
    public void increaseViewCounts(Long newsId) {
        News news = newsFinder.getNews(newsId);

        news.increaseTodayViewCount();
        news.increaseTotalViewCount();
        newsRepository.save(news);
    }

    @Transactional
    public void createNews(NewsCreateRequestDTO newsCreateRequestDTO, MultipartFile image) throws
        JsonProcessingException {

        String url = "";
        if (image != null) { url = s3Utils.upload(image, "static/news/images");}

        NewsAiResponseDTO newsAiResponseDTO = null;
        for (int i = 0; i < 3; i++) {
            newsAiResponseDTO = newsAiService.makeTitleAndSummary(
                new NewsAiRequestDTO(newsCreateRequestDTO.content(), newsCreateRequestDTO.title()));
            if (newsAiResponseDTO.isCompleted())
                break;
        }
        if (!newsAiResponseDTO.isCompleted()){
            throw new ServerException("ai가 응답을 주지 못했습니다.");
        }

        newsRepository.save(News.builder()
            .title(newsAiResponseDTO.headline())
            .content(newsCreateRequestDTO.content())
            .tag("뉴스")
            .todayViewCount(0L)
            .totalViewCount(0L)
            .imageUrl(url)
            .summary(newsAiResponseDTO.summary())
            .likeCount(0L)
            .commentCount(0L)
            .build());
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    protected void resetTodayViewCount() {
        newsRepository.resetTodayViewCount();
    }
}
