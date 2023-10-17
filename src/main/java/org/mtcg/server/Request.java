package org.mtcg.server;

import org.mtcg.http.Method;
import lombok.Getter;

@Getter
public class Request
{
    private final Method method;
    private final String path;
    private final String version;
    private final String body;

    public Request(Method method, String path, String version, String body)
    {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
    }
}
