package org.mtcg.http;

import lombok.Getter;

@Getter
public class Response
{
    private final String version;
    private final HttpStatus status;
    private final String body;
    private final ContentType contentType;

    public Response(String version, HttpStatus status, String body, ContentType contentType)
    {
        this.version = version;
        this.status = status;
        this.body = body;
        this.contentType = contentType;
    }
}
