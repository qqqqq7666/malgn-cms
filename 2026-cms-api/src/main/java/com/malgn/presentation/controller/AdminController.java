package com.malgn.presentation.controller;

import com.malgn.application.service.AdminService;
import com.malgn.presentation.api.AdminApi;
import com.malgn.presentation.dto.request.member.MemberUpdateRoleRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.member.MemberUpdateRoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminApi {
    private final AdminService adminService;

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<MemberUpdateRoleResponse>> updateRole(
            @PathVariable Long id,
            @RequestBody
            @Valid
            MemberUpdateRoleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateRole(id, request)));
    }
}
