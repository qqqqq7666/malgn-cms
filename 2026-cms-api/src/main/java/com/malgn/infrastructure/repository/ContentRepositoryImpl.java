package com.malgn.infrastructure.repository;

import com.malgn.domain.model.Content;
import com.malgn.domain.repository.ContentRepositoryCustom;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.malgn.domain.model.QContent.content;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Content> search(ContentSearchCondition condition, Pageable pageable) {
        List<Content> contents = queryFactory
                .selectFrom(content)
                .where(
                        titleContains(condition.title()),
                        createdByEq(condition.createdBy())
                )
                .orderBy(content.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(content.count())
                .from(content)
                .where(
                        titleContains(condition.title()),
                        createdByEq(condition.createdBy())
                )
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    private BooleanExpression titleContains(String title) {
        if (StringUtils.hasText(title))
            return content.title.containsIgnoreCase(title);

        return null;
    }

    private BooleanExpression createdByEq(String createdBy) {
        if (StringUtils.hasText(createdBy))
            return content.createdBy.eq(createdBy);

        return null;
    }
}
