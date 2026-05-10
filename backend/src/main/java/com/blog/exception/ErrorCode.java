package com.blog.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INTERNAL_ERROR(500, "Internal server error"),
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    OLD_PASSWORD_INCORRECT(1002, "Old password is incorrect"),
    ARTICLE_NOT_FOUND(2001, "Article not found"),
    COMMENT_NOT_FOUND(3001, "Comment not found");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
