package androidx.core.telephony.mbms;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.telephony.mbms.ServiceInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;

public final class MbmsHelper {
    private MbmsHelper() {
    }

    @SuppressLint({"BanTargetApiAnnotation"})
    @TargetApi(28)
    @Nullable
    public static CharSequence getBestNameForService(@NonNull Context context, @NonNull ServiceInfo serviceInfo) {
        CharSequence charSequence = null;
        if (VERSION.SDK_INT < 28) {
            return null;
        }
        LocaleList locales = context.getResources().getConfiguration().getLocales();
        int size = serviceInfo.getNamedContentLocales().size();
        if (size == 0) {
            return null;
        }
        String[] strArr = new String[size];
        int i = 0;
        for (Locale languageTag : serviceInfo.getNamedContentLocales()) {
            strArr[i] = languageTag.toLanguageTag();
            i++;
        }
        Locale firstMatch = locales.getFirstMatch(strArr);
        if (firstMatch != null) {
            charSequence = serviceInfo.getNameForLocale(firstMatch);
        }
        return charSequence;
    }
}
