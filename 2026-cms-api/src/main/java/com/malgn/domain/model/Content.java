package com.malgn.domain.model;

import com.malgn.presentation.dto.request.content.ContentUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

@Getter
@Entity
@Table(
        name = "contents",
        indexes = @Index(columnList = "id, created_date DESC")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Content extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private Long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Builder
    public Content(Long id, String title, String description, Long viewCount, Member member) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.viewCount = viewCount;
        this.member = member;
    }

    public static Content createContent(String title, String description, Member member) {
        return Content.builder()
                .title(title)
                .description(description)
                .viewCount(0L)
                .member(member)
                .build();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public boolean checkPermission(Member member) {
        return Objects.equals(this.member.getId(), member.getId());
    }
}