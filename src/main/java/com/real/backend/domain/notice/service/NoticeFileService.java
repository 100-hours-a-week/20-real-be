package com.real.backend.domain.notice.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.dto.NoticeFileGroups;
import com.real.backend.domain.notice.dto.NoticeFilesResponseDTO;
import com.real.backend.domain.notice.repository.NoticeFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeFileService {

    private final NoticeFileRepository noticeFileRepository;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        "jpg","jpeg","png","gif","bmp"
    );

    public NoticeFileGroups getNoticeFileGroups(Notice notice) {
        List<NoticeFilesResponseDTO> all = noticeFileRepository
            .findByNoticeOrderByFileSeqNoAsc(notice)
            .stream()
            .map(NoticeFilesResponseDTO::from)
            .toList();

        Map<Boolean, List<NoticeFilesResponseDTO>> partitioned =
            all.stream().collect(
                Collectors.partitioningBy(dto ->
                    IMAGE_EXTENSIONS.contains(dto.getFileType().toLowerCase())
                )
            );

        return new NoticeFileGroups(
            partitioned.get(false),
            partitioned.get(true)
        );
    }
}
