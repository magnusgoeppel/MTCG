package org.mtcg.http;

import lombok.Getter;

@Getter
public enum HttpStatus
{
    // Status-Codes
    OK(200, "OK"),
    CREATED(201, "CREATED"),
    NO_CONTENT(204, "NO CONTENT"),
    BAD_REQUEST(400, "BAD REQUEST"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    FORBIDDEN(403, "FORBIDDEN"),
    NOT_FOUND(404, "NOT FOUND"),
    CONFLICT(409, "CONFLICT"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR");

    // Variablen für die HTTP-Status-Codes
    private final int code;
    private final String message;

    // Konstruktor für die HTTP-Status-Codes
    HttpStatus(int code, String message)
    {
        this.code = code;
        this.message = message;
    }
}
