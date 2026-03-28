package com.malgn.unit;

import com.malgn.application.service.ContentService;
import com.malgn.domain.exception.AuthException;
import com.malgn.domain.exception.ContentException;
import com.malgn.domain.model.Content;
import com.malgn.domain.model.Member;
import com.malgn.domain.model.MemberRole;
import com.malgn.domain.repository.ContentRepository;
import com.malgn.presentation.dto.request.content.ContentCreateRequest;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import com.malgn.presentation.dto.request.content.ContentUpdateRequest;
import com.malgn.presentation.dto.response.PageResponse;
import com.malgn.presentation.dto.response.content.ContentCreateResponse;
import com.malgn.presentation.dto.response.content.ContentDetailResponse;
import com.malgn.presentation.dto.response.content.ContentListResponse;
import com.malgn.presentation.dto.response.content.ContentUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentService 단위 테스트")
public class ContentUnitTest {
    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    private Member owner;
    private Member anotherMember;
    private Member admin;

    @BeforeEach
    void setup() {
        owner = Member.builder()
                .id(1L)
                .username("owner")
                .password("passw0rd")
                .name("소유자")
                .role(MemberRole.MEMBER)
                .build();

        anotherMember = Member.builder()
                .id(2L)
                .username("other")
                .password("passw0rd")
                .name("타인")
                .role(MemberRole.MEMBER)
                .build();

        admin = Member.builder()
                .id(3L)
                .username("admin")
                .password("passw0rd")
                .name("관리자")
                .role(MemberRole.ADMIN)
                .build();
    }

    private Content buildContent(Long id, Member member) {
        return Content.builder()
                .id(id)
                .title("test title")
                .description("test description")
                .viewCount(0L)
                .member(member)
                .build();
    }

    @Nested
    @DisplayName("콘텐츠 생성")
    class Create {
        @Test
        @DisplayName("성공 - 제목과 내용이 정상 저장된다")
        void success() {
            ContentCreateRequest request = ContentCreateRequest.builder()
                    .title("new title")
                    .description("new description")
                    .build();
            Content saved = Content.createContent("new title", "new description", owner);

            given(contentRepository.save(any(Content.class)))
                    .willReturn(saved);

            ContentCreateResponse result = contentService.create(request, owner);

            assertThat(result).isNotNull();
            verify(contentRepository).save(any(Content.class));
        }

        @Test
        @DisplayName("성공 - description 없이 생성 가능")
        void success_withoutDescription() {
            ContentCreateRequest request = ContentCreateRequest.builder()
                    .title("new title")
                    .description(null)
                    .build();
            Content saved = Content.createContent("new title", "new description", owner);

            given(contentRepository.save(any(Content.class)))
                    .willReturn(saved);

            ContentCreateResponse result = contentService.create(request, owner);

            assertThat(result).isNotNull();
            verify(contentRepository).save(any(Content.class));
        }
    }

    @Nested
    @DisplayName("콘텐츠 목록 조회")
    class GetContents {
        @Test
        @DisplayName("성공 - 페이징된 목록을 반환한다")
        void success() {
            Content content0 = buildContent(1L, owner);
            Content content1 = buildContent(2L, anotherMember);
            Page<Content> page = new PageImpl<>(List.of(content0, content1), Pageable.ofSize(10), 2);

            given(contentRepository.findAll(any(Pageable.class)))
                    .willReturn(page);

            PageResponse<ContentListResponse> result = contentService.getContents(Pageable.ofSize(10));

            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
            verify(contentRepository).findAll(Pageable.ofSize(10));
        }

        @Test
        @DisplayName("성공 - 콘텐츠가 없으면 빈 목록을 반환한다")
        void success_emptyList() {
            Page<Content> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);

            given(contentRepository.findAll(Pageable.ofSize(10)))
                    .willReturn(emptyPage);

            PageResponse<ContentListResponse> result = contentService.getContents(Pageable.ofSize(10));

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("콘텐츠 검색")
    class Search {

        @Test
        @DisplayName("성공 - 제목 키워드로 검색하면 일치하는 콘텐츠만 반환된다")
        void success_searchByTitle() {
            Content content = buildContent(1L, owner);
            Page<Content> page = new PageImpl<>(List.of(content), Pageable.ofSize(10), 1);
            ContentSearchCondition condition = new ContentSearchCondition("테스트", null);

            given(contentRepository.search(condition, Pageable.ofSize(10)))
                    .willReturn(page);

            PageResponse<ContentListResponse> result = contentService.search(condition, Pageable.ofSize(10));

            assertThat(result.content()).hasSize(1);
            verify(contentRepository).search(condition, Pageable.ofSize(10));
        }

