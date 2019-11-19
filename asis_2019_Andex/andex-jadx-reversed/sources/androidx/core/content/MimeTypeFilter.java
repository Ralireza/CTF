package androidx.core.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public final class MimeTypeFilter {
    private MimeTypeFilter() {
    }

    private static boolean mimeTypeAgainstFilter(@NonNull String[] strArr, @NonNull String[] strArr2) {
        if (strArr2.length != 2) {
            throw new IllegalArgumentException("Ill-formatted MIME type filter. Must be type/subtype.");
        } else if (strArr2[0].isEmpty() || strArr2[1].isEmpty()) {
            throw new IllegalArgumentException("Ill-formatted MIME type filter. Type or subtype empty.");
        } else if (strArr.length != 2) {
            return false;
        } else {
            String str = "*";
            if (!str.equals(strArr2[0]) && !strArr2[0].equals(strArr[0])) {
                return false;
            }
            if (str.equals(strArr2[1]) || strArr2[1].equals(strArr[1])) {
                return true;
            }
            return false;
        }
    }

    public static boolean matches(@Nullable String str, @NonNull String str2) {
        if (str == null) {
            return false;
        }
        String str3 = "/";
        return mimeTypeAgainstFilter(str.split(str3), str2.split(str3));
    }

    @Nullable
    public static String matches(@Nullable String str, @NonNull String[] strArr) {
        if (str == null) {
            return null;
        }
        String str2 = "/";
        String[] split = str.split(str2);
        for (String str3 : strArr) {
            if (mimeTypeAgainstFilter(split, str3.split(str2))) {
                return str3;
            }
        }
        return null;
    }

    @Nullable
    public static String matches(@Nullable String[] strArr, @NonNull String str) {
        if (strArr == null) {
            return null;
        }
        String str2 = "/";
        String[] split = str.split(str2);
        for (String str3 : strArr) {
            if (mimeTypeAgainstFilter(str3.split(str2), split)) {
                return str3;
            }
        }
        return null;
    }

    @NonNull
    public static String[] matchesMany(@Nullable String[] strArr, @NonNull String str) {
        if (strArr == null) {
            return new String[0];
        }
        ArrayList arrayList = new ArrayList();
        String str2 = "/";
        String[] split = str.split(str2);
        for (String str3 : strArr) {
            if (mimeTypeAgainstFilter(str3.split(str2), split)) {
                arrayList.add(str3);
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }
}
