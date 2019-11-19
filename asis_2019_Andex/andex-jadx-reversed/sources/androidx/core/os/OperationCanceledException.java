package androidx.core.os;

import androidx.annotation.Nullable;

public class OperationCanceledException extends RuntimeException {
    public OperationCanceledException() {
        this(null);
    }

    public OperationCanceledException(@Nullable String str) {
        if (str == null) {
            str = "The operation has been canceled.";
        }
        super(str);
    }
}
