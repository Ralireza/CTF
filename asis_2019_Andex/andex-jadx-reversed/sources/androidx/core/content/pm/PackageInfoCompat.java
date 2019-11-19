package androidx.core.content.pm;

import android.content.pm.PackageInfo;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;

public final class PackageInfoCompat {
    public static long getLongVersionCode(@NonNull PackageInfo packageInfo) {
        if (VERSION.SDK_INT >= 28) {
            return packageInfo.getLongVersionCode();
        }
        return (long) packageInfo.versionCode;
    }

    private PackageInfoCompat() {
    }
}
