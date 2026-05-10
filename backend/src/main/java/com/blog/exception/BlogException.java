package com.blog.exception;

import lombok.Getter;

@Getter
public class BlogException extends RuntimeException {
    private final Integer code;

    public BlogException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BlogException(String message) {
        super(message);
        this.code = 500;
    }
}
