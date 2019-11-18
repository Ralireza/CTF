package com.asisctf.config;

import android.content.Context;
import android.os.Build;
import androidx.core.p003os.EnvironmentCompat;
import com.scottyab.rootbeer.RootBeer;

public class SayHelloToYourLittleFriend {
    public String[] config(Context context) {
        String[] strArr = new String[0];
        new RootBeer(context);
        return new String[]{"/api/shop/items/get_data", " /api/shop/order/", "/api/userClass/me", "/api/shop/order/as/gold/item_id/"};
    }

    public boolean isEmulator() {
        String str = "generic";
        if (!Build.FINGERPRINT.startsWith(str) && !Build.FINGERPRINT.startsWith(EnvironmentCompat.MEDIA_UNKNOWN)) {
            String str2 = "google_sdk";
            if (!Build.MODEL.contains(str2) && !Build.MODEL.contains("Emulator") && !Build.MODEL.contains("Android SDK built for x86") && !Build.MANUFACTURER.contains("Genymotion") && ((!Build.BRAND.startsWith(str) || !Build.DEVICE.startsWith(str)) && !str2.equals(Build.PRODUCT))) {
                return false;
            }
        }
        return true;
    }
}