package com.malgn.presentation.controller;

import com.malgn.application.service.ContentService;
import com.malgn.infrastructure.security.userdetails.CustomUserDetails;
import com.malgn.presentation.api.ContentApi;
import com.malgn.presentation.dto.request.content.ContentCreateRequest;
import com.malgn.presentation.dto.request.content.ContentSearchCondition;
import com.malgn.presentation.dto.request.content.ContentUpdateRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.PageResponse;
import com.malgn.presentation.dto.response.content.ContentCreateResponse;
import com.malgn.presentation.dto.response.content.ContentDetailResponse;
import com.malgn.presentation.dto.response.content.ContentListResponse;
import com.malgn.presentation.dto.response.content.ContentUpdateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
public class ContentController implements ContentApi {
    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContentCreateResponse>> create(
            @RequestBody
            @Valid
            ContentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ContentCreateResponse response = contentService.create(request, userDetails.getMember());

        return ResponseEntity.created(URI.create("/api/v1/contents/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentListResponse>>> getContents(
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getContents(pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ContentListResponse>>> search(
            @ModelAttribute ContentSearchCondition condition,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(contentService.search(condition, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getContentDetail(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentUpdateResponse>> update(
            @PathVariable Long id,
            @RequestBody
            @Valid
            ContentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(contentService.update(id, request, userDetails.getMember())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentService.delete(id, userDetails.getMember());
        return ResponseEntity.noContent()
                .build();
    }
}
