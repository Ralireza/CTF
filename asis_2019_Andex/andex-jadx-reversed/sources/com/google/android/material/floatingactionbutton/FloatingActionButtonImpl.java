package com.google.android.material.floatingactionbutton;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.R;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.ImageMatrixProperty;
import com.google.android.material.animation.MatrixEvaluator;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.internal.CircularBorderDrawable;
import com.google.android.material.internal.StateListAnimator;
import com.google.android.material.internal.VisibilityAwareImageButton;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shadow.ShadowDrawableWrapper;
import com.google.android.material.shadow.ShadowViewDelegate;
import java.util.ArrayList;
import java.util.Iterator;

class FloatingActionButtonImpl {
    static final int ANIM_STATE_HIDING = 1;
    static final int ANIM_STATE_NONE = 0;
    static final int ANIM_STATE_SHOWING = 2;
    static final long ELEVATION_ANIM_DELAY = 100;
    static final long ELEVATION_ANIM_DURATION = 100;
    static final TimeInterpolator ELEVATION_ANIM_INTERPOLATOR = AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;
    static final int[] EMPTY_STATE_SET = new int[0];
    static final int[] ENABLED_STATE_SET = {16842910};
    static final int[] FOCUSED_ENABLED_STATE_SET = {16842908, 16842910};
    private static final float HIDE_ICON_SCALE = 0.0f;
    private static final float HIDE_OPACITY = 0.0f;
    private static final float HIDE_SCALE = 0.0f;
    static final int[] HOVERED_ENABLED_STATE_SET = {16843623, 16842910};
    static final int[] HOVERED_FOCUSED_ENABLED_STATE_SET = {16843623, 16842908, 16842910};
    static final int[] PRESSED_ENABLED_STATE_SET = {16842919, 16842910};
    private static final float SHOW_ICON_SCALE = 1.0f;
    private static final float SHOW_OPACITY = 1.0f;
    private static final float SHOW_SCALE = 1.0f;
    int animState = 0;
    CircularBorderDrawable borderDrawable;
    Drawable contentBackground;
    @Nullable
    Animator currentAnimator;
    @Nullable
    private MotionSpec defaultHideMotionSpec;
    @Nullable
    private MotionSpec defaultShowMotionSpec;
    float elevation;
    private ArrayList<AnimatorListener> hideListeners;
    @Nullable
    MotionSpec hideMotionSpec;
    float hoveredFocusedTranslationZ;
    float imageMatrixScale = 1.0f;
    int maxImageSize;
    private OnPreDrawListener preDrawListener;
    float pressedTranslationZ;
    Drawable rippleDrawable;
    private float rotation;
    ShadowDrawableWrapper shadowDrawable;
    final ShadowViewDelegate shadowViewDelegate;
    Drawable shapeDrawable;
    private ArrayList<AnimatorListener> showListeners;
    @Nullable
    MotionSpec showMotionSpec;
    private final StateListAnimator stateListAnimator;
    private final Matrix tmpMatrix = new Matrix();
    private final Rect tmpRect = new Rect();
    private final RectF tmpRectF1 = new RectF();
    private final RectF tmpRectF2 = new RectF();
    final VisibilityAwareImageButton view;

    private class DisabledElevationAnimation extends ShadowAnimatorImpl {
        /* access modifiers changed from: protected */
        public float getTargetShadowSize() {
            return 0.0f;
        }

        DisabledElevationAnimation() {
            super();
        }
    }

    private class ElevateToHoveredFocusedTranslationZAnimation extends ShadowAnimatorImpl {
        ElevateToHoveredFocusedTranslationZAnimation() {
            super();
        }

        /* access modifiers changed from: protected */
        public float getTargetShadowSize() {
            return FloatingActionButtonImpl.this.elevation + FloatingActionButtonImpl.this.hoveredFocusedTranslationZ;
        }
    }

    private class ElevateToPressedTranslationZAnimation extends ShadowAnimatorImpl {
        ElevateToPressedTranslationZAnimation() {
            super();
        }

        /* access modifiers changed from: protected */
        public float getTargetShadowSize() {
            return FloatingActionButtonImpl.this.elevation + FloatingActionButtonImpl.this.pressedTranslationZ;
        }
    }

    interface InternalVisibilityChangedListener {
        void onHidden();

