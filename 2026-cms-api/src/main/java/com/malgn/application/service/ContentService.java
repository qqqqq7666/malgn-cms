package com.malgn.application.service;

import com.malgn.domain.exception.AuthException;
import com.malgn.domain.exception.ContentException;
import com.malgn.domain.model.Content;
import com.malgn.domain.model.ErrorCode;
import com.malgn.domain.model.Member;
import com.malgn.domain.repository.ContentRepository;
import com.malgn.presentation.dto.request.content.ContentCreateRequest;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import com.malgn.presentation.dto.request.content.ContentUpdateRequest;
import com.malgn.presentation.dto.response.PageResponse;
import com.malgn.presentation.dto.response.content.ContentCreateResponse;
import com.malgn.presentation.dto.response.content.ContentDetailResponse;
import com.malgn.presentation.dto.response.content.ContentListResponse;
import com.malgn.presentation.dto.response.content.ContentUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;

    @Transactional
    public ContentCreateResponse create(ContentCreateRequest request, Member member) {
        Content content = Content.createContent(request.title(), request.description(), member);

        return ContentCreateResponse.from(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentListResponse> getContents(Pageable pageable) {
        return PageResponse.from(
                contentRepository.findAll(pageable)
                        .map(ContentListResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentListResponse> search(ContentSearchCondition condition, Pageable pageable) {
        return PageResponse.from(
                contentRepository.search(condition, pageable)
                        .map(ContentListResponse::from)
        );
    }

    @Transactional
    public ContentDetailResponse getContentDetail(Long id) {
        Content content = findContentById(id);

        content.increaseViewCount();

        return ContentDetailResponse.from(content);
    }

    @Transactional
    public ContentUpdateResponse update(Long id, ContentUpdateRequest request, Member member) {
        Content content = findContentById(id);

        checkPermission(content, member);
        content.update(request.title(), request.description());

        // lastModified 필드 변경 반영
        contentRepository.saveAndFlush(content);

        return ContentUpdateResponse.from(content);
    }

    @Transactional
    public void delete(Long id, Member member) {
        Content content = findContentById(id);

        checkPermission(content, member);

        contentRepository.delete(content);
    }

    private Content findContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ContentException(ErrorCode.CONTENT_NOT_FOUND));
    }

    private void checkPermission(Content content, Member member) {
        if (!content.checkPermission(member) && !member.isAdmin())
            throw new AuthException(ErrorCode.ACCESS_DENIED, "해당 콘텐츠에 대한 권한이 없습니다.");
    }
}
