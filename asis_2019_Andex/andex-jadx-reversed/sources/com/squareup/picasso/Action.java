package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso.Priority;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

abstract class Action<T> {
    boolean cancelled;
    final Drawable errorDrawable;
    final int errorResId;
    final String key;
    final int memoryPolicy;
    final int networkPolicy;
    final boolean noFade;
    final Picasso picasso;
    final Request request;
    final Object tag;
    final WeakReference<T> target;
    boolean willReplay;

    static class RequestWeakReference<M> extends WeakReference<M> {
        final Action action;

        RequestWeakReference(Action action2, M m, ReferenceQueue<? super M> referenceQueue) {
            super(m, referenceQueue);
            this.action = action2;
        }
    }

    /* access modifiers changed from: 0000 */
    public abstract void complete(Bitmap bitmap, LoadedFrom loadedFrom);

    /* access modifiers changed from: 0000 */
    public abstract void error(Exception exc);

    Action(Picasso picasso2, T t, Request request2, int i, int i2, int i3, Drawable drawable, String str, Object obj, boolean z) {
        WeakReference<T> weakReference;
        this.picasso = picasso2;
        this.request = request2;
        if (t == null) {
            weakReference = null;
        } else {
            weakReference = new RequestWeakReference<>(this, t, picasso2.referenceQueue);
        }
        this.target = weakReference;
        this.memoryPolicy = i;
        this.networkPolicy = i2;
        this.noFade = z;
        this.errorResId = i3;
        this.errorDrawable = drawable;
        this.key = str;
        if (obj == 0) {
            obj = this;
        }
        this.tag = obj;
    }

    /* access modifiers changed from: 0000 */
    public void cancel() {
        this.cancelled = true;
    }

    /* access modifiers changed from: 0000 */
    public Request getRequest() {
        return this.request;
    }

    /* access modifiers changed from: 0000 */
    public T getTarget() {
        WeakReference<T> weakReference = this.target;
        if (weakReference == null) {
            return null;
        }
        return weakReference.get();
    }

    /* access modifiers changed from: 0000 */
    public String getKey() {
        return this.key;
    }

    /* access modifiers changed from: 0000 */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /* access modifiers changed from: 0000 */
    public boolean willReplay() {
        return this.willReplay;
    }

    /* access modifiers changed from: 0000 */
    public int getMemoryPolicy() {
        return this.memoryPolicy;
    }

    /* access modifiers changed from: 0000 */
    public int getNetworkPolicy() {
        return this.networkPolicy;
    }

    /* access modifiers changed from: 0000 */
    public Picasso getPicasso() {
        return this.picasso;
    }

    /* access modifiers changed from: 0000 */
    public Priority getPriority() {
        return this.request.priority;
    }

    /* access modifiers changed from: 0000 */
    public Object getTag() {
        return this.tag;
    }
}
