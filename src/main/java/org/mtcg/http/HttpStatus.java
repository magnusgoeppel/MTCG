package org.mtcg.http;

import lombok.Getter;

@Getter
public enum HttpStatus
{
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String reason;

    HttpStatus(int code, String reason)
    {
        this.code = code;
        this.reason = reason;
    }
}
