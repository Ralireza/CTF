package com.squareup.picasso;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import okhttp3.Cache;
import okhttp3.Call.Factory;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public final class OkHttp3Downloader implements Downloader {
    private final Cache cache;
    @VisibleForTesting
    final Factory client;
    private boolean sharedClient;

    public OkHttp3Downloader(Context context) {
        this(Utils.createDefaultCacheDir(context));
    }

    public OkHttp3Downloader(File file) {
        this(file, Utils.calculateDiskCacheSize(file));
    }

    public OkHttp3Downloader(Context context, long j) {
        this(Utils.createDefaultCacheDir(context), j);
    }

    public OkHttp3Downloader(File file, long j) {
        this(new Builder().cache(new Cache(file, j)).build());
        this.sharedClient = false;
    }

    public OkHttp3Downloader(OkHttpClient okHttpClient) {
        this.sharedClient = true;
        this.client = okHttpClient;
        this.cache = okHttpClient.cache();
    }

    public OkHttp3Downloader(Factory factory) {
        this.sharedClient = true;
        this.client = factory;
        this.cache = null;
    }

    @NonNull
    public Response load(@NonNull Request request) throws IOException {
        return this.client.newCall(request).execute();
    }

    public void shutdown() {
        if (!this.sharedClient) {
            Cache cache2 = this.cache;
            if (cache2 != null) {
                try {
                    cache2.close();
                } catch (IOException unused) {
                }
            }
        }
    }
}
