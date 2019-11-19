package androidx.transition;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

@SuppressLint({"ViewConstructor"})
class GhostViewApi14 extends View implements GhostViewImpl {
    Matrix mCurrentMatrix;
    private int mDeltaX;
    private int mDeltaY;
    private final Matrix mMatrix = new Matrix();
    private final OnPreDrawListener mOnPreDrawListener = new OnPreDrawListener() {
        public boolean onPreDraw() {
            GhostViewApi14 ghostViewApi14 = GhostViewApi14.this;
            ghostViewApi14.mCurrentMatrix = ghostViewApi14.mView.getMatrix();
            ViewCompat.postInvalidateOnAnimation(GhostViewApi14.this);
            if (!(GhostViewApi14.this.mStartParent == null || GhostViewApi14.this.mStartView == null)) {
                GhostViewApi14.this.mStartParent.endViewTransition(GhostViewApi14.this.mStartView);
                ViewCompat.postInvalidateOnAnimation(GhostViewApi14.this.mStartParent);
                GhostViewApi14 ghostViewApi142 = GhostViewApi14.this;
                ghostViewApi142.mStartParent = null;
                ghostViewApi142.mStartView = null;
            }
            return true;
        }
    };
    int mReferences;
    ViewGroup mStartParent;
    View mStartView;
    final View mView;

    static GhostViewImpl addGhost(View view, ViewGroup viewGroup) {
        GhostViewApi14 ghostView = getGhostView(view);
        if (ghostView == null) {
            FrameLayout findFrameLayout = findFrameLayout(viewGroup);
            if (findFrameLayout == null) {
                return null;
            }
            ghostView = new GhostViewApi14(view);
            findFrameLayout.addView(ghostView);
        }
        ghostView.mReferences++;
        return ghostView;
    }

    static void removeGhost(View view) {
        GhostViewApi14 ghostView = getGhostView(view);
        if (ghostView != null) {
            ghostView.mReferences--;
            if (ghostView.mReferences <= 0) {
                ViewParent parent = ghostView.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    viewGroup.endViewTransition(ghostView);
                    viewGroup.removeView(ghostView);
                }
            }
        }
    }

    private static FrameLayout findFrameLayout(ViewGroup viewGroup) {
        while (!(viewGroup instanceof FrameLayout)) {
            ViewParent parent = viewGroup.getParent();
            if (!(parent instanceof ViewGroup)) {
                return null;
            }
            viewGroup = (ViewGroup) parent;
        }
        return (FrameLayout) viewGroup;
    }

    GhostViewApi14(View view) {
        super(view.getContext());
        this.mView = view;
        setLayerType(2, null);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setGhostView(this.mView, this);
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        getLocationOnScreen(iArr);
        this.mView.getLocationOnScreen(iArr2);
        iArr2[0] = (int) (((float) iArr2[0]) - this.mView.getTranslationX());
        iArr2[1] = (int) (((float) iArr2[1]) - this.mView.getTranslationY());
        this.mDeltaX = iArr2[0] - iArr[0];
        this.mDeltaY = iArr2[1] - iArr[1];
        this.mView.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        this.mView.setVisibility(4);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        this.mView.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        this.mView.setVisibility(0);
        setGhostView(this.mView, null);
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        this.mMatrix.set(this.mCurrentMatrix);
        this.mMatrix.postTranslate((float) this.mDeltaX, (float) this.mDeltaY);
        canvas.setMatrix(this.mMatrix);
        this.mView.draw(canvas);
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        this.mView.setVisibility(i == 0 ? 4 : 0);
    }

    public void reserveEndViewTransition(ViewGroup viewGroup, View view) {
        this.mStartParent = viewGroup;
        this.mStartView = view;
    }

    private static void setGhostView(@NonNull View view, GhostViewApi14 ghostViewApi14) {
        view.setTag(R.id.ghost_view, ghostViewApi14);
    }

    static GhostViewApi14 getGhostView(@NonNull View view) {
        return (GhostViewApi14) view.getTag(R.id.ghost_view);
    }
}
