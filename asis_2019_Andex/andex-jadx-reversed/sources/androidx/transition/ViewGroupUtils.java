package androidx.transition;

import android.os.Build.VERSION;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

class ViewGroupUtils {
    static ViewGroupOverlayImpl getOverlay(@NonNull ViewGroup viewGroup) {
        if (VERSION.SDK_INT >= 18) {
            return new ViewGroupOverlayApi18(viewGroup);
        }
        return ViewGroupOverlayApi14.createFrom(viewGroup);
    }

    static void suppressLayout(@NonNull ViewGroup viewGroup, boolean z) {
        if (VERSION.SDK_INT >= 18) {
            ViewGroupUtilsApi18.suppressLayout(viewGroup, z);
        } else {
            ViewGroupUtilsApi14.suppressLayout(viewGroup, z);
        }
    }

    private ViewGroupUtils() {
    }
}
