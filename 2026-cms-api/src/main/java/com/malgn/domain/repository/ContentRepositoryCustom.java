package com.malgn.domain.repository;

import com.malgn.domain.model.Content;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ContentRepositoryCustom {
    Page<Content> search(ContentSearchCondition condition, Pageable pageable);
}
