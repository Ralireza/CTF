package okhttp3.internal.http;

import java.io.IOException;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

public final class BridgeInterceptor implements Interceptor {
    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar2) {
        this.cookieJar = cookieJar2;
    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Builder newBuilder = request.newBuilder();
        RequestBody body = request.body();
        String str = "Content-Type";
        String str2 = "Content-Length";
        if (body != null) {
            MediaType contentType = body.contentType();
            if (contentType != null) {
                newBuilder.header(str, contentType.toString());
            }
            long contentLength = body.contentLength();
            String str3 = "Transfer-Encoding";
            if (contentLength != -1) {
                newBuilder.header(str2, Long.toString(contentLength));
                newBuilder.removeHeader(str3);
            } else {
                newBuilder.header(str3, "chunked");
                newBuilder.removeHeader(str2);
            }
        }
        String str4 = "Host";
        boolean z = false;
        if (request.header(str4) == null) {
            newBuilder.header(str4, Util.hostHeader(request.url(), false));
        }
        String str5 = "Connection";
        if (request.header(str5) == null) {
            newBuilder.header(str5, "Keep-Alive");
        }
        String str6 = "Accept-Encoding";
        String str7 = "gzip";
        if (request.header(str6) == null && request.header("Range") == null) {
            z = true;
            newBuilder.header(str6, str7);
        }
        List loadForRequest = this.cookieJar.loadForRequest(request.url());
        if (!loadForRequest.isEmpty()) {
            newBuilder.header("Cookie", cookieHeader(loadForRequest));
        }
        String str8 = "User-Agent";
        if (request.header(str8) == null) {
            newBuilder.header(str8, Version.userAgent());
        }
        Response proceed = chain.proceed(newBuilder.build());
        HttpHeaders.receiveHeaders(this.cookieJar, request.url(), proceed.headers());
        Response.Builder request2 = proceed.newBuilder().request(request);
        if (z) {
            String str9 = "Content-Encoding";
            if (str7.equalsIgnoreCase(proceed.header(str9)) && HttpHeaders.hasBody(proceed)) {
                GzipSource gzipSource = new GzipSource(proceed.body().source());
                request2.headers(proceed.headers().newBuilder().removeAll(str9).removeAll(str2).build());
                request2.body(new RealResponseBody(proceed.header(str), -1, Okio.buffer((Source) gzipSource)));
            }
        }
        return request2.build();
    }

    private String cookieHeader(List<Cookie> list) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append("; ");
            }
            Cookie cookie = (Cookie) list.get(i);
            sb.append(cookie.name());
            sb.append('=');
            sb.append(cookie.value());
        }
        return sb.toString();
    }
}
