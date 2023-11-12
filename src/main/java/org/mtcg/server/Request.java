package org.mtcg.server;

import org.mtcg.http.Method;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Request
{
    // Variablen für die HTTP-Request-Parameter
    private final Method method;
    private final String path;
    private final String version;
    private final String body;
    private final Map<String, String> headers;

    // Konstruktor für die HTTP-Request-Parameter
    public Request(Method method, String path, String version, String body, Map<String, String> headers)
    {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
    }
}
