package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.UserPageResult;

/**
 * HTTP response body describing one page of users.
 */
@Schema(description = "One page of users")
public record UserPageResponse(

        @Schema(description = "Users on this page")
        List<UserResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Requested page size", example = "20")
        int size,

        @Schema(description = "Total number of users", example = "42")
        long totalElements,

        @Schema(description = "Total number of pages", example = "3")
        int totalPages,

        @Schema(description = "Whether a next page exists")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists")
        boolean hasPrevious
) {
    public static UserPageResponse from(UserPageResult result) {
        return new UserPageResponse(
                result.content().stream().map(UserResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}