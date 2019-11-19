package androidx.versionedparcelable;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@RestrictTo({Scope.LIBRARY_GROUP_PREFIX})
@Retention(RetentionPolicy.SOURCE)
public @interface NonParcelField {
}
