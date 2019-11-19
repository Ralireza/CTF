package com.squareup.picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.RequestHandler.Result;
import java.io.IOException;
import java.io.InputStream;
import okio.Okio;

class ContactsPhotoRequestHandler extends RequestHandler {
    private static final int ID_CONTACT = 3;
    private static final int ID_DISPLAY_PHOTO = 4;
    private static final int ID_LOOKUP = 1;
    private static final int ID_THUMBNAIL = 2;
    private static final UriMatcher matcher = new UriMatcher(-1);
    private final Context context;

    static {
        String str = "com.android.contacts";
        matcher.addURI(str, "contacts/lookup/*/#", 1);
        matcher.addURI(str, "contacts/lookup/*", 1);
        matcher.addURI(str, "contacts/#/photo", 2);
        matcher.addURI(str, "contacts/#", 3);
        matcher.addURI(str, "display_photo/#", 4);
    }

    ContactsPhotoRequestHandler(Context context2) {
        this.context = context2;
    }

    public boolean canHandleRequest(Request request) {
        Uri uri = request.uri;
        return "content".equals(uri.getScheme()) && Contacts.CONTENT_URI.getHost().equals(uri.getHost()) && matcher.match(request.uri) != -1;
    }

    public Result load(Request request, int i) throws IOException {
        InputStream inputStream = getInputStream(request);
        if (inputStream == null) {
            return null;
        }
        return new Result(Okio.source(inputStream), LoadedFrom.DISK);
    }

    private InputStream getInputStream(Request request) throws IOException {
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri uri = request.uri;
        int match = matcher.match(uri);
        if (match != 1) {
            if (match != 2) {
                if (match != 3) {
                    if (match != 4) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Invalid uri: ");
                        sb.append(uri);
                        throw new IllegalStateException(sb.toString());
                    }
                }
            }
            return contentResolver.openInputStream(uri);
        }
        uri = Contacts.lookupContact(contentResolver, uri);
        if (uri == null) {
            return null;
        }
        return Contacts.openContactPhotoInputStream(contentResolver, uri, true);
    }
}
