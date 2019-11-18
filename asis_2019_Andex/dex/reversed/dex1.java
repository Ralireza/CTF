package com.asisctf.config;

import android.content.Context;
import android.os.Build;
import androidx.core.p003os.EnvironmentCompat;
import com.scottyab.rootbeer.RootBeer;

public class SayHelloToYourLittleFriend {
    public String[] config(Context context) {
        String shop_item = "/api/shop/items/get_data";
        String shop_order = " /api/shop/order/";
        String user_profile = "/api/userClass/me";
        String[] config = new String[0];
        if (new RootBeer(context).isRooted() || isEmulator()) {
            return config;
        }
        return new String[]{shop_item, shop_order, user_profile};
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