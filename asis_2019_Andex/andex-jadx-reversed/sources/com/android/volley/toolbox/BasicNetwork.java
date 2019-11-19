package com.android.volley.toolbox;

import android.os.SystemClock;
import com.android.volley.Cache.Entry;
import com.android.volley.Header;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class BasicNetwork implements Network {
    protected static final boolean DEBUG = VolleyLog.DEBUG;
    private static final int DEFAULT_POOL_SIZE = 4096;
    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;
    private final BaseHttpStack mBaseHttpStack;
    @Deprecated
    protected final HttpStack mHttpStack;
    protected final ByteArrayPool mPool;

    @Deprecated
    public BasicNetwork(HttpStack httpStack) {
        this(httpStack, new ByteArrayPool(4096));
    }

    @Deprecated
    public BasicNetwork(HttpStack httpStack, ByteArrayPool byteArrayPool) {
        this.mHttpStack = httpStack;
        this.mBaseHttpStack = new AdaptedHttpStack(httpStack);
        this.mPool = byteArrayPool;
    }

    public BasicNetwork(BaseHttpStack baseHttpStack) {
        this(baseHttpStack, new ByteArrayPool(4096));
    }

    public BasicNetwork(BaseHttpStack baseHttpStack, ByteArrayPool byteArrayPool) {
        this.mBaseHttpStack = baseHttpStack;
        this.mHttpStack = baseHttpStack;
        this.mPool = byteArrayPool;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005e, code lost:
        r15 = null;
        r19 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b3, code lost:
        r1 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b6, code lost:
        r19 = r1;
        r15 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ba, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00bb, code lost:
        r19 = r1;
        r12 = null;
        r15 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c1, code lost:
        r0 = r12.getStatusCode();
        com.android.volley.VolleyLog.e("Unexpected response code %d for %s", java.lang.Integer.valueOf(r0), r29.getUrl());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00da, code lost:
        if (r15 != null) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00dc, code lost:
        r13 = new com.android.volley.NetworkResponse(r0, r15, false, android.os.SystemClock.elapsedRealtime() - r9, r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ed, code lost:
        if (r0 == 401) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f6, code lost:
        if (r0 < 400) goto L_0x0103;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0102, code lost:
        throw new com.android.volley.ClientError(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0105, code lost:
        if (r0 < 500) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x010f, code lost:
        if (r29.shouldRetryServerErrors() != false) goto L_0x0111;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0111, code lost:
        attemptRetryOnException("server", r8, new com.android.volley.ServerError(r13));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0122, code lost:
        throw new com.android.volley.ServerError(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0128, code lost:
        throw new com.android.volley.ServerError(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0129, code lost:
        attemptRetryOnException("auth", r8, new com.android.volley.AuthFailureError(r13));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0135, code lost:
        attemptRetryOnException("network", r8, new com.android.volley.NetworkError());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0146, code lost:
        throw new com.android.volley.NoConnectionError(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0147, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0148, code lost:
        r2 = new java.lang.StringBuilder();
        r2.append("Bad URL ");
        r2.append(r29.getUrl());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0162, code lost:
        throw new java.lang.RuntimeException(r2.toString(), r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0163, code lost:
        attemptRetryOnException("socket", r8, new com.android.volley.TimeoutError());
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0147 A[ExcHandler: MalformedURLException (r0v1 'e' java.net.MalformedURLException A[CUSTOM_DECLARE]), Splitter:B:2:0x000e] */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A[ExcHandler: SocketTimeoutException (unused java.net.SocketTimeoutException), SYNTHETIC, Splitter:B:2:0x000e] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0141 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.volley.NetworkResponse performRequest(com.android.volley.Request<?> r29) throws com.android.volley.VolleyError {
        /*
            r28 = this;
            r7 = r28
            r8 = r29
            long r9 = android.os.SystemClock.elapsedRealtime()
        L_0x0008:
            java.util.List r1 = java.util.Collections.emptyList()
            r11 = 0
            r2 = 0
            com.android.volley.Cache$Entry r0 = r29.getCacheEntry()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ba }
            java.util.Map r0 = r7.getCacheHeaders(r0)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ba }
            com.android.volley.toolbox.BaseHttpStack r3 = r7.mBaseHttpStack     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ba }
            com.android.volley.toolbox.HttpResponse r12 = r3.executeRequest(r8, r0)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ba }
            int r14 = r12.getStatusCode()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00b5 }
            java.util.List r13 = r12.getHeaders()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00b5 }
            r0 = 304(0x130, float:4.26E-43)
            if (r14 != r0) goto L_0x0063
            com.android.volley.Cache$Entry r0 = r29.getCacheEntry()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            if (r0 != 0) goto L_0x0043
            com.android.volley.NetworkResponse r0 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            r16 = 304(0x130, float:4.26E-43)
            r17 = 0
            r18 = 1
            long r3 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            long r19 = r3 - r9
            r15 = r0
            r21 = r13
            r15.<init>(r16, r17, r18, r19, r21)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            return r0
        L_0x0043:
            java.util.List r27 = combineHeaders(r13, r0)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            com.android.volley.NetworkResponse r1 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            r22 = 304(0x130, float:4.26E-43)
            byte[] r0 = r0.data     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            r24 = 1
            long r3 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            long r25 = r3 - r9
            r21 = r1
            r23 = r0
            r21.<init>(r22, r23, r24, r25, r27)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            return r1
        L_0x005d:
            r0 = move-exception
            r15 = r2
            r19 = r13
            goto L_0x00bf
        L_0x0063:
            java.io.InputStream r0 = r12.getContent()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00b2 }
            if (r0 == 0) goto L_0x0072
            int r1 = r12.getContentLength()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            byte[] r0 = r7.inputStreamToBytes(r0, r1)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x005d }
            goto L_0x0074
        L_0x0072:
            byte[] r0 = new byte[r11]     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00b2 }
        L_0x0074:
            r20 = r0
            long r0 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ab }
            long r2 = r0 - r9
            r1 = r28
            r4 = r29
            r5 = r20
            r6 = r14
            r1.logSlowRequests(r2, r4, r5, r6)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ab }
            r0 = 200(0xc8, float:2.8E-43)
            if (r14 < r0) goto L_0x00a2
            r0 = 299(0x12b, float:4.19E-43)
            if (r14 > r0) goto L_0x00a2
            com.android.volley.NetworkResponse r0 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ab }
            r16 = 0
            long r1 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00ab }
            long r17 = r1 - r9
            r1 = r13
            r13 = r0
            r15 = r20
            r19 = r1
            r13.<init>(r14, r15, r16, r17, r19)     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00a9 }
            return r0
        L_0x00a2:
            r1 = r13
            java.io.IOException r0 = new java.io.IOException     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00a9 }
            r0.<init>()     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00a9 }
            throw r0     // Catch:{ SocketTimeoutException -> 0x0163, MalformedURLException -> 0x0147, IOException -> 0x00a9 }
        L_0x00a9:
            r0 = move-exception
            goto L_0x00ad
        L_0x00ab:
            r0 = move-exception
            r1 = r13
        L_0x00ad:
            r19 = r1
            r15 = r20
            goto L_0x00bf
        L_0x00b2:
            r0 = move-exception
            r1 = r13
            goto L_0x00b6
        L_0x00b5:
            r0 = move-exception
        L_0x00b6:
            r19 = r1
            r15 = r2
            goto L_0x00bf
        L_0x00ba:
            r0 = move-exception
            r19 = r1
            r12 = r2
            r15 = r12
        L_0x00bf:
            if (r12 == 0) goto L_0x0141
            int r0 = r12.getStatusCode()
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r0)
            r1[r11] = r2
            r2 = 1
            java.lang.String r3 = r29.getUrl()
            r1[r2] = r3
            java.lang.String r2 = "Unexpected response code %d for %s"
            com.android.volley.VolleyLog.e(r2, r1)
            if (r15 == 0) goto L_0x0135
            com.android.volley.NetworkResponse r1 = new com.android.volley.NetworkResponse
            r16 = 0
            long r2 = android.os.SystemClock.elapsedRealtime()
            long r17 = r2 - r9
            r13 = r1
            r14 = r0
            r13.<init>(r14, r15, r16, r17, r19)
            r2 = 401(0x191, float:5.62E-43)
            if (r0 == r2) goto L_0x0129
            r2 = 403(0x193, float:5.65E-43)
            if (r0 != r2) goto L_0x00f4
            goto L_0x0129
        L_0x00f4:
            r2 = 400(0x190, float:5.6E-43)
            if (r0 < r2) goto L_0x0103
            r2 = 499(0x1f3, float:6.99E-43)
            if (r0 <= r2) goto L_0x00fd
            goto L_0x0103
        L_0x00fd:
            com.android.volley.ClientError r0 = new com.android.volley.ClientError
            r0.<init>(r1)
            throw r0
        L_0x0103:
            r2 = 500(0x1f4, float:7.0E-43)
            if (r0 < r2) goto L_0x0123
            r2 = 599(0x257, float:8.4E-43)
            if (r0 > r2) goto L_0x0123
            boolean r0 = r29.shouldRetryServerErrors()
            if (r0 == 0) goto L_0x011d
            com.android.volley.ServerError r0 = new com.android.volley.ServerError
            r0.<init>(r1)
            java.lang.String r1 = "server"
            attemptRetryOnException(r1, r8, r0)
            goto L_0x0008
        L_0x011d:
            com.android.volley.ServerError r0 = new com.android.volley.ServerError
            r0.<init>(r1)
            throw r0
        L_0x0123:
            com.android.volley.ServerError r0 = new com.android.volley.ServerError
            r0.<init>(r1)
            throw r0
        L_0x0129:
            com.android.volley.AuthFailureError r0 = new com.android.volley.AuthFailureError
            r0.<init>(r1)
            java.lang.String r1 = "auth"
            attemptRetryOnException(r1, r8, r0)
            goto L_0x0008
        L_0x0135:
            com.android.volley.NetworkError r0 = new com.android.volley.NetworkError
            r0.<init>()
            java.lang.String r1 = "network"
            attemptRetryOnException(r1, r8, r0)
            goto L_0x0008
        L_0x0141:
            com.android.volley.NoConnectionError r1 = new com.android.volley.NoConnectionError
            r1.<init>(r0)
            throw r1
        L_0x0147:
            r0 = move-exception
            java.lang.RuntimeException r1 = new java.lang.RuntimeException
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Bad URL "
            r2.append(r3)
            java.lang.String r3 = r29.getUrl()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.<init>(r2, r0)
            throw r1
        L_0x0163:
            com.android.volley.TimeoutError r0 = new com.android.volley.TimeoutError
            r0.<init>()
            java.lang.String r1 = "socket"
            attemptRetryOnException(r1, r8, r0)
            goto L_0x0008
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.toolbox.BasicNetwork.performRequest(com.android.volley.Request):com.android.volley.NetworkResponse");
    }

    private void logSlowRequests(long j, Request<?> request, byte[] bArr, int i) {
        if (DEBUG || j > 3000) {
            Object[] objArr = new Object[5];
            objArr[0] = request;
            objArr[1] = Long.valueOf(j);
            objArr[2] = bArr != null ? Integer.valueOf(bArr.length) : "null";
            objArr[3] = Integer.valueOf(i);
            objArr[4] = Integer.valueOf(request.getRetryPolicy().getCurrentRetryCount());
            VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], [rc=%d], [retryCount=%s]", objArr);
        }
    }

    private static void attemptRetryOnException(String str, Request<?> request, VolleyError volleyError) throws VolleyError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int timeoutMs = request.getTimeoutMs();
        try {
            retryPolicy.retry(volleyError);
            request.addMarker(String.format("%s-retry [timeout=%s]", new Object[]{str, Integer.valueOf(timeoutMs)}));
        } catch (VolleyError e) {
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", new Object[]{str, Integer.valueOf(timeoutMs)}));
            throw e;
        }
    }

    private Map<String, String> getCacheHeaders(Entry entry) {
        if (entry == null) {
            return Collections.emptyMap();
        }
        HashMap hashMap = new HashMap();
        if (entry.etag != null) {
            hashMap.put("If-None-Match", entry.etag);
        }
        if (entry.lastModified > 0) {
            hashMap.put("If-Modified-Since", HttpHeaderParser.formatEpochAsRfc1123(entry.lastModified));
        }
        return hashMap;
    }

    /* access modifiers changed from: protected */
    public void logError(String str, String str2, long j) {
        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", str, Long.valueOf(SystemClock.elapsedRealtime() - j), str2);
    }

    private byte[] inputStreamToBytes(InputStream inputStream, int i) throws IOException, ServerError {
        PoolingByteArrayOutputStream poolingByteArrayOutputStream = new PoolingByteArrayOutputStream(this.mPool, i);
        String str = "Error occurred when closing InputStream";
        byte[] bArr = null;
        if (inputStream != null) {
            try {
                bArr = this.mPool.getBuf(1024);
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    poolingByteArrayOutputStream.write(bArr, 0, read);
                }
                byte[] byteArray = poolingByteArrayOutputStream.toByteArray();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException unused) {
                        VolleyLog.v(str, new Object[0]);
                    }
                }
                return byteArray;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException unused2) {
                        VolleyLog.v(str, new Object[0]);
                    }
                }
                this.mPool.returnBuf(bArr);
                poolingByteArrayOutputStream.close();
            }
        } else {
            throw new ServerError();
        }
    }

    @Deprecated
    protected static Map<String, String> convertHeaders(Header[] headerArr) {
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headerArr.length; i++) {
            treeMap.put(headerArr[i].getName(), headerArr[i].getValue());
        }
        return treeMap;
    }

    private static List<Header> combineHeaders(List<Header> list, Entry entry) {
        TreeSet treeSet = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        if (!list.isEmpty()) {
            for (Header name : list) {
                treeSet.add(name.getName());
            }
        }
        ArrayList arrayList = new ArrayList(list);
        if (entry.allResponseHeaders != null) {
            if (!entry.allResponseHeaders.isEmpty()) {
                for (Header header : entry.allResponseHeaders) {
                    if (!treeSet.contains(header.getName())) {
                        arrayList.add(header);
                    }
                }
            }
        } else if (!entry.responseHeaders.isEmpty()) {
            for (Map.Entry entry2 : entry.responseHeaders.entrySet()) {
                if (!treeSet.contains(entry2.getKey())) {
                    arrayList.add(new Header((String) entry2.getKey(), (String) entry2.getValue()));
                }
            }
        }
        return arrayList;
    }
}
