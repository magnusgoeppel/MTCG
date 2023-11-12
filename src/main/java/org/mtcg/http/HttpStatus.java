package org.mtcg.http;

import lombok.Getter;

@Getter
public enum HttpStatus
{
    // Status-Codes
    OK(200, "OK"),
    CREATED(201, "CREATED"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    CONFLICT(409, "Conflict");



    private final int code;
    private final String message;

    // Konstruktor für die HTTP-Status-Codes
    HttpStatus(int code, String message)
    {
        this.code = code;
        this.message = message;
    }
}
