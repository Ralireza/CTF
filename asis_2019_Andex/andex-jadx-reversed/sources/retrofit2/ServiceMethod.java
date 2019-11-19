package retrofit2;

import com.android.volley.toolbox.HttpClientStack.HttpPatch;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Call.Factory;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

final class ServiceMethod<R, T> {
    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_-]*)\\}");
    private final HttpUrl baseUrl;
    private final CallAdapter<R, T> callAdapter;
    private final Factory callFactory;
    private final MediaType contentType;
    private final boolean hasBody;
    private final Headers headers;
    private final String httpMethod;
    private final boolean isFormEncoded;
    private final boolean isMultipart;
    private final ParameterHandler<?>[] parameterHandlers;
    private final String relativeUrl;
    private final Converter<ResponseBody, R> responseConverter;

    static final class Builder<T, R> {
        CallAdapter<T, R> callAdapter;
        MediaType contentType;
        boolean gotBody;
        boolean gotField;
        boolean gotPart;
        boolean gotPath;
        boolean gotQuery;
        boolean gotUrl;
        boolean hasBody;
        Headers headers;
        String httpMethod;
        boolean isFormEncoded;
        boolean isMultipart;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        ParameterHandler<?>[] parameterHandlers;
        final Type[] parameterTypes;
        String relativeUrl;
        Set<String> relativeUrlParamNames;
        Converter<ResponseBody, T> responseConverter;
        Type responseType;
        final Retrofit retrofit;

        Builder(Retrofit retrofit3, Method method2) {
            this.retrofit = retrofit3;
            this.method = method2;
            this.methodAnnotations = method2.getAnnotations();
            this.parameterTypes = method2.getGenericParameterTypes();
            this.parameterAnnotationsArray = method2.getParameterAnnotations();
        }

        public ServiceMethod build() {
            this.callAdapter = createCallAdapter();
            this.responseType = this.callAdapter.responseType();
            Type type = this.responseType;
            if (type == Response.class || type == Response.class) {
                StringBuilder sb = new StringBuilder();
                sb.append("'");
                sb.append(Utils.getRawType(this.responseType).getName());
                sb.append("' is not a valid response body type. Did you mean ResponseBody?");
                throw methodError(sb.toString(), new Object[0]);
            }
            this.responseConverter = createResponseConverter();
            for (Annotation parseMethodAnnotation : this.methodAnnotations) {
                parseMethodAnnotation(parseMethodAnnotation);
            }
            if (this.httpMethod != null) {
                if (!this.hasBody) {
                    if (this.isMultipart) {
                        throw methodError("Multipart can only be specified on HTTP methods with request body (e.g., @POST).", new Object[0]);
                    } else if (this.isFormEncoded) {
                        throw methodError("FormUrlEncoded can only be specified on HTTP methods with request body (e.g., @POST).", new Object[0]);
                    }
                }
                int length = this.parameterAnnotationsArray.length;
                this.parameterHandlers = new ParameterHandler[length];
                int i = 0;
                while (i < length) {
                    Type type2 = this.parameterTypes[i];
                    if (!Utils.hasUnresolvableType(type2)) {
                        Annotation[] annotationArr = this.parameterAnnotationsArray[i];
                        if (annotationArr != null) {
                            this.parameterHandlers[i] = parseParameter(i, type2, annotationArr);
                            i++;
                        } else {
                            throw parameterError(i, "No Retrofit annotation found.", new Object[0]);
                        }
                    } else {
                        throw parameterError(i, "Parameter type must not include a type variable or wildcard: %s", type2);
                    }
                }
                if (this.relativeUrl == null && !this.gotUrl) {
                    throw methodError("Missing either @%s URL or @Url parameter.", this.httpMethod);
                } else if (!this.isFormEncoded && !this.isMultipart && !this.hasBody && this.gotBody) {
                    throw methodError("Non-body HTTP method cannot contain @Body.", new Object[0]);
                } else if (this.isFormEncoded && !this.gotField) {
                    throw methodError("Form-encoded method must contain at least one @Field.", new Object[0]);
                } else if (!this.isMultipart || this.gotPart) {
                    return new ServiceMethod(this);
                } else {
                    throw methodError("Multipart method must contain at least one @Part.", new Object[0]);
                }
            } else {
                throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).", new Object[0]);
            }
        }

        private CallAdapter<T, R> createCallAdapter() {
            Type genericReturnType = this.method.getGenericReturnType();
            if (Utils.hasUnresolvableType(genericReturnType)) {
                throw methodError("Method return type must not include a type variable or wildcard: %s", genericReturnType);
            } else if (genericReturnType != Void.TYPE) {
                try {
                    return this.retrofit.callAdapter(genericReturnType, this.method.getAnnotations());
                } catch (RuntimeException e) {
                    throw methodError(e, "Unable to create call adapter for %s", genericReturnType);
                }
            } else {
                throw methodError("Service methods cannot return void.", new Object[0]);
            }
        }

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof DELETE) {
                parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
            } else if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof HEAD) {
                parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
                if (!Void.class.equals(this.responseType)) {
                    throw methodError("HEAD method must use Void as response type.", new Object[0]);
                }
            } else if (annotation instanceof PATCH) {
                parseHttpMethodAndPath(HttpPatch.METHOD_NAME, ((PATCH) annotation).value(), true);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            } else if (annotation instanceof PUT) {
                parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
            } else if (annotation instanceof OPTIONS) {
                parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
            } else if (annotation instanceof HTTP) {
                HTTP http = (HTTP) annotation;
                parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
            } else if (annotation instanceof retrofit2.http.Headers) {
                String[] value = ((retrofit2.http.Headers) annotation).value();
                if (value.length != 0) {
                    this.headers = parseHeaders(value);
                } else {
                    throw methodError("@Headers annotation is empty.", new Object[0]);
                }
            } else {
                String str = "Only one encoding annotation is allowed.";
                if (annotation instanceof Multipart) {
                    if (!this.isFormEncoded) {
                        this.isMultipart = true;
                        return;
                    }
                    throw methodError(str, new Object[0]);
                } else if (!(annotation instanceof FormUrlEncoded)) {
                } else {
                    if (!this.isMultipart) {
                        this.isFormEncoded = true;
                        return;
                    }
                    throw methodError(str, new Object[0]);
                }
            }
        }

        private void parseHttpMethodAndPath(String str, String str2, boolean z) {
            String str3 = this.httpMethod;
            if (str3 == null) {
                this.httpMethod = str;
                this.hasBody = z;
                if (!str2.isEmpty()) {
                    int indexOf = str2.indexOf(63);
                    if (indexOf != -1 && indexOf < str2.length() - 1) {
                        String substring = str2.substring(indexOf + 1);
                        if (ServiceMethod.PARAM_URL_REGEX.matcher(substring).find()) {
                            throw methodError("URL query string \"%s\" must not have replace block. For dynamic query parameters use @Query.", substring);
                        }
                    }
                    this.relativeUrl = str2;
                    this.relativeUrlParamNames = ServiceMethod.parsePathParameters(str2);
                    return;
                }
                return;
            }
            throw methodError("Only one HTTP method is allowed. Found: %s and %s.", str3, str);
        }

        private Headers parseHeaders(String[] strArr) {
            okhttp3.Headers.Builder builder = new okhttp3.Headers.Builder();
            for (String str : strArr) {
                int indexOf = str.indexOf(58);
                if (indexOf == -1 || indexOf == 0 || indexOf == str.length() - 1) {
                    throw methodError("@Headers value must be in the form \"Name: Value\". Found: \"%s\"", str);
                }
                String substring = str.substring(0, indexOf);
                String trim = str.substring(indexOf + 1).trim();
                if ("Content-Type".equalsIgnoreCase(substring)) {
                    MediaType parse = MediaType.parse(trim);
                    if (parse != null) {
                        this.contentType = parse;
                    } else {
                        throw methodError("Malformed content type: %s", trim);
                    }
                } else {
                    builder.add(substring, trim);
                }
            }
            return builder.build();
        }

        private ParameterHandler<?> parseParameter(int i, Type type, Annotation[] annotationArr) {
            ParameterHandler<?> parameterHandler = null;
            for (Annotation parseParameterAnnotation : annotationArr) {
                ParameterHandler<?> parseParameterAnnotation2 = parseParameterAnnotation(i, type, annotationArr, parseParameterAnnotation);
                if (parseParameterAnnotation2 != null) {
                    if (parameterHandler == null) {
                        parameterHandler = parseParameterAnnotation2;
                    } else {
                        throw parameterError(i, "Multiple Retrofit annotations found, only one allowed.", new Object[0]);
                    }
                }
            }
            if (parameterHandler != null) {
                return parameterHandler;
            }
            throw parameterError(i, "No Retrofit annotation found.", new Object[0]);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0036, code lost:
            if ("android.net.Uri".equals(((java.lang.Class) r11).getName()) != false) goto L_0x0042;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private retrofit2.ParameterHandler<?> parseParameterAnnotation(int r10, java.lang.reflect.Type r11, java.lang.annotation.Annotation[] r12, java.lang.annotation.Annotation r13) {
            /*
                r9 = this;
                boolean r0 = r13 instanceof retrofit2.http.Url
                java.lang.String r1 = "@Path parameters may not be used with @Url."
                r2 = 1
                r3 = 0
                if (r0 == 0) goto L_0x006e
                boolean r12 = r9.gotUrl
                if (r12 != 0) goto L_0x0065
                boolean r12 = r9.gotPath
                if (r12 != 0) goto L_0x005e
                boolean r12 = r9.gotQuery
                if (r12 != 0) goto L_0x0055
                java.lang.String r12 = r9.relativeUrl
                if (r12 != 0) goto L_0x0048
                r9.gotUrl = r2
                java.lang.Class<okhttp3.HttpUrl> r12 = okhttp3.HttpUrl.class
                if (r11 == r12) goto L_0x0042
                java.lang.Class<java.lang.String> r12 = java.lang.String.class
                if (r11 == r12) goto L_0x0042
                java.lang.Class<java.net.URI> r12 = java.net.URI.class
                if (r11 == r12) goto L_0x0042
                boolean r12 = r11 instanceof java.lang.Class
                if (r12 == 0) goto L_0x0039
                java.lang.Class r11 = (java.lang.Class) r11
                java.lang.String r11 = r11.getName()
                java.lang.String r12 = "android.net.Uri"
                boolean r11 = r12.equals(r11)
                if (r11 == 0) goto L_0x0039
                goto L_0x0042
            L_0x0039:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@Url must be okhttp3.HttpUrl, String, java.net.URI, or android.net.Uri type."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x0042:
                retrofit2.ParameterHandler$RelativeUrl r10 = new retrofit2.ParameterHandler$RelativeUrl
                r10.<init>()
                return r10
            L_0x0048:
                java.lang.Object[] r11 = new java.lang.Object[r2]
                java.lang.String r12 = r9.httpMethod
                r11[r3] = r12
                java.lang.String r12 = "@Url cannot be used with @%s URL"
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x0055:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "A @Url parameter must not come after a @Query"
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x005e:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r1, r11)
                throw r10
            L_0x0065:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "Multiple @Url method annotations found."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x006e:
                boolean r0 = r13 instanceof retrofit2.http.Path
                if (r0 == 0) goto L_0x00b6
                boolean r0 = r9.gotQuery
                if (r0 != 0) goto L_0x00ad
                boolean r0 = r9.gotUrl
                if (r0 != 0) goto L_0x00a6
                java.lang.String r0 = r9.relativeUrl
                if (r0 == 0) goto L_0x0099
                r9.gotPath = r2
                retrofit2.http.Path r13 = (retrofit2.http.Path) r13
                java.lang.String r0 = r13.value()
                r9.validatePathName(r10, r0)
                retrofit2.Retrofit r10 = r9.retrofit
                retrofit2.Converter r10 = r10.stringConverter(r11, r12)
                retrofit2.ParameterHandler$Path r11 = new retrofit2.ParameterHandler$Path
                boolean r12 = r13.encoded()
                r11.<init>(r0, r10, r12)
                return r11
            L_0x0099:
                java.lang.Object[] r11 = new java.lang.Object[r2]
                java.lang.String r12 = r9.httpMethod
                r11[r3] = r12
                java.lang.String r12 = "@Path can only be used with relative url on @%s"
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x00a6:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r1, r11)
                throw r10
            L_0x00ad:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "A @Path parameter must not come after a @Query."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x00b6:
                boolean r0 = r13 instanceof retrofit2.http.Query
                java.lang.String r1 = "<String>)"
                java.lang.String r4 = " must include generic type (e.g., "
                if (r0 == 0) goto L_0x013e
                retrofit2.http.Query r13 = (retrofit2.http.Query) r13
                java.lang.String r0 = r13.value()
                boolean r13 = r13.encoded()
                java.lang.Class r5 = retrofit2.Utils.getRawType(r11)
                r9.gotQuery = r2
                java.lang.Class<java.lang.Iterable> r2 = java.lang.Iterable.class
                boolean r2 = r2.isAssignableFrom(r5)
                if (r2 == 0) goto L_0x0114
                boolean r2 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r2 == 0) goto L_0x00f0
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Query r11 = new retrofit2.ParameterHandler$Query
                r11.<init>(r0, r10, r13)
                retrofit2.ParameterHandler r10 = r11.iterable()
                return r10
            L_0x00f0:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x0114:
                boolean r10 = r5.isArray()
                if (r10 == 0) goto L_0x0132
                java.lang.Class r10 = r5.getComponentType()
                java.lang.Class r10 = retrofit2.ServiceMethod.boxIfPrimitive(r10)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Query r11 = new retrofit2.ParameterHandler$Query
                r11.<init>(r0, r10, r13)
                retrofit2.ParameterHandler r10 = r11.array()
                return r10
            L_0x0132:
                retrofit2.Retrofit r10 = r9.retrofit
                retrofit2.Converter r10 = r10.stringConverter(r11, r12)
                retrofit2.ParameterHandler$Query r11 = new retrofit2.ParameterHandler$Query
                r11.<init>(r0, r10, r13)
                return r11
            L_0x013e:
                boolean r0 = r13 instanceof retrofit2.http.QueryName
                if (r0 == 0) goto L_0x01be
                retrofit2.http.QueryName r13 = (retrofit2.http.QueryName) r13
                boolean r13 = r13.encoded()
                java.lang.Class r0 = retrofit2.Utils.getRawType(r11)
                r9.gotQuery = r2
                java.lang.Class<java.lang.Iterable> r2 = java.lang.Iterable.class
                boolean r2 = r2.isAssignableFrom(r0)
                if (r2 == 0) goto L_0x0194
                boolean r2 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r2 == 0) goto L_0x0170
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$QueryName r11 = new retrofit2.ParameterHandler$QueryName
                r11.<init>(r10, r13)
                retrofit2.ParameterHandler r10 = r11.iterable()
                return r10
            L_0x0170:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r0.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r0.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x0194:
                boolean r10 = r0.isArray()
                if (r10 == 0) goto L_0x01b2
                java.lang.Class r10 = r0.getComponentType()
                java.lang.Class r10 = retrofit2.ServiceMethod.boxIfPrimitive(r10)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$QueryName r11 = new retrofit2.ParameterHandler$QueryName
                r11.<init>(r10, r13)
                retrofit2.ParameterHandler r10 = r11.array()
                return r10
            L_0x01b2:
                retrofit2.Retrofit r10 = r9.retrofit
                retrofit2.Converter r10 = r10.stringConverter(r11, r12)
                retrofit2.ParameterHandler$QueryName r11 = new retrofit2.ParameterHandler$QueryName
                r11.<init>(r10, r13)
                return r11
            L_0x01be:
                boolean r0 = r13 instanceof retrofit2.http.QueryMap
                java.lang.String r5 = "Map must include generic types (e.g., Map<String, String>)"
                if (r0 == 0) goto L_0x0222
                java.lang.Class r0 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                boolean r1 = r1.isAssignableFrom(r0)
                if (r1 == 0) goto L_0x0219
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                java.lang.reflect.Type r11 = retrofit2.Utils.getSupertype(r11, r0, r1)
                boolean r0 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r0 == 0) goto L_0x0212
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r0 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<java.lang.String> r1 = java.lang.String.class
                if (r1 != r0) goto L_0x01fa
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r2, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$QueryMap r11 = new retrofit2.ParameterHandler$QueryMap
                retrofit2.http.QueryMap r13 = (retrofit2.http.QueryMap) r13
                boolean r12 = r13.encoded()
                r11.<init>(r10, r12)
                return r11
            L_0x01fa:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = "@QueryMap keys must be of type String: "
                r11.append(r12)
                r11.append(r0)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x0212:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r5, r11)
                throw r10
            L_0x0219:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@QueryMap parameter type must be Map."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x0222:
                boolean r0 = r13 instanceof retrofit2.http.Header
                if (r0 == 0) goto L_0x02a0
                retrofit2.http.Header r13 = (retrofit2.http.Header) r13
                java.lang.String r13 = r13.value()
                java.lang.Class r0 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.lang.Iterable> r2 = java.lang.Iterable.class
                boolean r2 = r2.isAssignableFrom(r0)
                if (r2 == 0) goto L_0x0276
                boolean r2 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r2 == 0) goto L_0x0252
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Header r11 = new retrofit2.ParameterHandler$Header
                r11.<init>(r13, r10)
                retrofit2.ParameterHandler r10 = r11.iterable()
                return r10
            L_0x0252:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r0.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r0.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x0276:
                boolean r10 = r0.isArray()
                if (r10 == 0) goto L_0x0294
                java.lang.Class r10 = r0.getComponentType()
                java.lang.Class r10 = retrofit2.ServiceMethod.boxIfPrimitive(r10)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Header r11 = new retrofit2.ParameterHandler$Header
                r11.<init>(r13, r10)
                retrofit2.ParameterHandler r10 = r11.array()
                return r10
            L_0x0294:
                retrofit2.Retrofit r10 = r9.retrofit
                retrofit2.Converter r10 = r10.stringConverter(r11, r12)
                retrofit2.ParameterHandler$Header r11 = new retrofit2.ParameterHandler$Header
                r11.<init>(r13, r10)
                return r11
            L_0x02a0:
                boolean r0 = r13 instanceof retrofit2.http.HeaderMap
                if (r0 == 0) goto L_0x02fc
                java.lang.Class r13 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.util.Map> r0 = java.util.Map.class
                boolean r0 = r0.isAssignableFrom(r13)
                if (r0 == 0) goto L_0x02f3
                java.lang.Class<java.util.Map> r0 = java.util.Map.class
                java.lang.reflect.Type r11 = retrofit2.Utils.getSupertype(r11, r13, r0)
                boolean r13 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r13 == 0) goto L_0x02ec
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r13 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<java.lang.String> r0 = java.lang.String.class
                if (r0 != r13) goto L_0x02d4
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r2, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$HeaderMap r11 = new retrofit2.ParameterHandler$HeaderMap
                r11.<init>(r10)
                return r11
            L_0x02d4:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = "@HeaderMap keys must be of type String: "
                r11.append(r12)
                r11.append(r13)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x02ec:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r5, r11)
                throw r10
            L_0x02f3:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@HeaderMap parameter type must be Map."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x02fc:
                boolean r0 = r13 instanceof retrofit2.http.Field
                if (r0 == 0) goto L_0x038d
                boolean r0 = r9.isFormEncoded
                if (r0 == 0) goto L_0x0384
                retrofit2.http.Field r13 = (retrofit2.http.Field) r13
                java.lang.String r0 = r13.value()
                boolean r13 = r13.encoded()
                r9.gotField = r2
                java.lang.Class r2 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.lang.Iterable> r5 = java.lang.Iterable.class
                boolean r5 = r5.isAssignableFrom(r2)
                if (r5 == 0) goto L_0x035a
                boolean r5 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r5 == 0) goto L_0x0336
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Field r11 = new retrofit2.ParameterHandler$Field
                r11.<init>(r0, r10, r13)
                retrofit2.ParameterHandler r10 = r11.iterable()
                return r10
            L_0x0336:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r2.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r2.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x035a:
                boolean r10 = r2.isArray()
                if (r10 == 0) goto L_0x0378
                java.lang.Class r10 = r2.getComponentType()
                java.lang.Class r10 = retrofit2.ServiceMethod.boxIfPrimitive(r10)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                retrofit2.ParameterHandler$Field r11 = new retrofit2.ParameterHandler$Field
                r11.<init>(r0, r10, r13)
                retrofit2.ParameterHandler r10 = r11.array()
                return r10
            L_0x0378:
                retrofit2.Retrofit r10 = r9.retrofit
                retrofit2.Converter r10 = r10.stringConverter(r11, r12)
                retrofit2.ParameterHandler$Field r11 = new retrofit2.ParameterHandler$Field
                r11.<init>(r0, r10, r13)
                return r11
            L_0x0384:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@Field parameters can only be used with form encoding."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x038d:
                boolean r0 = r13 instanceof retrofit2.http.FieldMap
                if (r0 == 0) goto L_0x03fe
                boolean r0 = r9.isFormEncoded
                if (r0 == 0) goto L_0x03f5
                java.lang.Class r0 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                boolean r1 = r1.isAssignableFrom(r0)
                if (r1 == 0) goto L_0x03ec
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                java.lang.reflect.Type r11 = retrofit2.Utils.getSupertype(r11, r0, r1)
                boolean r0 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r0 == 0) goto L_0x03e5
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r0 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<java.lang.String> r1 = java.lang.String.class
                if (r1 != r0) goto L_0x03cd
                java.lang.reflect.Type r10 = retrofit2.Utils.getParameterUpperBound(r2, r11)
                retrofit2.Retrofit r11 = r9.retrofit
                retrofit2.Converter r10 = r11.stringConverter(r10, r12)
                r9.gotField = r2
                retrofit2.ParameterHandler$FieldMap r11 = new retrofit2.ParameterHandler$FieldMap
                retrofit2.http.FieldMap r13 = (retrofit2.http.FieldMap) r13
                boolean r12 = r13.encoded()
                r11.<init>(r10, r12)
                return r11
            L_0x03cd:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = "@FieldMap keys must be of type String: "
                r11.append(r12)
                r11.append(r0)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x03e5:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r5, r11)
                throw r10
            L_0x03ec:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@FieldMap parameter type must be Map."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x03f5:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@FieldMap parameters can only be used with form encoding."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x03fe:
                boolean r0 = r13 instanceof retrofit2.http.Part
                if (r0 == 0) goto L_0x057d
                boolean r0 = r9.isMultipart
                if (r0 == 0) goto L_0x0574
                retrofit2.http.Part r13 = (retrofit2.http.Part) r13
                r9.gotPart = r2
                java.lang.String r0 = r13.value()
                java.lang.Class r5 = retrofit2.Utils.getRawType(r11)
                boolean r6 = r0.isEmpty()
                if (r6 == 0) goto L_0x049c
                java.lang.Class<java.lang.Iterable> r12 = java.lang.Iterable.class
                boolean r12 = r12.isAssignableFrom(r5)
                java.lang.String r13 = "@Part annotation must supply a name or use MultipartBody.Part parameter type."
                if (r12 == 0) goto L_0x046a
                boolean r12 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r12 == 0) goto L_0x0446
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r11 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<okhttp3.MultipartBody$Part> r12 = okhttp3.MultipartBody.Part.class
                java.lang.Class r11 = retrofit2.Utils.getRawType(r11)
                boolean r11 = r12.isAssignableFrom(r11)
                if (r11 == 0) goto L_0x043f
                retrofit2.ParameterHandler$RawPart r10 = retrofit2.ParameterHandler.RawPart.INSTANCE
                retrofit2.ParameterHandler r10 = r10.iterable()
                return r10
            L_0x043f:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r13, r11)
                throw r10
            L_0x0446:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x046a:
                boolean r11 = r5.isArray()
                if (r11 == 0) goto L_0x048a
                java.lang.Class r11 = r5.getComponentType()
                java.lang.Class<okhttp3.MultipartBody$Part> r12 = okhttp3.MultipartBody.Part.class
                boolean r11 = r12.isAssignableFrom(r11)
                if (r11 == 0) goto L_0x0483
                retrofit2.ParameterHandler$RawPart r10 = retrofit2.ParameterHandler.RawPart.INSTANCE
                retrofit2.ParameterHandler r10 = r10.array()
                return r10
            L_0x0483:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r13, r11)
                throw r10
            L_0x048a:
                java.lang.Class<okhttp3.MultipartBody$Part> r11 = okhttp3.MultipartBody.Part.class
                boolean r11 = r11.isAssignableFrom(r5)
                if (r11 == 0) goto L_0x0495
                retrofit2.ParameterHandler$RawPart r10 = retrofit2.ParameterHandler.RawPart.INSTANCE
                return r10
            L_0x0495:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r13, r11)
                throw r10
            L_0x049c:
                r6 = 4
                java.lang.String[] r6 = new java.lang.String[r6]
                java.lang.String r7 = "Content-Disposition"
                r6[r3] = r7
                java.lang.StringBuilder r7 = new java.lang.StringBuilder
                r7.<init>()
                java.lang.String r8 = "form-data; name=\""
                r7.append(r8)
                r7.append(r0)
                java.lang.String r0 = "\""
                r7.append(r0)
                java.lang.String r0 = r7.toString()
                r6[r2] = r0
                r0 = 2
                java.lang.String r2 = "Content-Transfer-Encoding"
                r6[r0] = r2
                r0 = 3
                java.lang.String r13 = r13.encoding()
                r6[r0] = r13
                okhttp3.Headers r13 = okhttp3.Headers.of(r6)
                java.lang.Class<java.lang.Iterable> r0 = java.lang.Iterable.class
                boolean r0 = r0.isAssignableFrom(r5)
                java.lang.String r2 = "@Part parameters using the MultipartBody.Part must not include a part name in the annotation."
                if (r0 == 0) goto L_0x0528
                boolean r0 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r0 == 0) goto L_0x0504
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r11 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<okhttp3.MultipartBody$Part> r0 = okhttp3.MultipartBody.Part.class
                java.lang.Class r1 = retrofit2.Utils.getRawType(r11)
                boolean r0 = r0.isAssignableFrom(r1)
                if (r0 != 0) goto L_0x04fd
                retrofit2.Retrofit r10 = r9.retrofit
                java.lang.annotation.Annotation[] r0 = r9.methodAnnotations
                retrofit2.Converter r10 = r10.requestBodyConverter(r11, r12, r0)
                retrofit2.ParameterHandler$Part r11 = new retrofit2.ParameterHandler$Part
                r11.<init>(r13, r10)
                retrofit2.ParameterHandler r10 = r11.iterable()
                return r10
            L_0x04fd:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r2, r11)
                throw r10
            L_0x0504:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r4)
                java.lang.String r12 = r5.getSimpleName()
                r11.append(r12)
                r11.append(r1)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x0528:
                boolean r0 = r5.isArray()
                if (r0 == 0) goto L_0x0557
                java.lang.Class r11 = r5.getComponentType()
                java.lang.Class r11 = retrofit2.ServiceMethod.boxIfPrimitive(r11)
                java.lang.Class<okhttp3.MultipartBody$Part> r0 = okhttp3.MultipartBody.Part.class
                boolean r0 = r0.isAssignableFrom(r11)
                if (r0 != 0) goto L_0x0550
                retrofit2.Retrofit r10 = r9.retrofit
                java.lang.annotation.Annotation[] r0 = r9.methodAnnotations
                retrofit2.Converter r10 = r10.requestBodyConverter(r11, r12, r0)
                retrofit2.ParameterHandler$Part r11 = new retrofit2.ParameterHandler$Part
                r11.<init>(r13, r10)
                retrofit2.ParameterHandler r10 = r11.array()
                return r10
            L_0x0550:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r2, r11)
                throw r10
            L_0x0557:
                java.lang.Class<okhttp3.MultipartBody$Part> r0 = okhttp3.MultipartBody.Part.class
                boolean r0 = r0.isAssignableFrom(r5)
                if (r0 != 0) goto L_0x056d
                retrofit2.Retrofit r10 = r9.retrofit
                java.lang.annotation.Annotation[] r0 = r9.methodAnnotations
                retrofit2.Converter r10 = r10.requestBodyConverter(r11, r12, r0)
                retrofit2.ParameterHandler$Part r11 = new retrofit2.ParameterHandler$Part
                r11.<init>(r13, r10)
                return r11
            L_0x056d:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r2, r11)
                throw r10
            L_0x0574:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@Part parameters can only be used with multipart encoding."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x057d:
                boolean r0 = r13 instanceof retrofit2.http.PartMap
                if (r0 == 0) goto L_0x0605
                boolean r0 = r9.isMultipart
                if (r0 == 0) goto L_0x05fc
                r9.gotPart = r2
                java.lang.Class r0 = retrofit2.Utils.getRawType(r11)
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                boolean r1 = r1.isAssignableFrom(r0)
                if (r1 == 0) goto L_0x05f3
                java.lang.Class<java.util.Map> r1 = java.util.Map.class
                java.lang.reflect.Type r11 = retrofit2.Utils.getSupertype(r11, r0, r1)
                boolean r0 = r11 instanceof java.lang.reflect.ParameterizedType
                if (r0 == 0) goto L_0x05ec
                java.lang.reflect.ParameterizedType r11 = (java.lang.reflect.ParameterizedType) r11
                java.lang.reflect.Type r0 = retrofit2.Utils.getParameterUpperBound(r3, r11)
                java.lang.Class<java.lang.String> r1 = java.lang.String.class
                if (r1 != r0) goto L_0x05d4
                java.lang.reflect.Type r11 = retrofit2.Utils.getParameterUpperBound(r2, r11)
                java.lang.Class<okhttp3.MultipartBody$Part> r0 = okhttp3.MultipartBody.Part.class
                java.lang.Class r1 = retrofit2.Utils.getRawType(r11)
                boolean r0 = r0.isAssignableFrom(r1)
                if (r0 != 0) goto L_0x05cb
                retrofit2.Retrofit r10 = r9.retrofit
                java.lang.annotation.Annotation[] r0 = r9.methodAnnotations
                retrofit2.Converter r10 = r10.requestBodyConverter(r11, r12, r0)
                retrofit2.http.PartMap r13 = (retrofit2.http.PartMap) r13
                retrofit2.ParameterHandler$PartMap r11 = new retrofit2.ParameterHandler$PartMap
                java.lang.String r12 = r13.encoding()
                r11.<init>(r10, r12)
                return r11
            L_0x05cb:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@PartMap values cannot be MultipartBody.Part. Use @Part List<Part> or a different value type instead."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x05d4:
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                r11.<init>()
                java.lang.String r12 = "@PartMap keys must be of type String: "
                r11.append(r12)
                r11.append(r0)
                java.lang.String r11 = r11.toString()
                java.lang.Object[] r12 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r11, r12)
                throw r10
            L_0x05ec:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.RuntimeException r10 = r9.parameterError(r10, r5, r11)
                throw r10
            L_0x05f3:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@PartMap parameter type must be Map."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x05fc:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@PartMap parameters can only be used with multipart encoding."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x0605:
                boolean r13 = r13 instanceof retrofit2.http.Body
                if (r13 == 0) goto L_0x0643
                boolean r13 = r9.isFormEncoded
                if (r13 != 0) goto L_0x063a
                boolean r13 = r9.isMultipart
                if (r13 != 0) goto L_0x063a
                boolean r13 = r9.gotBody
                if (r13 != 0) goto L_0x0631
                retrofit2.Retrofit r13 = r9.retrofit     // Catch:{ RuntimeException -> 0x0625 }
                java.lang.annotation.Annotation[] r0 = r9.methodAnnotations     // Catch:{ RuntimeException -> 0x0625 }
                retrofit2.Converter r10 = r13.requestBodyConverter(r11, r12, r0)     // Catch:{ RuntimeException -> 0x0625 }
                r9.gotBody = r2
                retrofit2.ParameterHandler$Body r11 = new retrofit2.ParameterHandler$Body
                r11.<init>(r10)
                return r11
            L_0x0625:
                r12 = move-exception
                java.lang.Object[] r13 = new java.lang.Object[r2]
                r13[r3] = r11
                java.lang.String r11 = "Unable to create @Body converter for %s"
                java.lang.RuntimeException r10 = r9.parameterError(r12, r10, r11, r13)
                throw r10
            L_0x0631:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "Multiple @Body method annotations found."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x063a:
                java.lang.Object[] r11 = new java.lang.Object[r3]
                java.lang.String r12 = "@Body parameters cannot be used with form or multi-part encoding."
                java.lang.RuntimeException r10 = r9.parameterError(r10, r12, r11)
                throw r10
            L_0x0643:
                r10 = 0
                return r10
            */
            throw new UnsupportedOperationException("Method not decompiled: retrofit2.ServiceMethod.Builder.parseParameterAnnotation(int, java.lang.reflect.Type, java.lang.annotation.Annotation[], java.lang.annotation.Annotation):retrofit2.ParameterHandler");
        }

        private void validatePathName(int i, String str) {
            if (!ServiceMethod.PARAM_NAME_REGEX.matcher(str).matches()) {
                throw parameterError(i, "@Path parameter name must match %s. Found: %s", ServiceMethod.PARAM_URL_REGEX.pattern(), str);
            } else if (!this.relativeUrlParamNames.contains(str)) {
                throw parameterError(i, "URL \"%s\" does not contain \"{%s}\".", this.relativeUrl, str);
            }
        }

        private Converter<ResponseBody, T> createResponseConverter() {
            try {
                return this.retrofit.responseBodyConverter(this.responseType, this.method.getAnnotations());
            } catch (RuntimeException e) {
                throw methodError(e, "Unable to create converter for %s", this.responseType);
            }
        }

        private RuntimeException methodError(String str, Object... objArr) {
            return methodError(null, str, objArr);
        }

        private RuntimeException methodError(Throwable th, String str, Object... objArr) {
            String format = String.format(str, objArr);
            StringBuilder sb = new StringBuilder();
            sb.append(format);
            sb.append("\n    for method ");
            sb.append(this.method.getDeclaringClass().getSimpleName());
            sb.append(".");
            sb.append(this.method.getName());
            return new IllegalArgumentException(sb.toString(), th);
        }

        private RuntimeException parameterError(Throwable th, int i, String str, Object... objArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" (parameter #");
            sb.append(i + 1);
            sb.append(")");
            return methodError(th, sb.toString(), objArr);
        }

        private RuntimeException parameterError(int i, String str, Object... objArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" (parameter #");
            sb.append(i + 1);
            sb.append(")");
            return methodError(sb.toString(), objArr);
        }
    }

    ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
        this.baseUrl = builder.retrofit.baseUrl();
        this.responseConverter = builder.responseConverter;
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.headers = builder.headers;
        this.contentType = builder.contentType;
        this.hasBody = builder.hasBody;
        this.isFormEncoded = builder.isFormEncoded;
        this.isMultipart = builder.isMultipart;
        this.parameterHandlers = builder.parameterHandlers;
    }

    /* access modifiers changed from: 0000 */
    public Call toCall(@Nullable Object... objArr) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(this.httpMethod, this.baseUrl, this.relativeUrl, this.headers, this.contentType, this.hasBody, this.isFormEncoded, this.isMultipart);
        ParameterHandler<?>[] parameterHandlerArr = this.parameterHandlers;
        int length = objArr != null ? objArr.length : 0;
        if (length == parameterHandlerArr.length) {
            for (int i = 0; i < length; i++) {
                parameterHandlerArr[i].apply(requestBuilder, objArr[i]);
            }
            return this.callFactory.newCall(requestBuilder.build());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Argument count (");
        sb.append(length);
        sb.append(") doesn't match expected count (");
        sb.append(parameterHandlerArr.length);
        sb.append(")");
        throw new IllegalArgumentException(sb.toString());
    }

    /* access modifiers changed from: 0000 */
    public T adapt(Call<R> call) {
        return this.callAdapter.adapt(call);
    }

    /* access modifiers changed from: 0000 */
    public R toResponse(ResponseBody responseBody) throws IOException {
        return this.responseConverter.convert(responseBody);
    }

    static Set<String> parsePathParameters(String str) {
        Matcher matcher = PARAM_URL_REGEX.matcher(str);
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        while (matcher.find()) {
            linkedHashSet.add(matcher.group(1));
        }
        return linkedHashSet;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=java.lang.Class<?>, code=java.lang.Class, for r1v0, types: [java.lang.Class<?>, java.lang.Class] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static java.lang.Class<?> boxIfPrimitive(java.lang.Class r1) {
        /*
            java.lang.Class r0 = java.lang.Boolean.TYPE
            if (r0 != r1) goto L_0x0007
            java.lang.Class<java.lang.Boolean> r1 = java.lang.Boolean.class
            return r1
        L_0x0007:
            java.lang.Class r0 = java.lang.Byte.TYPE
            if (r0 != r1) goto L_0x000e
            java.lang.Class<java.lang.Byte> r1 = java.lang.Byte.class
            return r1
        L_0x000e:
            java.lang.Class r0 = java.lang.Character.TYPE
            if (r0 != r1) goto L_0x0015
            java.lang.Class<java.lang.Character> r1 = java.lang.Character.class
            return r1
        L_0x0015:
            java.lang.Class r0 = java.lang.Double.TYPE
            if (r0 != r1) goto L_0x001c
            java.lang.Class<java.lang.Double> r1 = java.lang.Double.class
            return r1
        L_0x001c:
            java.lang.Class r0 = java.lang.Float.TYPE
            if (r0 != r1) goto L_0x0023
            java.lang.Class<java.lang.Float> r1 = java.lang.Float.class
            return r1
        L_0x0023:
            java.lang.Class r0 = java.lang.Integer.TYPE
            if (r0 != r1) goto L_0x002a
            java.lang.Class<java.lang.Integer> r1 = java.lang.Integer.class
            return r1
        L_0x002a:
            java.lang.Class r0 = java.lang.Long.TYPE
            if (r0 != r1) goto L_0x0031
            java.lang.Class<java.lang.Long> r1 = java.lang.Long.class
            return r1
        L_0x0031:
            java.lang.Class r0 = java.lang.Short.TYPE
            if (r0 != r1) goto L_0x0037
            java.lang.Class<java.lang.Short> r1 = java.lang.Short.class
        L_0x0037:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: retrofit2.ServiceMethod.boxIfPrimitive(java.lang.Class):java.lang.Class");
    }
}
