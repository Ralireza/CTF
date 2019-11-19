package androidx.core.view.inputmethod;

import android.content.ClipDescription;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputContentInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class InputConnectionCompat {
    private static final String COMMIT_CONTENT_ACTION = "androidx.core.view.inputmethod.InputConnectionCompat.COMMIT_CONTENT";
    private static final String COMMIT_CONTENT_CONTENT_URI_INTEROP_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_URI";
    private static final String COMMIT_CONTENT_CONTENT_URI_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_URI";
    private static final String COMMIT_CONTENT_DESCRIPTION_INTEROP_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_DESCRIPTION";
    private static final String COMMIT_CONTENT_DESCRIPTION_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_DESCRIPTION";
    private static final String COMMIT_CONTENT_FLAGS_INTEROP_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_FLAGS";
    private static final String COMMIT_CONTENT_FLAGS_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_FLAGS";
    private static final String COMMIT_CONTENT_INTEROP_ACTION = "androidx.core.view.inputmethod.InputConnectionCompat.COMMIT_CONTENT";
    private static final String COMMIT_CONTENT_LINK_URI_INTEROP_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_LINK_URI";
    private static final String COMMIT_CONTENT_LINK_URI_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_LINK_URI";
    private static final String COMMIT_CONTENT_OPTS_INTEROP_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_OPTS";
    private static final String COMMIT_CONTENT_OPTS_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_OPTS";
    private static final String COMMIT_CONTENT_RESULT_INTEROP_RECEIVER_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_RESULT_RECEIVER";
    private static final String COMMIT_CONTENT_RESULT_RECEIVER_KEY = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_RESULT_RECEIVER";
    public static final int INPUT_CONTENT_GRANT_READ_URI_PERMISSION = 1;

    public interface OnCommitContentListener {
        boolean onCommitContent(InputContentInfoCompat inputContentInfoCompat, int i, Bundle bundle);
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean handlePerformPrivateCommand(@androidx.annotation.Nullable java.lang.String r10, @androidx.annotation.NonNull android.os.Bundle r11, @androidx.annotation.NonNull androidx.core.view.inputmethod.InputConnectionCompat.OnCommitContentListener r12) {
        /*
            java.lang.String r0 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_OPTS"
            java.lang.String r1 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_FLAGS"
            java.lang.String r2 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_LINK_URI"
            java.lang.String r3 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_DESCRIPTION"
            java.lang.String r4 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_URI"
            java.lang.String r5 = "androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_RESULT_RECEIVER"
            r6 = 0
            if (r11 != 0) goto L_0x0010
            return r6
        L_0x0010:
            java.lang.String r7 = "androidx.core.view.inputmethod.InputConnectionCompat.COMMIT_CONTENT"
            boolean r8 = android.text.TextUtils.equals(r7, r10)
            r9 = 1
            if (r8 == 0) goto L_0x001b
            r10 = 0
            goto L_0x0022
        L_0x001b:
            boolean r10 = android.text.TextUtils.equals(r7, r10)
            if (r10 == 0) goto L_0x0067
            r10 = 1
        L_0x0022:
            r7 = 0
            android.os.Parcelable r5 = r11.getParcelable(r5)     // Catch:{ all -> 0x005f }
            android.os.ResultReceiver r5 = (android.os.ResultReceiver) r5     // Catch:{ all -> 0x005f }
            android.os.Parcelable r4 = r11.getParcelable(r4)     // Catch:{ all -> 0x005d }
            android.net.Uri r4 = (android.net.Uri) r4     // Catch:{ all -> 0x005d }
            android.os.Parcelable r3 = r11.getParcelable(r3)     // Catch:{ all -> 0x005d }
            android.content.ClipDescription r3 = (android.content.ClipDescription) r3     // Catch:{ all -> 0x005d }
            android.os.Parcelable r2 = r11.getParcelable(r2)     // Catch:{ all -> 0x005d }
            android.net.Uri r2 = (android.net.Uri) r2     // Catch:{ all -> 0x005d }
            int r1 = r11.getInt(r1)     // Catch:{ all -> 0x005d }
            android.os.Parcelable r10 = r11.getParcelable(r0)     // Catch:{ all -> 0x005d }
            android.os.Bundle r10 = (android.os.Bundle) r10     // Catch:{ all -> 0x005d }
            if (r4 == 0) goto L_0x0053
            if (r3 == 0) goto L_0x0053
            androidx.core.view.inputmethod.InputContentInfoCompat r11 = new androidx.core.view.inputmethod.InputContentInfoCompat     // Catch:{ all -> 0x005d }
            r11.<init>(r4, r3, r2)     // Catch:{ all -> 0x005d }
            boolean r10 = r12.onCommitContent(r11, r1, r10)     // Catch:{ all -> 0x005d }
            goto L_0x0054
        L_0x0053:
            r10 = 0
        L_0x0054:
            if (r5 == 0) goto L_0x005c
            if (r10 == 0) goto L_0x0059
            r6 = 1
        L_0x0059:
            r5.send(r6, r7)
        L_0x005c:
            return r10
        L_0x005d:
            r10 = move-exception
            goto L_0x0061
        L_0x005f:
            r10 = move-exception
            r5 = r7
        L_0x0061:
            if (r5 == 0) goto L_0x0066
            r5.send(r6, r7)
        L_0x0066:
            throw r10
        L_0x0067:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.view.inputmethod.InputConnectionCompat.handlePerformPrivateCommand(java.lang.String, android.os.Bundle, androidx.core.view.inputmethod.InputConnectionCompat$OnCommitContentListener):boolean");
    }

    public static boolean commitContent(@NonNull InputConnection inputConnection, @NonNull EditorInfo editorInfo, @NonNull InputContentInfoCompat inputContentInfoCompat, int i, @Nullable Bundle bundle) {
        boolean z;
        ClipDescription description = inputContentInfoCompat.getDescription();
        String[] contentMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        int length = contentMimeTypes.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                z = false;
                break;
            } else if (description.hasMimeType(contentMimeTypes[i2])) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        if (!z) {
            return false;
        }
        if (VERSION.SDK_INT >= 25) {
            return inputConnection.commitContent((InputContentInfo) inputContentInfoCompat.unwrap(), i, bundle);
        }
        int protocol = EditorInfoCompat.getProtocol(editorInfo);
        if (protocol != 2) {
            if (!(protocol == 3 || protocol == 4)) {
                return false;
            }
        }
        Bundle bundle2 = new Bundle();
        bundle2.putParcelable("androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_URI", inputContentInfoCompat.getContentUri());
        bundle2.putParcelable("androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_DESCRIPTION", inputContentInfoCompat.getDescription());
        bundle2.putParcelable("androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_LINK_URI", inputContentInfoCompat.getLinkUri());
        bundle2.putInt("androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_FLAGS", i);
        bundle2.putParcelable("androidx.core.view.inputmethod.InputConnectionCompat.CONTENT_OPTS", bundle);
        return inputConnection.performPrivateCommand("androidx.core.view.inputmethod.InputConnectionCompat.COMMIT_CONTENT", bundle2);
    }

    @NonNull
    public static InputConnection createWrapper(@NonNull InputConnection inputConnection, @NonNull EditorInfo editorInfo, @NonNull final OnCommitContentListener onCommitContentListener) {
        if (inputConnection == null) {
            throw new IllegalArgumentException("inputConnection must be non-null");
        } else if (editorInfo == null) {
            throw new IllegalArgumentException("editorInfo must be non-null");
        } else if (onCommitContentListener == null) {
            throw new IllegalArgumentException("onCommitContentListener must be non-null");
        } else if (VERSION.SDK_INT >= 25) {
            return new InputConnectionWrapper(inputConnection, false) {
                public boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle) {
                    if (onCommitContentListener.onCommitContent(InputContentInfoCompat.wrap(inputContentInfo), i, bundle)) {
                        return true;
                    }
                    return super.commitContent(inputContentInfo, i, bundle);
                }
            };
        } else {
            if (EditorInfoCompat.getContentMimeTypes(editorInfo).length == 0) {
                return inputConnection;
            }
            return new InputConnectionWrapper(inputConnection, false) {
                public boolean performPrivateCommand(String str, Bundle bundle) {
                    if (InputConnectionCompat.handlePerformPrivateCommand(str, bundle, onCommitContentListener)) {
                        return true;
                    }
                    return super.performPrivateCommand(str, bundle);
                }
            };
        }
    }
}
