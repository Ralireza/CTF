package com.squareup.picasso;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.Response;

public interface Downloader {
    @NonNull
    Response load(@NonNull Request request) throws IOException;

    void shutdown();
}
