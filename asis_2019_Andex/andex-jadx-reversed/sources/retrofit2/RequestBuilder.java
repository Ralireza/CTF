package retrofit2;

import java.io.IOException;
import javax.annotation.Nullable;
import okhttp3.FormBody.Builder;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;

final class RequestBuilder {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String PATH_SEGMENT_ALWAYS_ENCODE_SET = " \"<>^`{}|\\?#";
    private final HttpUrl baseUrl;
    @Nullable
    private RequestBody body;
    @Nullable
    private MediaType contentType;
    @Nullable
    private Builder formBuilder;
    private final boolean hasBody;
    private final String method;
    @Nullable
    private MultipartBody.Builder multipartBuilder;
    @Nullable
    private String relativeUrl;
    private final Request.Builder requestBuilder = new Request.Builder();
    @Nullable
    private HttpUrl.Builder urlBuilder;

    private static class ContentTypeOverridingRequestBody extends RequestBody {
        private final MediaType contentType;
        private final RequestBody delegate;

        ContentTypeOverridingRequestBody(RequestBody requestBody, MediaType mediaType) {
            this.delegate = requestBody;
            this.contentType = mediaType;
        }

        public MediaType contentType() {
            return this.contentType;
        }

        public long contentLength() throws IOException {
            return this.delegate.contentLength();
        }

        public void writeTo(BufferedSink bufferedSink) throws IOException {
            this.delegate.writeTo(bufferedSink);
        }
    }

    RequestBuilder(String str, HttpUrl httpUrl, @Nullable String str2, @Nullable Headers headers, @Nullable MediaType mediaType, boolean z, boolean z2, boolean z3) {
        this.method = str;
        this.baseUrl = httpUrl;
        this.relativeUrl = str2;
        this.contentType = mediaType;
        this.hasBody = z;
        if (headers != null) {
            this.requestBuilder.headers(headers);
        }
        if (z2) {
            this.formBuilder = new Builder();
        } else if (z3) {
            this.multipartBuilder = new MultipartBody.Builder();
            this.multipartBuilder.setType(MultipartBody.FORM);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setRelativeUrl(Object obj) {
        this.relativeUrl = obj.toString();
    }

    /* access modifiers changed from: 0000 */
    public void addHeader(String str, String str2) {
        if ("Content-Type".equalsIgnoreCase(str)) {
            MediaType parse = MediaType.parse(str2);
            if (parse != null) {
                this.contentType = parse;
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Malformed content type: ");
            sb.append(str2);
            throw new IllegalArgumentException(sb.toString());
        }
        this.requestBuilder.addHeader(str, str2);
    }

    /* access modifiers changed from: 0000 */
    public void addPathParam(String str, String str2, boolean z) {
        String str3 = this.relativeUrl;
        if (str3 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(str);
            sb.append("}");
            this.relativeUrl = str3.replace(sb.toString(), canonicalizeForPath(str2, z));
            return;
        }
        throw new AssertionError();
    }

    private static String canonicalizeForPath(String str, boolean z) {
        int length = str.length();
        int i = 0;
        while (i < length) {
            int codePointAt = str.codePointAt(i);
            if (codePointAt < 32 || codePointAt >= 127 || PATH_SEGMENT_ALWAYS_ENCODE_SET.indexOf(codePointAt) != -1 || (!z && (codePointAt == 47 || codePointAt == 37))) {
                Buffer buffer = new Buffer();
                buffer.writeUtf8(str, 0, i);
                canonicalizeForPath(buffer, str, i, length, z);
                return buffer.readUtf8();
            }
            i += Character.charCount(codePointAt);
        }
        return str;
    }

    private static void canonicalizeForPath(Buffer buffer, String str, int i, int i2, boolean z) {
        Buffer buffer2 = null;
        while (i < i2) {
            int codePointAt = str.codePointAt(i);
            if (!z || !(codePointAt == 9 || codePointAt == 10 || codePointAt == 12 || codePointAt == 13)) {
                if (codePointAt < 32 || codePointAt >= 127 || PATH_SEGMENT_ALWAYS_ENCODE_SET.indexOf(codePointAt) != -1 || (!z && (codePointAt == 47 || codePointAt == 37))) {
                    if (buffer2 == null) {
                        buffer2 = new Buffer();
                    }
                    buffer2.writeUtf8CodePoint(codePointAt);
                    while (!buffer2.exhausted()) {
                        byte readByte = buffer2.readByte() & 255;
                        buffer.writeByte(37);
                        buffer.writeByte((int) HEX_DIGITS[(readByte >> 4) & 15]);
                        buffer.writeByte((int) HEX_DIGITS[readByte & 15]);
                    }
                } else {
                    buffer.writeUtf8CodePoint(codePointAt);
                }
            }
            i += Character.charCount(codePointAt);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addQueryParam(String str, @Nullable String str2, boolean z) {
        String str3 = this.relativeUrl;
        if (str3 != null) {
            this.urlBuilder = this.baseUrl.newBuilder(str3);
            if (this.urlBuilder != null) {
                this.relativeUrl = null;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Malformed URL. Base: ");
                sb.append(this.baseUrl);
                sb.append(", Relative: ");
                sb.append(this.relativeUrl);
                throw new IllegalArgumentException(sb.toString());
            }
        }
        if (z) {
            this.urlBuilder.addEncodedQueryParameter(str, str2);
        } else {
            this.urlBuilder.addQueryParameter(str, str2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addFormField(String str, String str2, boolean z) {
        if (z) {
            this.formBuilder.addEncoded(str, str2);
        } else {
            this.formBuilder.add(str, str2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addPart(Headers headers, RequestBody requestBody) {
        this.multipartBuilder.addPart(headers, requestBody);
    }

    /* access modifiers changed from: 0000 */
    public void addPart(Part part) {
        this.multipartBuilder.addPart(part);
    }

    /* access modifiers changed from: 0000 */
    public void setBody(RequestBody requestBody) {
        this.body = requestBody;
    }

    /* access modifiers changed from: 0000 */
    public Request build() {
        HttpUrl httpUrl;
        HttpUrl.Builder builder = this.urlBuilder;
        if (builder != null) {
            httpUrl = builder.build();
        } else {
            httpUrl = this.baseUrl.resolve(this.relativeUrl);
            if (httpUrl == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Malformed URL. Base: ");
                sb.append(this.baseUrl);
                sb.append(", Relative: ");
                sb.append(this.relativeUrl);
                throw new IllegalArgumentException(sb.toString());
            }
        }
        RequestBody requestBody = this.body;
        if (requestBody == null) {
            Builder builder2 = this.formBuilder;
            if (builder2 != null) {
                requestBody = builder2.build();
            } else {
                MultipartBody.Builder builder3 = this.multipartBuilder;
                if (builder3 != null) {
                    requestBody = builder3.build();
                } else if (this.hasBody) {
                    requestBody = RequestBody.create((MediaType) null, new byte[0]);
                }
            }
        }
        MediaType mediaType = this.contentType;
        if (mediaType != null) {
            if (requestBody != null) {
                requestBody = new ContentTypeOverridingRequestBody(requestBody, mediaType);
            } else {
                this.requestBuilder.addHeader("Content-Type", mediaType.toString());
            }
        }
        return this.requestBuilder.url(httpUrl).method(this.method, requestBody).build();
    }
}
