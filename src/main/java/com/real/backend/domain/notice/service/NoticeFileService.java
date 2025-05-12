package com.real.backend.domain.notice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeFile;
import com.real.backend.domain.notice.dto.NoticeFileGroups;
import com.real.backend.domain.notice.dto.NoticeFilesResponseDTO;
import com.real.backend.domain.notice.repository.NoticeFileRepository;
import com.real.backend.util.S3Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeFileService {

    private final NoticeFileRepository noticeFileRepository;
    private final S3Utils s3Utils;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        "jpg","jpeg","png","gif","bmp","webp","svg"
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

    @Transactional
    public void uploadFilesToS3(List<MultipartFile> files, Notice notice, boolean isImage) {
        List<MultipartFile> safeFiles = Optional.ofNullable(files).orElseGet(ArrayList::new);
        String dirName = "static/notice/" + (isImage ? "images" : "files");

        for (int i = 0; i < safeFiles.size(); i++) {
            MultipartFile file = safeFiles.get(i);

            String url = s3Utils.upload(file, dirName);
            String name = file.getOriginalFilename();
            String type = "";
            if (name != null && name.contains(".")) {
                type = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
            }

            noticeFileRepository.save(NoticeFile.builder()
                .notice(notice)
                .name(name)
                .fileSeqNo(i+1)
                .fileUrl(url)
                .type(type)
                .build());

        }
    }
}
