package org.mtcg.http;

import lombok.Getter;

@Getter
public enum ContentType
{
    // Content-Types
    HTML("text/html"),
    TEXT("text/plain"),
    JSON("application/json");

    private final String contentType;

    // Konstruktor für die Content-Types
    ContentType(String value)
    {
        this.contentType = value;
    }
}