        void onShown();
    }

    private class ResetElevationAnimation extends ShadowAnimatorImpl {
        ResetElevationAnimation() {
            super();
        }

        /* access modifiers changed from: protected */
        public float getTargetShadowSize() {
            return FloatingActionButtonImpl.this.elevation;
        }
    }

    private abstract class ShadowAnimatorImpl extends AnimatorListenerAdapter implements AnimatorUpdateListener {
        private float shadowSizeEnd;
        private float shadowSizeStart;
        private boolean validValues;

        /* access modifiers changed from: protected */
        public abstract float getTargetShadowSize();

        private ShadowAnimatorImpl() {
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (!this.validValues) {
                this.shadowSizeStart = FloatingActionButtonImpl.this.shadowDrawable.getShadowSize();
                this.shadowSizeEnd = getTargetShadowSize();
                this.validValues = true;
            }
            ShadowDrawableWrapper shadowDrawableWrapper = FloatingActionButtonImpl.this.shadowDrawable;
            float f = this.shadowSizeStart;
            shadowDrawableWrapper.setShadowSize(f + ((this.shadowSizeEnd - f) * valueAnimator.getAnimatedFraction()));
        }

        public void onAnimationEnd(Animator animator) {
            FloatingActionButtonImpl.this.shadowDrawable.setShadowSize(this.shadowSizeEnd);
            this.validValues = false;
        }
    }

    /* access modifiers changed from: 0000 */
    public void onCompatShadowChanged() {
    }

    /* access modifiers changed from: 0000 */
    public void onPaddingUpdated(Rect rect) {
    }

    /* access modifiers changed from: 0000 */
    public boolean requirePreDrawListener() {
        return true;
    }

