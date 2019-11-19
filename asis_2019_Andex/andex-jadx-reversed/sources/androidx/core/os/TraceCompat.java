package androidx.core.os;

import android.os.Build.VERSION;
import android.os.Trace;
import androidx.annotation.NonNull;

public final class TraceCompat {
    public static void beginSection(@NonNull String str) {
        if (VERSION.SDK_INT >= 18) {
            Trace.beginSection(str);
        }
    }

    public static void endSection() {
        if (VERSION.SDK_INT >= 18) {
            Trace.endSection();
        }
    }

    private TraceCompat() {
    }
}
