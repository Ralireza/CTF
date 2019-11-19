package androidx.core.location;

import android.location.LocationManager;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;

public final class LocationManagerCompat {
    public static boolean isLocationEnabled(@NonNull LocationManager locationManager) {
        if (VERSION.SDK_INT >= 28) {
            return locationManager.isLocationEnabled();
        }
        return locationManager.isProviderEnabled("network") || locationManager.isProviderEnabled("gps");
    }

    private LocationManagerCompat() {
    }
}