    FloatingActionButtonImpl(VisibilityAwareImageButton visibilityAwareImageButton, ShadowViewDelegate shadowViewDelegate2) {
        this.view = visibilityAwareImageButton;
        this.shadowViewDelegate = shadowViewDelegate2;
        this.stateListAnimator = new StateListAnimator();
        this.stateListAnimator.addState(PRESSED_ENABLED_STATE_SET, createElevationAnimator(new ElevateToPressedTranslationZAnimation()));
        this.stateListAnimator.addState(HOVERED_FOCUSED_ENABLED_STATE_SET, createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
        this.stateListAnimator.addState(FOCUSED_ENABLED_STATE_SET, createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
        this.stateListAnimator.addState(HOVERED_ENABLED_STATE_SET, createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
        this.stateListAnimator.addState(ENABLED_STATE_SET, createElevationAnimator(new ResetElevationAnimation()));
        this.stateListAnimator.addState(EMPTY_STATE_SET, createElevationAnimator(new DisabledElevationAnimation()));
        this.rotation = this.view.getRotation();
    }

    /* access modifiers changed from: 0000 */
    public void setBackgroundDrawable(ColorStateList colorStateList, Mode mode, ColorStateList colorStateList2, int i) {
        Drawable[] drawableArr;
        this.shapeDrawable = DrawableCompat.wrap(createShapeDrawable());
        DrawableCompat.setTintList(this.shapeDrawable, colorStateList);
        if (mode != null) {
            DrawableCompat.setTintMode(this.shapeDrawable, mode);
        }
        this.rippleDrawable = DrawableCompat.wrap(createShapeDrawable());
        DrawableCompat.setTintList(this.rippleDrawable, RippleUtils.convertToRippleDrawableColor(colorStateList2));
        if (i > 0) {
            this.borderDrawable = createBorderDrawable(i, colorStateList);
            drawableArr = new Drawable[]{this.borderDrawable, this.shapeDrawable, this.rippleDrawable};
        } else {
            this.borderDrawable = null;
            drawableArr = new Drawable[]{this.shapeDrawable, this.rippleDrawable};
        }
        this.contentBackground = new LayerDrawable(drawableArr);
        Context context = this.view.getContext();
        Drawable drawable = this.contentBackground;
        float radius = this.shadowViewDelegate.getRadius();
        float f = this.elevation;
        ShadowDrawableWrapper shadowDrawableWrapper = new ShadowDrawableWrapper(context, drawable, radius, f, f + this.pressedTranslationZ);
        this.shadowDrawable = shadowDrawableWrapper;
        this.shadowDrawable.setAddPaddingForCorners(false);
        this.shadowViewDelegate.setBackgroundDrawable(this.shadowDrawable);
    }

    /* access modifiers changed from: 0000 */
    public void setBackgroundTintList(ColorStateList colorStateList) {
        Drawable drawable = this.shapeDrawable;
        if (drawable != null) {
            DrawableCompat.setTintList(drawable, colorStateList);
        }
        CircularBorderDrawable circularBorderDrawable = this.borderDrawable;
        if (circularBorderDrawable != null) {
            circularBorderDrawable.setBorderTint(colorStateList);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setBackgroundTintMode(Mode mode) {
        Drawable drawable = this.shapeDrawable;
        if (drawable != null) {
            DrawableCompat.setTintMode(drawable, mode);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setRippleColor(ColorStateList colorStateList) {
        Drawable drawable = this.rippleDrawable;
        if (drawable != null) {
            DrawableCompat.setTintList(drawable, RippleUtils.convertToRippleDrawableColor(colorStateList));
        }
    }

    /* access modifiers changed from: 0000 */
    public final void setElevation(float f) {
        if (this.elevation != f) {
            this.elevation = f;
            onElevationsChanged(this.elevation, this.hoveredFocusedTranslationZ, this.pressedTranslationZ);
        }
    }

    /* access modifiers changed from: 0000 */
    public float getElevation() {
        return this.elevation;
    }

    /* access modifiers changed from: 0000 */
    public float getHoveredFocusedTranslationZ() {
        return this.hoveredFocusedTranslationZ;
    }

    /* access modifiers changed from: 0000 */
    public float getPressedTranslationZ() {
        return this.pressedTranslationZ;
    }

    /* access modifiers changed from: 0000 */
    public final void setHoveredFocusedTranslationZ(float f) {
        if (this.hoveredFocusedTranslationZ != f) {
            this.hoveredFocusedTranslationZ = f;
            onElevationsChanged(this.elevation, this.hoveredFocusedTranslationZ, this.pressedTranslationZ);
        }
    }

    /* access modifiers changed from: 0000 */
    public final void setPressedTranslationZ(float f) {
        if (this.pressedTranslationZ != f) {
            this.pressedTranslationZ = f;
            onElevationsChanged(this.elevation, this.hoveredFocusedTranslationZ, this.pressedTranslationZ);
        }
    }

    /* access modifiers changed from: 0000 */
    public final void setMaxImageSize(int i) {
        if (this.maxImageSize != i) {
            this.maxImageSize = i;
            updateImageMatrixScale();
        }
    }

    /* access modifiers changed from: 0000 */
    public final void updateImageMatrixScale() {
        setImageMatrixScale(this.imageMatrixScale);
    }

    /* access modifiers changed from: 0000 */
    public final void setImageMatrixScale(float f) {
        this.imageMatrixScale = f;
        Matrix matrix = this.tmpMatrix;
        calculateImageMatrixFromScale(f, matrix);
        this.view.setImageMatrix(matrix);
    }

    private void calculateImageMatrixFromScale(float f, Matrix matrix) {
        matrix.reset();
        Drawable drawable = this.view.getDrawable();
        if (drawable != null && this.maxImageSize != 0) {
            RectF rectF = this.tmpRectF1;
            RectF rectF2 = this.tmpRectF2;
            rectF.set(0.0f, 0.0f, (float) drawable.getIntrinsicWidth(), (float) drawable.getIntrinsicHeight());
            int i = this.maxImageSize;
            rectF2.set(0.0f, 0.0f, (float) i, (float) i);
            matrix.setRectToRect(rectF, rectF2, ScaleToFit.CENTER);
            int i2 = this.maxImageSize;
            matrix.postScale(f, f, ((float) i2) / 2.0f, ((float) i2) / 2.0f);
        }
    }

    /* access modifiers changed from: 0000 */
    @Nullable
    public final MotionSpec getShowMotionSpec() {
        return this.showMotionSpec;
    }

    /* access modifiers changed from: 0000 */
    public final void setShowMotionSpec(@Nullable MotionSpec motionSpec) {
        this.showMotionSpec = motionSpec;
    }

    /* access modifiers changed from: 0000 */
    @Nullable
    public final MotionSpec getHideMotionSpec() {
        return this.hideMotionSpec;
    }

    /* access modifiers changed from: 0000 */
    public final void setHideMotionSpec(@Nullable MotionSpec motionSpec) {
        this.hideMotionSpec = motionSpec;
    }

    /* access modifiers changed from: 0000 */
    public void onElevationsChanged(float f, float f2, float f3) {
        ShadowDrawableWrapper shadowDrawableWrapper = this.shadowDrawable;
        if (shadowDrawableWrapper != null) {
            shadowDrawableWrapper.setShadowSize(f, this.pressedTranslationZ + f);
            updatePadding();
        }
    }

    /* access modifiers changed from: 0000 */
    public void onDrawableStateChanged(int[] iArr) {
        this.stateListAnimator.setState(iArr);
    }

    /* access modifiers changed from: 0000 */
    public void jumpDrawableToCurrentState() {
        this.stateListAnimator.jumpToCurrentState();
    }

    /* access modifiers changed from: 0000 */
    public void addOnShowAnimationListener(@NonNull AnimatorListener animatorListener) {
        if (this.showListeners == null) {
            this.showListeners = new ArrayList<>();
        }
        this.showListeners.add(animatorListener);
    }

    /* access modifiers changed from: 0000 */
    public void removeOnShowAnimationListener(@NonNull AnimatorListener animatorListener) {
        ArrayList<AnimatorListener> arrayList = this.showListeners;
        if (arrayList != null) {
            arrayList.remove(animatorListener);
        }
    }

    public void addOnHideAnimationListener(@NonNull AnimatorListener animatorListener) {
        if (this.hideListeners == null) {
            this.hideListeners = new ArrayList<>();
        }
        this.hideListeners.add(animatorListener);
    }

    public void removeOnHideAnimationListener(@NonNull AnimatorListener animatorListener) {
        ArrayList<AnimatorListener> arrayList = this.hideListeners;
        if (arrayList != null) {
            arrayList.remove(animatorListener);
        }
    }

    /* access modifiers changed from: 0000 */
    public void hide(@Nullable final InternalVisibilityChangedListener internalVisibilityChangedListener, final boolean z) {
        if (!isOrWillBeHidden()) {
            Animator animator = this.currentAnimator;
            if (animator != null) {
                animator.cancel();
            }
            if (shouldAnimateVisibilityChange()) {
                MotionSpec motionSpec = this.hideMotionSpec;
                if (motionSpec == null) {
                    motionSpec = getDefaultHideMotionSpec();
                }
                AnimatorSet createAnimator = createAnimator(motionSpec, 0.0f, 0.0f, 0.0f);
                createAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean cancelled;

                    public void onAnimationStart(Animator animator) {
                        FloatingActionButtonImpl.this.view.internalSetVisibility(0, z);
                        FloatingActionButtonImpl floatingActionButtonImpl = FloatingActionButtonImpl.this;
                        floatingActionButtonImpl.animState = 1;
                        floatingActionButtonImpl.currentAnimator = animator;
                        this.cancelled = false;
                    }

                    public void onAnimationCancel(Animator animator) {
                        this.cancelled = true;
                    }

                    public void onAnimationEnd(Animator animator) {
                        FloatingActionButtonImpl floatingActionButtonImpl = FloatingActionButtonImpl.this;
                        floatingActionButtonImpl.animState = 0;
                        floatingActionButtonImpl.currentAnimator = null;
                        if (!this.cancelled) {
                            floatingActionButtonImpl.view.internalSetVisibility(z ? 8 : 4, z);
                            InternalVisibilityChangedListener internalVisibilityChangedListener = internalVisibilityChangedListener;
                            if (internalVisibilityChangedListener != null) {
                                internalVisibilityChangedListener.onHidden();
                            }
                        }
                    }
                });
                ArrayList<AnimatorListener> arrayList = this.hideListeners;
                if (arrayList != null) {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        createAnimator.addListener((AnimatorListener) it.next());
                    }
                }
                createAnimator.start();
            } else {
                this.view.internalSetVisibility(z ? 8 : 4, z);
                if (internalVisibilityChangedListener != null) {
                    internalVisibilityChangedListener.onHidden();
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void show(@Nullable final InternalVisibilityChangedListener internalVisibilityChangedListener, final boolean z) {
        if (!isOrWillBeShown()) {
            Animator animator = this.currentAnimator;
            if (animator != null) {
                animator.cancel();
            }
            if (shouldAnimateVisibilityChange()) {
                if (this.view.getVisibility() != 0) {
                    this.view.setAlpha(0.0f);
                    this.view.setScaleY(0.0f);
                    this.view.setScaleX(0.0f);
                    setImageMatrixScale(0.0f);
                }
                MotionSpec motionSpec = this.showMotionSpec;
                if (motionSpec == null) {
                    motionSpec = getDefaultShowMotionSpec();
                }
                AnimatorSet createAnimator = createAnimator(motionSpec, 1.0f, 1.0f, 1.0f);
                createAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animator) {
                        FloatingActionButtonImpl.this.view.internalSetVisibility(0, z);
                        FloatingActionButtonImpl floatingActionButtonImpl = FloatingActionButtonImpl.this;
                        floatingActionButtonImpl.animState = 2;
                        floatingActionButtonImpl.currentAnimator = animator;
                    }

                    public void onAnimationEnd(Animator animator) {
                        FloatingActionButtonImpl floatingActionButtonImpl = FloatingActionButtonImpl.this;
                        floatingActionButtonImpl.animState = 0;
                        floatingActionButtonImpl.currentAnimator = null;
                        InternalVisibilityChangedListener internalVisibilityChangedListener = internalVisibilityChangedListener;
                        if (internalVisibilityChangedListener != null) {
                            internalVisibilityChangedListener.onShown();
                        }
                    }
                });
                ArrayList<AnimatorListener> arrayList = this.showListeners;
                if (arrayList != null) {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        createAnimator.addListener((AnimatorListener) it.next());
                    }
                }
                createAnimator.start();
            } else {
                this.view.internalSetVisibility(0, z);
                this.view.setAlpha(1.0f);
                this.view.setScaleY(1.0f);
                this.view.setScaleX(1.0f);
                setImageMatrixScale(1.0f);
                if (internalVisibilityChangedListener != null) {
                    internalVisibilityChangedListener.onShown();
                }
            }
        }
    }

    private MotionSpec getDefaultShowMotionSpec() {
        if (this.defaultShowMotionSpec == null) {
            this.defaultShowMotionSpec = MotionSpec.createFromResource(this.view.getContext(), R.animator.design_fab_show_motion_spec);
        }
        return this.defaultShowMotionSpec;
    }

    private MotionSpec getDefaultHideMotionSpec() {
        if (this.defaultHideMotionSpec == null) {
            this.defaultHideMotionSpec = MotionSpec.createFromResource(this.view.getContext(), R.animator.design_fab_hide_motion_spec);
        }
        return this.defaultHideMotionSpec;
    }

    @NonNull
    private AnimatorSet createAnimator(@NonNull MotionSpec motionSpec, float f, float f2, float f3) {
        ArrayList arrayList = new ArrayList();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.view, View.ALPHA, new float[]{f});
        motionSpec.getTiming("opacity").apply(ofFloat);
        arrayList.add(ofFloat);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.view, View.SCALE_X, new float[]{f2});
        String str = "scale";
        motionSpec.getTiming(str).apply(ofFloat2);
        arrayList.add(ofFloat2);
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.view, View.SCALE_Y, new float[]{f2});
        motionSpec.getTiming(str).apply(ofFloat3);
        arrayList.add(ofFloat3);
        calculateImageMatrixFromScale(f3, this.tmpMatrix);
        ObjectAnimator ofObject = ObjectAnimator.ofObject(this.view, new ImageMatrixProperty(), new MatrixEvaluator(), new Matrix[]{new Matrix(this.tmpMatrix)});
        motionSpec.getTiming("iconScale").apply(ofObject);
        arrayList.add(ofObject);
        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSetCompat.playTogether(animatorSet, arrayList);
        return animatorSet;
    }

    /* access modifiers changed from: 0000 */
    public final Drawable getContentBackground() {
        return this.contentBackground;
    }

    /* access modifiers changed from: 0000 */
    public final void updatePadding() {
        Rect rect = this.tmpRect;
        getPadding(rect);
        onPaddingUpdated(rect);
        this.shadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
    }

    /* access modifiers changed from: 0000 */
    public void getPadding(Rect rect) {
        this.shadowDrawable.getPadding(rect);
    }

    /* access modifiers changed from: 0000 */
    public void onAttachedToWindow() {
        if (requirePreDrawListener()) {
            ensurePreDrawListener();
            this.view.getViewTreeObserver().addOnPreDrawListener(this.preDrawListener);
        }
    }

    /* access modifiers changed from: 0000 */
    public void onDetachedFromWindow() {
        if (this.preDrawListener != null) {
            this.view.getViewTreeObserver().removeOnPreDrawListener(this.preDrawListener);
            this.preDrawListener = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public CircularBorderDrawable createBorderDrawable(int i, ColorStateList colorStateList) {
        Context context = this.view.getContext();
        CircularBorderDrawable newCircularDrawable = newCircularDrawable();
        newCircularDrawable.setGradientColors(ContextCompat.getColor(context, R.color.design_fab_stroke_top_outer_color), ContextCompat.getColor(context, R.color.design_fab_stroke_top_inner_color), ContextCompat.getColor(context, R.color.design_fab_stroke_end_inner_color), ContextCompat.getColor(context, R.color.design_fab_stroke_end_outer_color));
        newCircularDrawable.setBorderWidth((float) i);
        newCircularDrawable.setBorderTint(colorStateList);
        return newCircularDrawable;
    }

    /* access modifiers changed from: 0000 */
    public CircularBorderDrawable newCircularDrawable() {
        return new CircularBorderDrawable();
    }

    /* access modifiers changed from: 0000 */
    public void onPreDraw() {
        float rotation2 = this.view.getRotation();
        if (this.rotation != rotation2) {
            this.rotation = rotation2;
            updateFromViewRotation();
        }
    }

    private void ensurePreDrawListener() {
        if (this.preDrawListener == null) {
            this.preDrawListener = new OnPreDrawListener() {
                public boolean onPreDraw() {
                    FloatingActionButtonImpl.this.onPreDraw();
                    return true;
                }
            };
        }
    }

    /* access modifiers changed from: 0000 */
    public GradientDrawable createShapeDrawable() {
        GradientDrawable newGradientDrawableForShape = newGradientDrawableForShape();
        newGradientDrawableForShape.setShape(1);
        newGradientDrawableForShape.setColor(-1);
        return newGradientDrawableForShape;
    }

    /* access modifiers changed from: 0000 */
    public GradientDrawable newGradientDrawableForShape() {
        return new GradientDrawable();
    }

    /* access modifiers changed from: 0000 */
    public boolean isOrWillBeShown() {
        boolean z = false;
        if (this.view.getVisibility() != 0) {
            if (this.animState == 2) {
                z = true;
            }
            return z;
        }
        if (this.animState != 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public boolean isOrWillBeHidden() {
        boolean z = false;
        if (this.view.getVisibility() == 0) {
            if (this.animState == 1) {
                z = true;
            }
            return z;
        }
        if (this.animState != 2) {
            z = true;
        }
        return z;
    }

    private ValueAnimator createElevationAnimator(@NonNull ShadowAnimatorImpl shadowAnimatorImpl) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
        valueAnimator.setDuration(100);
        valueAnimator.addListener(shadowAnimatorImpl);
        valueAnimator.addUpdateListener(shadowAnimatorImpl);
        valueAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        return valueAnimator;
    }

    private boolean shouldAnimateVisibilityChange() {
        return ViewCompat.isLaidOut(this.view) && !this.view.isInEditMode();
    }

    private void updateFromViewRotation() {
        if (VERSION.SDK_INT == 19) {
            if (this.rotation % 90.0f != 0.0f) {
                if (this.view.getLayerType() != 1) {
                    this.view.setLayerType(1, null);
                }
            } else if (this.view.getLayerType() != 0) {
                this.view.setLayerType(0, null);
            }
        }
        ShadowDrawableWrapper shadowDrawableWrapper = this.shadowDrawable;
        if (shadowDrawableWrapper != null) {
            shadowDrawableWrapper.setRotation(-this.rotation);
        }
        CircularBorderDrawable circularBorderDrawable = this.borderDrawable;
        if (circularBorderDrawable != null) {
            circularBorderDrawable.setRotation(-this.rotation);
        }
    }
}
