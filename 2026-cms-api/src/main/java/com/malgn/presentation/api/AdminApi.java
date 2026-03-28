package com.malgn.presentation.api;

import com.malgn.presentation.dto.request.member.MemberUpdateRoleRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.member.MemberUpdateRoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "03. 관리자", description = "시스템 관리자 전용 권한 제어 API")
public interface AdminApi {

    @Operation(
            summary = "회원 권한 변경",
            description = "특정 회원의 권한을 USER 또는 ADMIN으로 변경합니다.",
            operationId = "admin-01"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    ResponseEntity<ApiResponse<MemberUpdateRoleResponse>> updateRole(
            @Parameter(description = "대상 회원 ID", example = "1") @PathVariable Long id,
            @RequestBody MemberUpdateRoleRequest request
    );
}
