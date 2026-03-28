package com.malgn.presentation.api;

import com.malgn.infrastructure.security.userdetails.CustomUserDetails;
import com.malgn.presentation.dto.request.content.ContentCreateRequest;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import com.malgn.presentation.dto.request.content.ContentUpdateRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.PageResponse;
import com.malgn.presentation.dto.response.content.ContentCreateResponse;
import com.malgn.presentation.dto.response.content.ContentDetailResponse;
import com.malgn.presentation.dto.response.content.ContentListResponse;
import com.malgn.presentation.dto.response.content.ContentUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "02. 콘텐츠", description = "게시글 작성, 수정, 삭제 및 조회 API")
public interface ContentApi {

    @Operation(summary = "콘텐츠 생성", operationId = "content-01")
    ResponseEntity<ApiResponse<ContentCreateResponse>> create(
            @RequestBody ContentCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0번부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 조회할 데이터 개수 (기본값: 10)", example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 (기본값: createdDate, desc)", example = "createdDate,desc")
    })
    @Operation(summary = "콘텐츠 페이징 조회", description = "최신순 등으로 정렬된 콘텐츠 목록을 조회합니다.", operationId = "content-02")
    ResponseEntity<ApiResponse<PageResponse<ContentListResponse>>> getContents(
            @Parameter(hidden = true) Pageable pageable);

    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0번부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 조회할 데이터 개수 (기본값: 10)", example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 (기본값: createdDate, desc)", example = "createdDate,desc")
    })
    @Operation(summary = "콘텐츠 검색", description = "최신순 등으로 정렬된 콘텐츠 목록을 조회합니다.", operationId = "content-03")
    ResponseEntity<ApiResponse<PageResponse<ContentListResponse>>> search(
            @Parameter(description = "검색 조건 제목 contains, 사용자 이름 equal") ContentSearchCondition condition,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "콘텐츠 상세 조회", operationId = "content-04")
    ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(
            @Parameter(description = "콘텐츠 ID", example = "10") @PathVariable Long id);

    @Operation(summary = "콘텐츠 수정", description = "본인의 게시글만 수정 가능합니다.", operationId = "content-05")
    ResponseEntity<ApiResponse<ContentUpdateResponse>> update(
            @Parameter(description = "수정할 ID") @PathVariable Long id,
            @RequestBody ContentUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "콘텐츠 삭제", operationId = "content-06")
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