        @Test
        @DisplayName("성공 - 작성자로 검색하면 해당 작성자의 콘텐츠만 반환된다")
        void success_searchByCreatedBy() {
            Content content = buildContent(1L, owner);
            Page<Content> page = new PageImpl<>(List.of(content), Pageable.ofSize(10), 1);
            ContentSearchCondition condition = new ContentSearchCondition(null, "owner");

            given(contentRepository.search(condition, Pageable.ofSize(10)))
                    .willReturn(page);

            PageResponse<ContentListResponse> result = contentService.search(condition, Pageable.ofSize(10));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - 조건이 없으면 모든 결과가 반환된다")
        void success_noConditionReturnsEmpty() {
            Content content0 = buildContent(1L, owner);
            Content content1 = buildContent(2L, owner);
            Page<Content> page = new PageImpl<>(List.of(content0, content1), Pageable.ofSize(10), 0);
            ContentSearchCondition condition = new ContentSearchCondition(null, null);

            given(contentRepository.search(condition, Pageable.ofSize(10)))
                    .willReturn(page);

            PageResponse<ContentListResponse> result = contentService.search(condition, Pageable.ofSize(10));

            assertThat(result.content()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("콘텐츠 상세 조회")
    class GetContentDetail {

        @Test
        @DisplayName("성공 - 조회 시 viewCount가 1 증가한다")
        void success_viewCountIncreases() {
            Content content = buildContent(1L, owner);  // viewCount = 0
            given(contentRepository.findById(1L)).willReturn(Optional.of(content));

            ContentDetailResponse result = contentService.getContentDetail(1L);

            assertThat(result.viewCount()).isEqualTo(1L);
            verify(contentRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 조회된 콘텐츠의 전체 필드가 채워진다")
        void success_allFieldsPopulated() {
            Content content = buildContent(1L, owner);
            given(contentRepository.findById(1L)).willReturn(Optional.of(content));

            ContentDetailResponse result = contentService.getContentDetail(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("test title");
            assertThat(result.description()).isEqualTo("test description");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID 조회 시 ContentException 발생")
        void fail_contentNotFound() {
            given(contentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentService.getContentDetail(999L))
                    .isInstanceOf(ContentException.class);

            verify(contentRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("콘텐츠 수정")
    class Update {

        private ContentUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = ContentUpdateRequest.builder()
                    .title("updated title")
                    .description("updated title")
                    .build();
        }

        @Test
        @DisplayName("성공 - 본인 소유 콘텐츠를 수정할 수 있다")
        void success_ownerCanUpdate() {
            Content content = buildContent(1L, owner);

            given(contentRepository.findById(1L))
                    .willReturn(Optional.of(content));
            given(contentRepository.saveAndFlush(any(Content.class)))
                    .willReturn(content);

            ContentUpdateResponse result = contentService.update(1L, updateRequest, owner);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(contentRepository).findById(1L);
            verify(contentRepository).saveAndFlush(any(Content.class));
        }

        @Test
        @DisplayName("성공 - ADMIN은 타인 콘텐츠도 수정할 수 있다")
        void success_adminCanUpdateAnyContent() {
            Content content = buildContent(1L, owner);

            given(contentRepository.findById(1L))
                    .willReturn(Optional.of(content));
            given(contentRepository.saveAndFlush(any(Content.class)))
                    .willReturn(content);

            ContentUpdateResponse result = contentService.update(1L, updateRequest, admin);

            assertThat(result).isNotNull();
            verify(contentRepository).saveAndFlush(any(Content.class));
        }

        @Test
        @DisplayName("실패 - 타인 콘텐츠 수정 시 AuthException 발생")
        void fail_anotherMemberCannotUpdate() {
            Content content = buildContent(1L, owner);

            given(contentRepository.findById(1L))
                    .willReturn(Optional.of(content));

            assertThatThrownBy(() -> contentService.update(1L, updateRequest, anotherMember))
                    .isInstanceOf(AuthException.class);
            verify(contentRepository, never()).saveAndFlush(any(Content.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠 수정 시 ContentException 발생")
        void fail_contentNotFound() {
            given(contentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentService.update(999L, updateRequest, owner))
                    .isInstanceOf(ContentException.class);
            verify(contentRepository, never()).saveAndFlush(any(Content.class));
        }
    }
}
