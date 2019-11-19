package androidx.core.view.inputmethod;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class EditorInfoCompat {
    private static final String CONTENT_MIME_TYPES_INTEROP_KEY = "androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES";
    private static final String CONTENT_MIME_TYPES_KEY = "androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final int IME_FLAG_FORCE_ASCII = Integer.MIN_VALUE;
    public static final int IME_FLAG_NO_PERSONALIZED_LEARNING = 16777216;

    public static void setContentMimeTypes(@NonNull EditorInfo editorInfo, @Nullable String[] strArr) {
        if (VERSION.SDK_INT >= 25) {
            editorInfo.contentMimeTypes = strArr;
            return;
        }
        if (editorInfo.extras == null) {
            editorInfo.extras = new Bundle();
        }
        String str = "androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES";
        editorInfo.extras.putStringArray(str, strArr);
        editorInfo.extras.putStringArray(str, strArr);
    }

    @NonNull
    public static String[] getContentMimeTypes(EditorInfo editorInfo) {
        if (VERSION.SDK_INT >= 25) {
            String[] strArr = editorInfo.contentMimeTypes;
            if (strArr == null) {
                strArr = EMPTY_STRING_ARRAY;
            }
            return strArr;
        } else if (editorInfo.extras == null) {
            return EMPTY_STRING_ARRAY;
        } else {
            String str = "androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES";
            String[] stringArray = editorInfo.extras.getStringArray(str);
            if (stringArray == null) {
                stringArray = editorInfo.extras.getStringArray(str);
            }
            if (stringArray == null) {
                stringArray = EMPTY_STRING_ARRAY;
            }
            return stringArray;
        }
    }

    static int getProtocol(EditorInfo editorInfo) {
        if (VERSION.SDK_INT >= 25) {
            return 1;
        }
        if (editorInfo.extras == null) {
            return 0;
        }
        String str = "androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES";
        boolean containsKey = editorInfo.extras.containsKey(str);
        boolean containsKey2 = editorInfo.extras.containsKey(str);
        if (containsKey && containsKey2) {
            return 4;
        }
        if (containsKey) {
            return 3;
        }
        if (containsKey2) {
            return 2;
        }
        return 0;
    }
}
