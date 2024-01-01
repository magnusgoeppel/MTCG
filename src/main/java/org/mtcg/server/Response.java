package org.mtcg.server;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;

@Getter
@Setter(AccessLevel.PRIVATE)
public class Response
{
    // Variablen für die HTTP-Response-Parameter
    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String content;

    // Konstruktor für die HTTP-Response-Parameter
    public Response(HttpStatus httpStatus, ContentType contentType, String content)
    {
        setStatusCode(httpStatus.getCode());
        setContentType(contentType.getContentType());
        setStatusMessage(httpStatus.getMessage());
        setContent(content);
    }

    // Methode zum Erstellen der HTTP-Response
    protected String build()
    {
        return "HTTP/1.1 " + getStatusCode() + " " + getStatusMessage() + "\r\n" +
                "Content-Type: " + getContentType() + "\r\n" +
                "Content-Length: " + getContent().length() + "\r\n" +
                "\r\n" +
                getContent();
    }
}