package org.mtcg.http;

import lombok.Getter;

@Getter
public enum HttpStatus
{
    // Status-Codes
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String message;

    // Konstruktor für die HTTP-Status-Codes
    HttpStatus(int code, String message)
    {
        this.code = code;
        this.message = message;
    }
}
