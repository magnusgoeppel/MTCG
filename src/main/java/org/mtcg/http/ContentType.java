package org.mtcg.http;

import lombok.Getter;

@Getter
public enum ContentType
{
    HTML("text/html"),
    TEXT("text/plain"),
    JSON("application/json");

    private final String value;

    ContentType(String value)
    {
        this.value = value;
    }
}
