package de.rhm176.modmenu.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class HttpUtil {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static HttpResponse<JsonElement> httpGet(URI uri) throws IOException, InterruptedException {
        return httpGet(
                uri,
                responseInfo -> HttpResponse.BodySubscribers.mapping(
                        HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), JsonParser::parseString));
    }

    public static <T> HttpResponse<T> httpGet(URI uri, HttpResponse.BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException {
        return CLIENT.send(HttpRequest.newBuilder(uri).GET().build(), bodyHandler);
    }
}
