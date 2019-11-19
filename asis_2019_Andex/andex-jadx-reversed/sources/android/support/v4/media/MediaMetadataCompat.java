package android.support.v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.collection.ArrayMap;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public final class MediaMetadataCompat implements Parcelable {
    public static final Creator<MediaMetadataCompat> CREATOR = new Creator<MediaMetadataCompat>() {
        public MediaMetadataCompat createFromParcel(Parcel parcel) {
            return new MediaMetadataCompat(parcel);
        }

        public MediaMetadataCompat[] newArray(int i) {
            return new MediaMetadataCompat[i];
        }
    };
    static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = new ArrayMap<>();
    public static final String METADATA_KEY_ADVERTISEMENT = "android.media.metadata.ADVERTISEMENT";
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    public static final String METADATA_KEY_BT_FOLDER_TYPE = "android.media.metadata.BT_FOLDER_TYPE";
    public static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";
    public static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";
    public static final String METADATA_KEY_DATE = "android.media.metadata.DATE";
    public static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";
    public static final String METADATA_KEY_DISPLAY_DESCRIPTION = "android.media.metadata.DISPLAY_DESCRIPTION";
    public static final String METADATA_KEY_DISPLAY_ICON = "android.media.metadata.DISPLAY_ICON";
    public static final String METADATA_KEY_DISPLAY_ICON_URI = "android.media.metadata.DISPLAY_ICON_URI";
    public static final String METADATA_KEY_DISPLAY_SUBTITLE = "android.media.metadata.DISPLAY_SUBTITLE";
    public static final String METADATA_KEY_DISPLAY_TITLE = "android.media.metadata.DISPLAY_TITLE";
    public static final String METADATA_KEY_DOWNLOAD_STATUS = "android.media.metadata.DOWNLOAD_STATUS";
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";
    public static final String METADATA_KEY_MEDIA_URI = "android.media.metadata.MEDIA_URI";
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";
    static final int METADATA_TYPE_BITMAP = 2;
    static final int METADATA_TYPE_LONG = 0;
    static final int METADATA_TYPE_RATING = 3;
    static final int METADATA_TYPE_TEXT = 1;
    private static final String[] PREFERRED_BITMAP_ORDER;
    private static final String[] PREFERRED_DESCRIPTION_ORDER = {METADATA_KEY_TITLE, METADATA_KEY_ARTIST, METADATA_KEY_ALBUM, METADATA_KEY_ALBUM_ARTIST, METADATA_KEY_WRITER, METADATA_KEY_AUTHOR, METADATA_KEY_COMPOSER};
    private static final String[] PREFERRED_URI_ORDER;
    private static final String TAG = "MediaMetadata";
    final Bundle mBundle;
    private MediaDescriptionCompat mDescription;
    private Object mMetadataObj;

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BitmapKey {
    }

    public static final class Builder {
        private final Bundle mBundle;

        public Builder() {
            this.mBundle = new Bundle();
        }

        public Builder(MediaMetadataCompat mediaMetadataCompat) {
            this.mBundle = new Bundle(mediaMetadataCompat.mBundle);
            MediaSessionCompat.ensureClassLoader(this.mBundle);
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public Builder(MediaMetadataCompat mediaMetadataCompat, int i) {
            this(mediaMetadataCompat);
            for (String str : this.mBundle.keySet()) {
                Object obj = this.mBundle.get(str);
                if (obj instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) obj;
                    if (bitmap.getHeight() > i || bitmap.getWidth() > i) {
                        putBitmap(str, scaleBitmap(bitmap, i));
                    }
                }
            }
        }

        public Builder putText(String str, CharSequence charSequence) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(str) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(str)).intValue() == 1) {
                this.mBundle.putCharSequence(str, charSequence);
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(str);
            sb.append(" key cannot be used to put a CharSequence");
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder putString(String str, String str2) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(str) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(str)).intValue() == 1) {
                this.mBundle.putCharSequence(str, str2);
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(str);
            sb.append(" key cannot be used to put a String");
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder putLong(String str, long j) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(str) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(str)).intValue() == 0) {
                this.mBundle.putLong(str, j);
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(str);
            sb.append(" key cannot be used to put a long");
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder putRating(String str, RatingCompat ratingCompat) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(str) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(str)).intValue() == 3) {
                if (VERSION.SDK_INT >= 19) {
                    this.mBundle.putParcelable(str, (Parcelable) ratingCompat.getRating());
                } else {
                    this.mBundle.putParcelable(str, ratingCompat);
                }
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(str);
            sb.append(" key cannot be used to put a Rating");
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder putBitmap(String str, Bitmap bitmap) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(str) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(str)).intValue() == 2) {
                this.mBundle.putParcelable(str, bitmap);
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(str);
            sb.append(" key cannot be used to put a Bitmap");
            throw new IllegalArgumentException(sb.toString());
        }

        public MediaMetadataCompat build() {
            return new MediaMetadataCompat(this.mBundle);
        }

        private Bitmap scaleBitmap(Bitmap bitmap, int i) {
            float f = (float) i;
            float min = Math.min(f / ((float) bitmap.getWidth()), f / ((float) bitmap.getHeight()));
            return Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * min), (int) (((float) bitmap.getHeight()) * min), true);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LongKey {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RatingKey {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextKey {
    }

    public int describeContents() {
        return 0;
    }

    static {
        ArrayMap<String, Integer> arrayMap = METADATA_KEYS_TYPE;
        Integer valueOf = Integer.valueOf(1);
        arrayMap.put(METADATA_KEY_TITLE, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ARTIST, valueOf);
        ArrayMap<String, Integer> arrayMap2 = METADATA_KEYS_TYPE;
        Integer valueOf2 = Integer.valueOf(0);
        arrayMap2.put(METADATA_KEY_DURATION, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_AUTHOR, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_WRITER, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPOSER, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPILATION, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DATE, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_YEAR, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_GENRE, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_TRACK_NUMBER, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_NUM_TRACKS, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISC_NUMBER, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ARTIST, valueOf);
        ArrayMap<String, Integer> arrayMap3 = METADATA_KEYS_TYPE;
        Integer valueOf3 = Integer.valueOf(2);
        String str = METADATA_KEY_ART;
        arrayMap3.put(str, valueOf3);
        ArrayMap<String, Integer> arrayMap4 = METADATA_KEYS_TYPE;
        String str2 = METADATA_KEY_ART_URI;
        arrayMap4.put(str2, valueOf);
        ArrayMap<String, Integer> arrayMap5 = METADATA_KEYS_TYPE;
        String str3 = METADATA_KEY_ALBUM_ART;
        arrayMap5.put(str3, valueOf3);
        ArrayMap<String, Integer> arrayMap6 = METADATA_KEYS_TYPE;
        String str4 = METADATA_KEY_ALBUM_ART_URI;
        arrayMap6.put(str4, valueOf);
        ArrayMap<String, Integer> arrayMap7 = METADATA_KEYS_TYPE;
        Integer valueOf4 = Integer.valueOf(3);
        arrayMap7.put(METADATA_KEY_USER_RATING, valueOf4);
        METADATA_KEYS_TYPE.put(METADATA_KEY_RATING, valueOf4);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_TITLE, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_SUBTITLE, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_DESCRIPTION, valueOf);
        ArrayMap<String, Integer> arrayMap8 = METADATA_KEYS_TYPE;
        String str5 = METADATA_KEY_DISPLAY_ICON;
        arrayMap8.put(str5, valueOf3);
        ArrayMap<String, Integer> arrayMap9 = METADATA_KEYS_TYPE;
        String str6 = METADATA_KEY_DISPLAY_ICON_URI;
        arrayMap9.put(str6, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_MEDIA_ID, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_BT_FOLDER_TYPE, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_MEDIA_URI, valueOf);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ADVERTISEMENT, valueOf2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DOWNLOAD_STATUS, valueOf2);
        PREFERRED_BITMAP_ORDER = new String[]{str5, str, str3};
        PREFERRED_URI_ORDER = new String[]{str6, str2, str4};
    }

    MediaMetadataCompat(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
        MediaSessionCompat.ensureClassLoader(this.mBundle);
    }

    MediaMetadataCompat(Parcel parcel) {
        this.mBundle = parcel.readBundle(MediaSessionCompat.class.getClassLoader());
    }

    public boolean containsKey(String str) {
        return this.mBundle.containsKey(str);
    }

    public CharSequence getText(String str) {
        return this.mBundle.getCharSequence(str);
    }

    public String getString(String str) {
        CharSequence charSequence = this.mBundle.getCharSequence(str);
        if (charSequence != null) {
            return charSequence.toString();
        }
        return null;
    }

    public long getLong(String str) {
        return this.mBundle.getLong(str, 0);
    }

    public RatingCompat getRating(String str) {
        try {
            if (VERSION.SDK_INT >= 19) {
                return RatingCompat.fromRating(this.mBundle.getParcelable(str));
            }
            return (RatingCompat) this.mBundle.getParcelable(str);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
            return null;
        }
    }

    public Bitmap getBitmap(String str) {
        try {
            return (Bitmap) this.mBundle.getParcelable(str);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return null;
        }
    }

    public MediaDescriptionCompat getDescription() {
        Uri uri;
        Bitmap bitmap;
        Uri uri2;
        MediaDescriptionCompat mediaDescriptionCompat = this.mDescription;
        if (mediaDescriptionCompat != null) {
            return mediaDescriptionCompat;
        }
        String string = getString(METADATA_KEY_MEDIA_ID);
        CharSequence[] charSequenceArr = new CharSequence[3];
        CharSequence text = getText(METADATA_KEY_DISPLAY_TITLE);
        if (TextUtils.isEmpty(text)) {
            int i = 0;
            int i2 = 0;
            while (i < charSequenceArr.length) {
                String[] strArr = PREFERRED_DESCRIPTION_ORDER;
                if (i2 >= strArr.length) {
                    break;
                }
                int i3 = i2 + 1;
                CharSequence text2 = getText(strArr[i2]);
                if (!TextUtils.isEmpty(text2)) {
                    int i4 = i + 1;
                    charSequenceArr[i] = text2;
                    i = i4;
                }
                i2 = i3;
            }
        } else {
            charSequenceArr[0] = text;
            charSequenceArr[1] = getText(METADATA_KEY_DISPLAY_SUBTITLE);
            charSequenceArr[2] = getText(METADATA_KEY_DISPLAY_DESCRIPTION);
        }
        int i5 = 0;
        while (true) {
            String[] strArr2 = PREFERRED_BITMAP_ORDER;
            uri = null;
            if (i5 >= strArr2.length) {
                bitmap = null;
                break;
            }
            bitmap = getBitmap(strArr2[i5]);
            if (bitmap != null) {
                break;
            }
            i5++;
        }
        int i6 = 0;
        while (true) {
            String[] strArr3 = PREFERRED_URI_ORDER;
            if (i6 >= strArr3.length) {
                uri2 = null;
                break;
            }
            String string2 = getString(strArr3[i6]);
            if (!TextUtils.isEmpty(string2)) {
                uri2 = Uri.parse(string2);
                break;
            }
            i6++;
        }
        String string3 = getString(METADATA_KEY_MEDIA_URI);
        if (!TextUtils.isEmpty(string3)) {
            uri = Uri.parse(string3);
        }
        android.support.v4.media.MediaDescriptionCompat.Builder builder = new android.support.v4.media.MediaDescriptionCompat.Builder();
        builder.setMediaId(string);
        builder.setTitle(charSequenceArr[0]);
        builder.setSubtitle(charSequenceArr[1]);
        builder.setDescription(charSequenceArr[2]);
        builder.setIconBitmap(bitmap);
        builder.setIconUri(uri2);
        builder.setMediaUri(uri);
        Bundle bundle = new Bundle();
        Bundle bundle2 = this.mBundle;
        String str = METADATA_KEY_BT_FOLDER_TYPE;
        if (bundle2.containsKey(str)) {
            bundle.putLong(MediaDescriptionCompat.EXTRA_BT_FOLDER_TYPE, getLong(str));
        }
        Bundle bundle3 = this.mBundle;
        String str2 = METADATA_KEY_DOWNLOAD_STATUS;
        if (bundle3.containsKey(str2)) {
            bundle.putLong(MediaDescriptionCompat.EXTRA_DOWNLOAD_STATUS, getLong(str2));
        }
        if (!bundle.isEmpty()) {
            builder.setExtras(bundle);
        }
        this.mDescription = builder.build();
        return this.mDescription;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mBundle);
    }

    public int size() {
        return this.mBundle.size();
    }

    public Set<String> keySet() {
        return this.mBundle.keySet();
    }

    public Bundle getBundle() {
        return new Bundle(this.mBundle);
    }

    public static MediaMetadataCompat fromMediaMetadata(Object obj) {
        if (obj == null || VERSION.SDK_INT < 21) {
            return null;
        }
        Parcel obtain = Parcel.obtain();
        MediaMetadataCompatApi21.writeToParcel(obj, obtain, 0);
        obtain.setDataPosition(0);
        MediaMetadataCompat mediaMetadataCompat = (MediaMetadataCompat) CREATOR.createFromParcel(obtain);
        obtain.recycle();
        mediaMetadataCompat.mMetadataObj = obj;
        return mediaMetadataCompat;
    }

    public Object getMediaMetadata() {
        if (this.mMetadataObj == null && VERSION.SDK_INT >= 21) {
            Parcel obtain = Parcel.obtain();
            writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            this.mMetadataObj = MediaMetadataCompatApi21.createFromParcel(obtain);
            obtain.recycle();
        }
        return this.mMetadataObj;
    }
}
