package com.google.android.material.tabs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools.Pool;
import androidx.core.util.Pools.SimplePool;
import androidx.core.util.Pools.SynchronizedPool;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.DecorView;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.google.android.material.R;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

@DecorView
public class TabLayout extends HorizontalScrollView {
    private static final int ANIMATION_DURATION = 300;
    @Dimension(unit = 0)
    static final int DEFAULT_GAP_TEXT_ICON = 8;
    @Dimension(unit = 0)
    private static final int DEFAULT_HEIGHT = 48;
    @Dimension(unit = 0)
    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;
    @Dimension(unit = 0)
    static final int FIXED_WRAP_GUTTER_MIN = 16;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int INDICATOR_GRAVITY_BOTTOM = 0;
    public static final int INDICATOR_GRAVITY_CENTER = 1;
    public static final int INDICATOR_GRAVITY_STRETCH = 3;
    public static final int INDICATOR_GRAVITY_TOP = 2;
    private static final int INVALID_WIDTH = -1;
    @Dimension(unit = 0)
    private static final int MIN_INDICATOR_WIDTH = 24;
    public static final int MODE_FIXED = 1;
    public static final int MODE_SCROLLABLE = 0;
    @Dimension(unit = 0)
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private static final Pool<Tab> tabPool = new SynchronizedPool(16);
    private AdapterChangeListener adapterChangeListener;
    private int contentInsetStart;
    private BaseOnTabSelectedListener currentVpSelectedListener;
    boolean inlineLabel;
    int mode;
    private TabLayoutOnPageChangeListener pageChangeListener;
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private final int requestedTabMaxWidth;
    private final int requestedTabMinWidth;
    private ValueAnimator scrollAnimator;
    private final int scrollableTabMinWidth;
    private BaseOnTabSelectedListener selectedListener;
    private final ArrayList<BaseOnTabSelectedListener> selectedListeners;
    private Tab selectedTab;
    private boolean setupViewPagerImplicitly;
    private final SlidingTabIndicator slidingTabIndicator;
    final int tabBackgroundResId;
    int tabGravity;
    ColorStateList tabIconTint;
    android.graphics.PorterDuff.Mode tabIconTintMode;
    int tabIndicatorAnimationDuration;
    boolean tabIndicatorFullWidth;
    int tabIndicatorGravity;
    int tabMaxWidth;
    int tabPaddingBottom;
    int tabPaddingEnd;
    int tabPaddingStart;
    int tabPaddingTop;
    ColorStateList tabRippleColorStateList;
    @Nullable
    Drawable tabSelectedIndicator;
    int tabTextAppearance;
    ColorStateList tabTextColors;
    float tabTextMultiLineSize;
    float tabTextSize;
    /* access modifiers changed from: private */
    public final RectF tabViewContentBounds;
    private final Pool<TabView> tabViewPool;
    private final ArrayList<Tab> tabs;
    boolean unboundedRipple;
    ViewPager viewPager;

    private class AdapterChangeListener implements OnAdapterChangeListener {
        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter pagerAdapter, @Nullable PagerAdapter pagerAdapter2) {
            if (TabLayout.this.viewPager == viewPager) {
                TabLayout.this.setPagerAdapter(pagerAdapter2, this.autoRefresh);
            }
        }

        /* access modifiers changed from: 0000 */
        public void setAutoRefresh(boolean z) {
            this.autoRefresh = z;
        }
    }

    public interface BaseOnTabSelectedListener<T extends Tab> {
        void onTabReselected(T t);

        void onTabSelected(T t);

        void onTabUnselected(T t);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        public void onChanged() {
            TabLayout.this.populateFromPagerAdapter();
        }

        public void onInvalidated() {
            TabLayout.this.populateFromPagerAdapter();
        }
    }

    private class SlidingTabIndicator extends LinearLayout {
        private final GradientDrawable defaultSelectionIndicator;
        private ValueAnimator indicatorAnimator;
        private int indicatorLeft = -1;
        private int indicatorRight = -1;
        private int layoutDirection = -1;
        private int selectedIndicatorHeight;
        private final Paint selectedIndicatorPaint;
        int selectedPosition = -1;
        float selectionOffset;

        SlidingTabIndicator(Context context) {
            super(context);
            setWillNotDraw(false);
            this.selectedIndicatorPaint = new Paint();
            this.defaultSelectionIndicator = new GradientDrawable();
        }

        /* access modifiers changed from: 0000 */
        public void setSelectedIndicatorColor(int i) {
            if (this.selectedIndicatorPaint.getColor() != i) {
                this.selectedIndicatorPaint.setColor(i);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* access modifiers changed from: 0000 */
        public void setSelectedIndicatorHeight(int i) {
            if (this.selectedIndicatorHeight != i) {
                this.selectedIndicatorHeight = i;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean childrenNeedLayout() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: 0000 */
        public void setIndicatorPositionFromTabPosition(int i, float f) {
            ValueAnimator valueAnimator = this.indicatorAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.indicatorAnimator.cancel();
            }
            this.selectedPosition = i;
            this.selectionOffset = f;
            updateIndicatorPosition();
        }

        /* access modifiers changed from: 0000 */
        public float getIndicatorPosition() {
            return ((float) this.selectedPosition) + this.selectionOffset;
        }

        public void onRtlPropertiesChanged(int i) {
            super.onRtlPropertiesChanged(i);
            if (VERSION.SDK_INT < 23 && this.layoutDirection != i) {
                requestLayout();
                this.layoutDirection = i;
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            if (MeasureSpec.getMode(i) == 1073741824) {
                boolean z = true;
                if (TabLayout.this.mode == 1 && TabLayout.this.tabGravity == 1) {
                    int childCount = getChildCount();
                    int i3 = 0;
                    for (int i4 = 0; i4 < childCount; i4++) {
                        View childAt = getChildAt(i4);
                        if (childAt.getVisibility() == 0) {
                            i3 = Math.max(i3, childAt.getMeasuredWidth());
                        }
                    }
                    if (i3 > 0) {
                        if (i3 * childCount <= getMeasuredWidth() - (TabLayout.this.dpToPx(16) * 2)) {
                            boolean z2 = false;
                            for (int i5 = 0; i5 < childCount; i5++) {
                                LayoutParams layoutParams = (LayoutParams) getChildAt(i5).getLayoutParams();
                                if (layoutParams.width != i3 || layoutParams.weight != 0.0f) {
                                    layoutParams.width = i3;
                                    layoutParams.weight = 0.0f;
                                    z2 = true;
                                }
                            }
                            z = z2;
                        } else {
                            TabLayout tabLayout = TabLayout.this;
                            tabLayout.tabGravity = 0;
                            tabLayout.updateTabViews(false);
                        }
                        if (z) {
                            super.onMeasure(i, i2);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            ValueAnimator valueAnimator = this.indicatorAnimator;
            if (valueAnimator == null || !valueAnimator.isRunning()) {
                updateIndicatorPosition();
                return;
            }
            this.indicatorAnimator.cancel();
            animateIndicatorToPosition(this.selectedPosition, Math.round((1.0f - this.indicatorAnimator.getAnimatedFraction()) * ((float) this.indicatorAnimator.getDuration())));
        }

        private void updateIndicatorPosition() {
            int i;
            int i2;
            View childAt = getChildAt(this.selectedPosition);
            if (childAt == null || childAt.getWidth() <= 0) {
                i2 = -1;
                i = -1;
            } else {
                i2 = childAt.getLeft();
                i = childAt.getRight();
                if (!TabLayout.this.tabIndicatorFullWidth && (childAt instanceof TabView)) {
                    calculateTabViewContentBounds((TabView) childAt, TabLayout.this.tabViewContentBounds);
                    i2 = (int) TabLayout.this.tabViewContentBounds.left;
                    i = (int) TabLayout.this.tabViewContentBounds.right;
                }
                if (this.selectionOffset > 0.0f && this.selectedPosition < getChildCount() - 1) {
                    View childAt2 = getChildAt(this.selectedPosition + 1);
                    int left = childAt2.getLeft();
                    int right = childAt2.getRight();
                    if (!TabLayout.this.tabIndicatorFullWidth && (childAt2 instanceof TabView)) {
                        calculateTabViewContentBounds((TabView) childAt2, TabLayout.this.tabViewContentBounds);
                        left = (int) TabLayout.this.tabViewContentBounds.left;
                        right = (int) TabLayout.this.tabViewContentBounds.right;
                    }
                    float f = this.selectionOffset;
                    i2 = (int) ((((float) left) * f) + ((1.0f - f) * ((float) i2)));
                    i = (int) ((((float) right) * f) + ((1.0f - f) * ((float) i)));
                }
            }
            setIndicatorPosition(i2, i);
        }

        /* access modifiers changed from: 0000 */
        public void setIndicatorPosition(int i, int i2) {
            if (i != this.indicatorLeft || i2 != this.indicatorRight) {
                this.indicatorLeft = i;
                this.indicatorRight = i2;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* access modifiers changed from: 0000 */
        public void animateIndicatorToPosition(final int i, int i2) {
            ValueAnimator valueAnimator = this.indicatorAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.indicatorAnimator.cancel();
            }
            View childAt = getChildAt(i);
            if (childAt == null) {
                updateIndicatorPosition();
                return;
            }
            int left = childAt.getLeft();
            int right = childAt.getRight();
            if (!TabLayout.this.tabIndicatorFullWidth && (childAt instanceof TabView)) {
                calculateTabViewContentBounds((TabView) childAt, TabLayout.this.tabViewContentBounds);
                left = (int) TabLayout.this.tabViewContentBounds.left;
                right = (int) TabLayout.this.tabViewContentBounds.right;
            }
            final int i3 = left;
            final int i4 = right;
            final int i5 = this.indicatorLeft;
            final int i6 = this.indicatorRight;
            if (!(i5 == i3 && i6 == i4)) {
                ValueAnimator valueAnimator2 = new ValueAnimator();
                this.indicatorAnimator = valueAnimator2;
                valueAnimator2.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                valueAnimator2.setDuration((long) i2);
                valueAnimator2.setFloatValues(new float[]{0.0f, 1.0f});
                AnonymousClass1 r3 = new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        SlidingTabIndicator.this.setIndicatorPosition(AnimationUtils.lerp(i5, i3, animatedFraction), AnimationUtils.lerp(i6, i4, animatedFraction));
                    }
                };
                valueAnimator2.addUpdateListener(r3);
                valueAnimator2.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        SlidingTabIndicator slidingTabIndicator = SlidingTabIndicator.this;
                        slidingTabIndicator.selectedPosition = i;
                        slidingTabIndicator.selectionOffset = 0.0f;
                    }
                });
                valueAnimator2.start();
            }
        }

        private void calculateTabViewContentBounds(TabView tabView, RectF rectF) {
            int access$500 = tabView.getContentWidth();
            if (access$500 < TabLayout.this.dpToPx(24)) {
                access$500 = TabLayout.this.dpToPx(24);
            }
            int left = (tabView.getLeft() + tabView.getRight()) / 2;
            int i = access$500 / 2;
            rectF.set((float) (left - i), 0.0f, (float) (left + i), 0.0f);
        }

        public void draw(Canvas canvas) {
            int i = 0;
            int intrinsicHeight = TabLayout.this.tabSelectedIndicator != null ? TabLayout.this.tabSelectedIndicator.getIntrinsicHeight() : 0;
            int i2 = this.selectedIndicatorHeight;
            if (i2 >= 0) {
                intrinsicHeight = i2;
            }
            int i3 = TabLayout.this.tabIndicatorGravity;
            if (i3 == 0) {
                i = getHeight() - intrinsicHeight;
                intrinsicHeight = getHeight();
            } else if (i3 == 1) {
                i = (getHeight() - intrinsicHeight) / 2;
                intrinsicHeight = (getHeight() + intrinsicHeight) / 2;
            } else if (i3 != 2) {
                intrinsicHeight = i3 != 3 ? 0 : getHeight();
            }
            int i4 = this.indicatorLeft;
            if (i4 >= 0 && this.indicatorRight > i4) {
                Drawable wrap = DrawableCompat.wrap(TabLayout.this.tabSelectedIndicator != null ? TabLayout.this.tabSelectedIndicator : this.defaultSelectionIndicator);
                wrap.setBounds(this.indicatorLeft, i, this.indicatorRight, intrinsicHeight);
                if (this.selectedIndicatorPaint != null) {
                    if (VERSION.SDK_INT == 21) {
                        wrap.setColorFilter(this.selectedIndicatorPaint.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        DrawableCompat.setTint(wrap, this.selectedIndicatorPaint.getColor());
                    }
                }
                wrap.draw(canvas);
            }
            super.draw(canvas);
        }
    }

    public static class Tab {
        public static final int INVALID_POSITION = -1;
        /* access modifiers changed from: private */
        public CharSequence contentDesc;
        private View customView;
        private Drawable icon;
        public TabLayout parent;
        private int position = -1;
        private Object tag;
        /* access modifiers changed from: private */
        public CharSequence text;
        public TabView view;

        @Nullable
        public Object getTag() {
            return this.tag;
        }

        @NonNull
        public Tab setTag(@Nullable Object obj) {
            this.tag = obj;
            return this;
        }

        @Nullable
        public View getCustomView() {
            return this.customView;
        }

        @NonNull
        public Tab setCustomView(@Nullable View view2) {
            this.customView = view2;
            updateView();
            return this;
        }

        @NonNull
        public Tab setCustomView(@LayoutRes int i) {
            return setCustomView(LayoutInflater.from(this.view.getContext()).inflate(i, this.view, false));
        }

        @Nullable
        public Drawable getIcon() {
            return this.icon;
        }

        public int getPosition() {
            return this.position;
        }

        /* access modifiers changed from: 0000 */
        public void setPosition(int i) {
            this.position = i;
        }

        @Nullable
        public CharSequence getText() {
            return this.text;
        }

        @NonNull
        public Tab setIcon(@Nullable Drawable drawable) {
            this.icon = drawable;
            updateView();
            return this;
        }

        @NonNull
        public Tab setIcon(@DrawableRes int i) {
            TabLayout tabLayout = this.parent;
            if (tabLayout != null) {
                return setIcon(AppCompatResources.getDrawable(tabLayout.getContext(), i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        @NonNull
        public Tab setText(@Nullable CharSequence charSequence) {
            if (TextUtils.isEmpty(this.contentDesc) && !TextUtils.isEmpty(charSequence)) {
                this.view.setContentDescription(charSequence);
            }
            this.text = charSequence;
            updateView();
            return this;
        }

        @NonNull
        public Tab setText(@StringRes int i) {
            TabLayout tabLayout = this.parent;
            if (tabLayout != null) {
                return setText(tabLayout.getResources().getText(i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        public void select() {
            TabLayout tabLayout = this.parent;
            if (tabLayout != null) {
                tabLayout.selectTab(this);
                return;
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        public boolean isSelected() {
            TabLayout tabLayout = this.parent;
            if (tabLayout != null) {
                return tabLayout.getSelectedTabPosition() == this.position;
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        @NonNull
        public Tab setContentDescription(@StringRes int i) {
            TabLayout tabLayout = this.parent;
            if (tabLayout != null) {
                return setContentDescription(tabLayout.getResources().getText(i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        @NonNull
        public Tab setContentDescription(@Nullable CharSequence charSequence) {
            this.contentDesc = charSequence;
            updateView();
            return this;
        }

        @Nullable
        public CharSequence getContentDescription() {
            TabView tabView = this.view;
            if (tabView == null) {
                return null;
            }
            return tabView.getContentDescription();
        }

        /* access modifiers changed from: 0000 */
        public void updateView() {
            TabView tabView = this.view;
            if (tabView != null) {
                tabView.update();
            }
        }

        /* access modifiers changed from: 0000 */
        public void reset() {
            this.parent = null;
            this.view = null;
            this.tag = null;
            this.icon = null;
            this.text = null;
            this.contentDesc = null;
            this.position = -1;
            this.customView = null;
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorGravity {
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private int previousScrollState;
        private int scrollState;
        private final WeakReference<TabLayout> tabLayoutRef;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            this.tabLayoutRef = new WeakReference<>(tabLayout);
        }

        public void onPageScrollStateChanged(int i) {
            this.previousScrollState = this.scrollState;
            this.scrollState = i;
        }

        public void onPageScrolled(int i, float f, int i2) {
            TabLayout tabLayout = (TabLayout) this.tabLayoutRef.get();
            if (tabLayout != null) {
                boolean z = false;
                boolean z2 = this.scrollState != 2 || this.previousScrollState == 1;
                if (!(this.scrollState == 2 && this.previousScrollState == 0)) {
                    z = true;
                }
                tabLayout.setScrollPosition(i, f, z2, z);
            }
        }

        public void onPageSelected(int i) {
            TabLayout tabLayout = (TabLayout) this.tabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != i && i < tabLayout.getTabCount()) {
                int i2 = this.scrollState;
                tabLayout.selectTab(tabLayout.getTabAt(i), i2 == 0 || (i2 == 2 && this.previousScrollState == 0));
            }
        }

        /* access modifiers changed from: 0000 */
        public void reset() {
            this.scrollState = 0;
            this.previousScrollState = 0;
        }
    }

    class TabView extends LinearLayout {
        @Nullable
        private Drawable baseBackgroundDrawable;
        private ImageView customIconView;
        private TextView customTextView;
        private View customView;
        private int defaultMaxLines = 2;
        private ImageView iconView;
        private Tab tab;
        private TextView textView;

        public TabView(Context context) {
            super(context);
            updateBackgroundDrawable(context);
            ViewCompat.setPaddingRelative(this, TabLayout.this.tabPaddingStart, TabLayout.this.tabPaddingTop, TabLayout.this.tabPaddingEnd, TabLayout.this.tabPaddingBottom);
            setGravity(17);
            setOrientation(TabLayout.this.inlineLabel ^ true ? 1 : 0);
            setClickable(true);
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
        }

        /* JADX WARNING: type inference failed for: r7v2, types: [android.graphics.drawable.Drawable] */
        /* JADX WARNING: type inference failed for: r1v1, types: [android.graphics.drawable.LayerDrawable] */
        /* JADX WARNING: type inference failed for: r7v5 */
        /* JADX WARNING: type inference failed for: r2v1, types: [android.graphics.drawable.RippleDrawable] */
        /* JADX WARNING: type inference failed for: r7v7 */
        /* access modifiers changed from: private */
        /* JADX WARNING: Multi-variable type inference failed */
        /* JADX WARNING: Unknown variable types count: 2 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updateBackgroundDrawable(android.content.Context r7) {
            /*
                r6 = this;
                com.google.android.material.tabs.TabLayout r0 = com.google.android.material.tabs.TabLayout.this
                int r0 = r0.tabBackgroundResId
                r1 = 0
                if (r0 == 0) goto L_0x0025
                com.google.android.material.tabs.TabLayout r0 = com.google.android.material.tabs.TabLayout.this
                int r0 = r0.tabBackgroundResId
                android.graphics.drawable.Drawable r7 = androidx.appcompat.content.res.AppCompatResources.getDrawable(r7, r0)
                r6.baseBackgroundDrawable = r7
                android.graphics.drawable.Drawable r7 = r6.baseBackgroundDrawable
                if (r7 == 0) goto L_0x0027
                boolean r7 = r7.isStateful()
                if (r7 == 0) goto L_0x0027
                android.graphics.drawable.Drawable r7 = r6.baseBackgroundDrawable
                int[] r0 = r6.getDrawableState()
                r7.setState(r0)
                goto L_0x0027
            L_0x0025:
                r6.baseBackgroundDrawable = r1
            L_0x0027:
                android.graphics.drawable.GradientDrawable r7 = new android.graphics.drawable.GradientDrawable
                r7.<init>()
                r0 = r7
                android.graphics.drawable.GradientDrawable r0 = (android.graphics.drawable.GradientDrawable) r0
                r2 = 0
                r0.setColor(r2)
                com.google.android.material.tabs.TabLayout r0 = com.google.android.material.tabs.TabLayout.this
                android.content.res.ColorStateList r0 = r0.tabRippleColorStateList
                if (r0 == 0) goto L_0x0080
                android.graphics.drawable.GradientDrawable r0 = new android.graphics.drawable.GradientDrawable
                r0.<init>()
                r3 = 925353388(0x3727c5ac, float:1.0E-5)
                r0.setCornerRadius(r3)
                r3 = -1
                r0.setColor(r3)
                com.google.android.material.tabs.TabLayout r3 = com.google.android.material.tabs.TabLayout.this
                android.content.res.ColorStateList r3 = r3.tabRippleColorStateList
                android.content.res.ColorStateList r3 = com.google.android.material.ripple.RippleUtils.convertToRippleDrawableColor(r3)
                int r4 = android.os.Build.VERSION.SDK_INT
                r5 = 21
                if (r4 < r5) goto L_0x006b
                android.graphics.drawable.RippleDrawable r2 = new android.graphics.drawable.RippleDrawable
                com.google.android.material.tabs.TabLayout r4 = com.google.android.material.tabs.TabLayout.this
                boolean r4 = r4.unboundedRipple
                if (r4 == 0) goto L_0x005f
                r7 = r1
            L_0x005f:
                com.google.android.material.tabs.TabLayout r4 = com.google.android.material.tabs.TabLayout.this
                boolean r4 = r4.unboundedRipple
                if (r4 == 0) goto L_0x0066
                r0 = r1
            L_0x0066:
                r2.<init>(r3, r7, r0)
                r7 = r2
                goto L_0x0080
            L_0x006b:
                android.graphics.drawable.Drawable r0 = androidx.core.graphics.drawable.DrawableCompat.wrap(r0)
                androidx.core.graphics.drawable.DrawableCompat.setTintList(r0, r3)
                android.graphics.drawable.LayerDrawable r1 = new android.graphics.drawable.LayerDrawable
                r3 = 2
                android.graphics.drawable.Drawable[] r3 = new android.graphics.drawable.Drawable[r3]
                r3[r2] = r7
                r7 = 1
                r3[r7] = r0
                r1.<init>(r3)
                r7 = r1
            L_0x0080:
                androidx.core.view.ViewCompat.setBackground(r6, r7)
                com.google.android.material.tabs.TabLayout r7 = com.google.android.material.tabs.TabLayout.this
                r7.invalidate()
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.tabs.TabLayout.TabView.updateBackgroundDrawable(android.content.Context):void");
        }

        /* access modifiers changed from: private */
        public void drawBackground(Canvas canvas) {
            Drawable drawable = this.baseBackgroundDrawable;
            if (drawable != null) {
                drawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                this.baseBackgroundDrawable.draw(canvas);
            }
        }

        /* access modifiers changed from: protected */
        public void drawableStateChanged() {
            super.drawableStateChanged();
            int[] drawableState = getDrawableState();
            Drawable drawable = this.baseBackgroundDrawable;
            boolean z = false;
            if (drawable != null && drawable.isStateful()) {
                z = false | this.baseBackgroundDrawable.setState(drawableState);
            }
            if (z) {
                invalidate();
                TabLayout.this.invalidate();
            }
        }

        public boolean performClick() {
            boolean performClick = super.performClick();
            if (this.tab == null) {
                return performClick;
            }
            if (!performClick) {
                playSoundEffect(0);
            }
            this.tab.select();
            return true;
        }

        public void setSelected(boolean z) {
            boolean z2 = isSelected() != z;
            super.setSelected(z);
            if (z2 && z && VERSION.SDK_INT < 16) {
                sendAccessibilityEvent(4);
            }
            TextView textView2 = this.textView;
            if (textView2 != null) {
                textView2.setSelected(z);
            }
            ImageView imageView = this.iconView;
            if (imageView != null) {
                imageView.setSelected(z);
            }
            View view = this.customView;
            if (view != null) {
                view.setSelected(z);
            }
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(accessibilityEvent);
            accessibilityEvent.setClassName(androidx.appcompat.app.ActionBar.Tab.class.getName());
        }

        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.setClassName(androidx.appcompat.app.ActionBar.Tab.class.getName());
        }

        public void onMeasure(int i, int i2) {
            int size = MeasureSpec.getSize(i);
            int mode = MeasureSpec.getMode(i);
            int tabMaxWidth = TabLayout.this.getTabMaxWidth();
            if (tabMaxWidth > 0 && (mode == 0 || size > tabMaxWidth)) {
                i = MeasureSpec.makeMeasureSpec(TabLayout.this.tabMaxWidth, Integer.MIN_VALUE);
            }
            super.onMeasure(i, i2);
            if (this.textView != null) {
                float f = TabLayout.this.tabTextSize;
                int i3 = this.defaultMaxLines;
                ImageView imageView = this.iconView;
                boolean z = true;
                if (imageView == null || imageView.getVisibility() != 0) {
                    TextView textView2 = this.textView;
                    if (textView2 != null && textView2.getLineCount() > 1) {
                        f = TabLayout.this.tabTextMultiLineSize;
                    }
                } else {
                    i3 = 1;
                }
                float textSize = this.textView.getTextSize();
                int lineCount = this.textView.getLineCount();
                int maxLines = TextViewCompat.getMaxLines(this.textView);
                int i4 = (f > textSize ? 1 : (f == textSize ? 0 : -1));
                if (i4 != 0 || (maxLines >= 0 && i3 != maxLines)) {
                    if (TabLayout.this.mode == 1 && i4 > 0 && lineCount == 1) {
                        Layout layout = this.textView.getLayout();
                        if (layout == null || approximateLineWidth(layout, 0, f) > ((float) ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()))) {
                            z = false;
                        }
                    }
                    if (z) {
                        this.textView.setTextSize(0, f);
                        this.textView.setMaxLines(i3);
                        super.onMeasure(i, i2);
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void setTab(@Nullable Tab tab2) {
            if (tab2 != this.tab) {
                this.tab = tab2;
                update();
            }
        }

        /* access modifiers changed from: 0000 */
        public void reset() {
            setTab(null);
            setSelected(false);
        }

        /* access modifiers changed from: 0000 */
        public final void update() {
            Tab tab2 = this.tab;
            Drawable drawable = null;
            View customView2 = tab2 != null ? tab2.getCustomView() : null;
            if (customView2 != null) {
                ViewParent parent = customView2.getParent();
                if (parent != this) {
                    if (parent != null) {
                        ((ViewGroup) parent).removeView(customView2);
                    }
                    addView(customView2);
                }
                this.customView = customView2;
                TextView textView2 = this.textView;
                if (textView2 != null) {
                    textView2.setVisibility(8);
                }
                ImageView imageView = this.iconView;
                if (imageView != null) {
                    imageView.setVisibility(8);
                    this.iconView.setImageDrawable(null);
                }
                this.customTextView = (TextView) customView2.findViewById(16908308);
                TextView textView3 = this.customTextView;
                if (textView3 != null) {
                    this.defaultMaxLines = TextViewCompat.getMaxLines(textView3);
                }
                this.customIconView = (ImageView) customView2.findViewById(16908294);
            } else {
                View view = this.customView;
                if (view != null) {
                    removeView(view);
                    this.customView = null;
                }
                this.customTextView = null;
                this.customIconView = null;
            }
            boolean z = false;
            if (this.customView == null) {
                if (this.iconView == null) {
                    ImageView imageView2 = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_icon, this, false);
                    addView(imageView2, 0);
                    this.iconView = imageView2;
                }
                if (!(tab2 == null || tab2.getIcon() == null)) {
                    drawable = DrawableCompat.wrap(tab2.getIcon()).mutate();
                }
                if (drawable != null) {
                    DrawableCompat.setTintList(drawable, TabLayout.this.tabIconTint);
                    if (TabLayout.this.tabIconTintMode != null) {
                        DrawableCompat.setTintMode(drawable, TabLayout.this.tabIconTintMode);
                    }
                }
                if (this.textView == null) {
                    TextView textView4 = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_text, this, false);
                    addView(textView4);
                    this.textView = textView4;
                    this.defaultMaxLines = TextViewCompat.getMaxLines(this.textView);
                }
                TextViewCompat.setTextAppearance(this.textView, TabLayout.this.tabTextAppearance);
                if (TabLayout.this.tabTextColors != null) {
                    this.textView.setTextColor(TabLayout.this.tabTextColors);
                }
                updateTextAndIcon(this.textView, this.iconView);
            } else if (!(this.customTextView == null && this.customIconView == null)) {
                updateTextAndIcon(this.customTextView, this.customIconView);
            }
            if (tab2 != null && !TextUtils.isEmpty(tab2.contentDesc)) {
                setContentDescription(tab2.contentDesc);
            }
            if (tab2 != null && tab2.isSelected()) {
                z = true;
            }
            setSelected(z);
        }

        /* access modifiers changed from: 0000 */
        public final void updateOrientation() {
            setOrientation(TabLayout.this.inlineLabel ^ true ? 1 : 0);
            if (this.customTextView == null && this.customIconView == null) {
                updateTextAndIcon(this.textView, this.iconView);
            } else {
                updateTextAndIcon(this.customTextView, this.customIconView);
            }
        }

        private void updateTextAndIcon(@Nullable TextView textView2, @Nullable ImageView imageView) {
            Tab tab2 = this.tab;
            Drawable mutate = (tab2 == null || tab2.getIcon() == null) ? null : DrawableCompat.wrap(this.tab.getIcon()).mutate();
            Tab tab3 = this.tab;
            CharSequence text = tab3 != null ? tab3.getText() : null;
            if (imageView != null) {
                if (mutate != null) {
                    imageView.setImageDrawable(mutate);
                    imageView.setVisibility(0);
                    setVisibility(0);
                } else {
                    imageView.setVisibility(8);
                    imageView.setImageDrawable(null);
                }
            }
            boolean z = !TextUtils.isEmpty(text);
            if (textView2 != null) {
                if (z) {
                    textView2.setText(text);
                    textView2.setVisibility(0);
                    setVisibility(0);
                } else {
                    textView2.setVisibility(8);
                    textView2.setText(null);
                }
            }
            if (imageView != null) {
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) imageView.getLayoutParams();
                int dpToPx = (!z || imageView.getVisibility() != 0) ? 0 : TabLayout.this.dpToPx(8);
                if (TabLayout.this.inlineLabel) {
                    if (dpToPx != MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams)) {
                        MarginLayoutParamsCompat.setMarginEnd(marginLayoutParams, dpToPx);
                        marginLayoutParams.bottomMargin = 0;
                        imageView.setLayoutParams(marginLayoutParams);
                        imageView.requestLayout();
                    }
                } else if (dpToPx != marginLayoutParams.bottomMargin) {
                    marginLayoutParams.bottomMargin = dpToPx;
                    MarginLayoutParamsCompat.setMarginEnd(marginLayoutParams, 0);
                    imageView.setLayoutParams(marginLayoutParams);
                    imageView.requestLayout();
                }
            }
            Tab tab4 = this.tab;
            CharSequence access$100 = tab4 != null ? tab4.contentDesc : null;
            if (z) {
                access$100 = null;
            }
            TooltipCompat.setTooltipText(this, access$100);
        }

        /* access modifiers changed from: private */
        public int getContentWidth() {
            View[] viewArr = {this.textView, this.iconView, this.customView};
            int length = viewArr.length;
            int i = 0;
            int i2 = 0;
            boolean z = false;
            for (int i3 = 0; i3 < length; i3++) {
                View view = viewArr[i3];
                if (view != null && view.getVisibility() == 0) {
                    i2 = z ? Math.min(i2, view.getLeft()) : view.getLeft();
                    i = z ? Math.max(i, view.getRight()) : view.getRight();
                    z = true;
                }
            }
            return i - i2;
        }

        public Tab getTab() {
            return this.tab;
        }

        private float approximateLineWidth(Layout layout, int i, float f) {
            return layout.getLineWidth(i) * (f / layout.getPaint().getTextSize());
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager viewPager;

        public void onTabReselected(Tab tab) {
        }

        public void onTabUnselected(Tab tab) {
        }

        public ViewPagerOnTabSelectedListener(ViewPager viewPager2) {
            this.viewPager = viewPager2;
        }

        public void onTabSelected(Tab tab) {
            this.viewPager.setCurrentItem(tab.getPosition());
        }
    }

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.tabStyle);
    }

    /* JADX INFO: finally extract failed */
    public TabLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.tabs = new ArrayList<>();
        this.tabViewContentBounds = new RectF();
        this.tabMaxWidth = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.selectedListeners = new ArrayList<>();
        this.tabViewPool = new SimplePool(12);
        setHorizontalScrollBarEnabled(false);
        this.slidingTabIndicator = new SlidingTabIndicator(context);
        super.addView(this.slidingTabIndicator, 0, new FrameLayout.LayoutParams(-2, -1));
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(context, attributeSet, R.styleable.TabLayout, i, R.style.Widget_Design_TabLayout, R.styleable.TabLayout_tabTextAppearance);
        this.slidingTabIndicator.setSelectedIndicatorHeight(obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1));
        this.slidingTabIndicator.setSelectedIndicatorColor(obtainStyledAttributes.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));
        setSelectedTabIndicator(MaterialResources.getDrawable(context, obtainStyledAttributes, R.styleable.TabLayout_tabIndicator));
        setSelectedTabIndicatorGravity(obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIndicatorGravity, 0));
        setTabIndicatorFullWidth(obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
        this.tabPaddingBottom = dimensionPixelSize;
        this.tabPaddingEnd = dimensionPixelSize;
        this.tabPaddingTop = dimensionPixelSize;
        this.tabPaddingStart = dimensionPixelSize;
        this.tabPaddingStart = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, this.tabPaddingStart);
        this.tabPaddingTop = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, this.tabPaddingTop);
        this.tabPaddingEnd = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, this.tabPaddingEnd);
        this.tabPaddingBottom = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, this.tabPaddingBottom);
        this.tabTextAppearance = obtainStyledAttributes.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(this.tabTextAppearance, androidx.appcompat.R.styleable.TextAppearance);
        try {
            this.tabTextSize = (float) obtainStyledAttributes2.getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            this.tabTextColors = MaterialResources.getColorStateList(context, obtainStyledAttributes2, androidx.appcompat.R.styleable.TextAppearance_android_textColor);
            obtainStyledAttributes2.recycle();
            if (obtainStyledAttributes.hasValue(R.styleable.TabLayout_tabTextColor)) {
                this.tabTextColors = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabTextColor);
            }
            if (obtainStyledAttributes.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
                this.tabTextColors = createColorStateList(this.tabTextColors.getDefaultColor(), obtainStyledAttributes.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0));
            }
            this.tabIconTint = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabIconTint);
            this.tabIconTintMode = ViewUtils.parseTintMode(obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIconTintMode, -1), null);
            this.tabRippleColorStateList = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabRippleColor);
            this.tabIndicatorAnimationDuration = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);
            this.requestedTabMinWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, -1);
            this.requestedTabMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, -1);
            this.tabBackgroundResId = obtainStyledAttributes.getResourceId(R.styleable.TabLayout_tabBackground, 0);
            this.contentInsetStart = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
            this.mode = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabMode, 1);
            this.tabGravity = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabGravity, 0);
            this.inlineLabel = obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
            this.unboundedRipple = obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabUnboundedRipple, false);
            obtainStyledAttributes.recycle();
            Resources resources = getResources();
            this.tabTextMultiLineSize = (float) resources.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
            this.scrollableTabMinWidth = resources.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);
            applyModeAndGravity();
        } catch (Throwable th) {
            obtainStyledAttributes2.recycle();
            throw th;
        }
    }

    public void setSelectedTabIndicatorColor(@ColorInt int i) {
        this.slidingTabIndicator.setSelectedIndicatorColor(i);
    }

    @Deprecated
    public void setSelectedTabIndicatorHeight(int i) {
        this.slidingTabIndicator.setSelectedIndicatorHeight(i);
    }

    public void setScrollPosition(int i, float f, boolean z) {
        setScrollPosition(i, f, z, true);
    }

    /* access modifiers changed from: 0000 */
    public void setScrollPosition(int i, float f, boolean z, boolean z2) {
        int round = Math.round(((float) i) + f);
        if (round >= 0 && round < this.slidingTabIndicator.getChildCount()) {
            if (z2) {
                this.slidingTabIndicator.setIndicatorPositionFromTabPosition(i, f);
            }
            ValueAnimator valueAnimator = this.scrollAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.scrollAnimator.cancel();
            }
            scrollTo(calculateScrollXForTab(i, f), 0);
            if (z) {
                setSelectedTabView(round);
            }
        }
    }

    public void addTab(@NonNull Tab tab) {
        addTab(tab, this.tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, int i) {
        addTab(tab, i, this.tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, boolean z) {
        addTab(tab, this.tabs.size(), z);
    }

    public void addTab(@NonNull Tab tab, int i, boolean z) {
        if (tab.parent == this) {
            configureTab(tab, i);
            addTabView(tab);
            if (z) {
                tab.select();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }

    private void addTabFromItemView(@NonNull TabItem tabItem) {
        Tab newTab = newTab();
        if (tabItem.text != null) {
            newTab.setText(tabItem.text);
        }
        if (tabItem.icon != null) {
            newTab.setIcon(tabItem.icon);
        }
        if (tabItem.customLayout != 0) {
            newTab.setCustomView(tabItem.customLayout);
        }
        if (!TextUtils.isEmpty(tabItem.getContentDescription())) {
            newTab.setContentDescription(tabItem.getContentDescription());
        }
        addTab(newTab);
    }

    @Deprecated
    public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener baseOnTabSelectedListener) {
        BaseOnTabSelectedListener baseOnTabSelectedListener2 = this.selectedListener;
        if (baseOnTabSelectedListener2 != null) {
            removeOnTabSelectedListener(baseOnTabSelectedListener2);
        }
        this.selectedListener = baseOnTabSelectedListener;
        if (baseOnTabSelectedListener != null) {
            addOnTabSelectedListener(baseOnTabSelectedListener);
        }
    }

    public void addOnTabSelectedListener(@NonNull BaseOnTabSelectedListener baseOnTabSelectedListener) {
        if (!this.selectedListeners.contains(baseOnTabSelectedListener)) {
            this.selectedListeners.add(baseOnTabSelectedListener);
        }
    }

    public void removeOnTabSelectedListener(@NonNull BaseOnTabSelectedListener baseOnTabSelectedListener) {
        this.selectedListeners.remove(baseOnTabSelectedListener);
    }

    public void clearOnTabSelectedListeners() {
        this.selectedListeners.clear();
    }

    @NonNull
    public Tab newTab() {
        Tab createTabFromPool = createTabFromPool();
        createTabFromPool.parent = this;
        createTabFromPool.view = createTabView(createTabFromPool);
        return createTabFromPool;
    }

    /* access modifiers changed from: protected */
    public Tab createTabFromPool() {
        Tab tab = (Tab) tabPool.acquire();
        return tab == null ? new Tab() : tab;
    }

    /* access modifiers changed from: protected */
    public boolean releaseFromTabPool(Tab tab) {
        return tabPool.release(tab);
    }

    public int getTabCount() {
        return this.tabs.size();
    }

    @Nullable
    public Tab getTabAt(int i) {
        if (i < 0 || i >= getTabCount()) {
            return null;
        }
        return (Tab) this.tabs.get(i);
    }

    public int getSelectedTabPosition() {
        Tab tab = this.selectedTab;
        if (tab != null) {
            return tab.getPosition();
        }
        return -1;
    }

    public void removeTab(Tab tab) {
        if (tab.parent == this) {
            removeTabAt(tab.getPosition());
            return;
        }
        throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    public void removeTabAt(int i) {
        Tab tab = this.selectedTab;
        int position = tab != null ? tab.getPosition() : 0;
        removeTabViewAt(i);
        Tab tab2 = (Tab) this.tabs.remove(i);
        if (tab2 != null) {
            tab2.reset();
            releaseFromTabPool(tab2);
        }
        int size = this.tabs.size();
        for (int i2 = i; i2 < size; i2++) {
            ((Tab) this.tabs.get(i2)).setPosition(i2);
        }
        if (position == i) {
            selectTab(this.tabs.isEmpty() ? null : (Tab) this.tabs.get(Math.max(0, i - 1)));
        }
    }

    public void removeAllTabs() {
        for (int childCount = this.slidingTabIndicator.getChildCount() - 1; childCount >= 0; childCount--) {
            removeTabViewAt(childCount);
        }
        Iterator it = this.tabs.iterator();
        while (it.hasNext()) {
            Tab tab = (Tab) it.next();
            it.remove();
            tab.reset();
            releaseFromTabPool(tab);
        }
        this.selectedTab = null;
    }

    public void setTabMode(int i) {
        if (i != this.mode) {
            this.mode = i;
            applyModeAndGravity();
        }
    }

    public int getTabMode() {
        return this.mode;
    }

    public void setTabGravity(int i) {
        if (this.tabGravity != i) {
            this.tabGravity = i;
            applyModeAndGravity();
        }
    }

    public int getTabGravity() {
        return this.tabGravity;
    }

    public void setSelectedTabIndicatorGravity(int i) {
        if (this.tabIndicatorGravity != i) {
            this.tabIndicatorGravity = i;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }
    }

    public int getTabIndicatorGravity() {
        return this.tabIndicatorGravity;
    }

    public void setTabIndicatorFullWidth(boolean z) {
        this.tabIndicatorFullWidth = z;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
    }

    public boolean isTabIndicatorFullWidth() {
        return this.tabIndicatorFullWidth;
    }

    public void setInlineLabel(boolean z) {
        if (this.inlineLabel != z) {
            this.inlineLabel = z;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateOrientation();
                }
            }
            applyModeAndGravity();
        }
    }

    public void setInlineLabelResource(@BoolRes int i) {
        setInlineLabel(getResources().getBoolean(i));
    }

    public boolean isInlineLabel() {
        return this.inlineLabel;
    }

    public void setUnboundedRipple(boolean z) {
        if (this.unboundedRipple != z) {
            this.unboundedRipple = z;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setUnboundedRippleResource(@BoolRes int i) {
        setUnboundedRipple(getResources().getBoolean(i));
    }

    public boolean hasUnboundedRipple() {
        return this.unboundedRipple;
    }

    public void setTabTextColors(@Nullable ColorStateList colorStateList) {
        if (this.tabTextColors != colorStateList) {
            this.tabTextColors = colorStateList;
            updateAllTabs();
        }
    }

    @Nullable
    public ColorStateList getTabTextColors() {
        return this.tabTextColors;
    }

    public void setTabTextColors(int i, int i2) {
        setTabTextColors(createColorStateList(i, i2));
    }

    public void setTabIconTint(@Nullable ColorStateList colorStateList) {
        if (this.tabIconTint != colorStateList) {
            this.tabIconTint = colorStateList;
            updateAllTabs();
        }
    }

    public void setTabIconTintResource(@ColorRes int i) {
        setTabIconTint(AppCompatResources.getColorStateList(getContext(), i));
    }

    @Nullable
    public ColorStateList getTabIconTint() {
        return this.tabIconTint;
    }

    @Nullable
    public ColorStateList getTabRippleColor() {
        return this.tabRippleColorStateList;
    }

    public void setTabRippleColor(@Nullable ColorStateList colorStateList) {
        if (this.tabRippleColorStateList != colorStateList) {
            this.tabRippleColorStateList = colorStateList;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setTabRippleColorResource(@ColorRes int i) {
        setTabRippleColor(AppCompatResources.getColorStateList(getContext(), i));
    }

    @Nullable
    public Drawable getTabSelectedIndicator() {
        return this.tabSelectedIndicator;
    }

    public void setSelectedTabIndicator(@Nullable Drawable drawable) {
        if (this.tabSelectedIndicator != drawable) {
            this.tabSelectedIndicator = drawable;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }
    }

    public void setSelectedTabIndicator(@DrawableRes int i) {
        if (i != 0) {
            setSelectedTabIndicator(AppCompatResources.getDrawable(getContext(), i));
        } else {
            setSelectedTabIndicator((Drawable) null);
        }
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager2) {
        setupWithViewPager(viewPager2, true);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager2, boolean z) {
        setupWithViewPager(viewPager2, z, false);
    }

    private void setupWithViewPager(@Nullable ViewPager viewPager2, boolean z, boolean z2) {
        ViewPager viewPager3 = this.viewPager;
        if (viewPager3 != null) {
            TabLayoutOnPageChangeListener tabLayoutOnPageChangeListener = this.pageChangeListener;
            if (tabLayoutOnPageChangeListener != null) {
                viewPager3.removeOnPageChangeListener(tabLayoutOnPageChangeListener);
            }
            AdapterChangeListener adapterChangeListener2 = this.adapterChangeListener;
            if (adapterChangeListener2 != null) {
                this.viewPager.removeOnAdapterChangeListener(adapterChangeListener2);
            }
        }
        BaseOnTabSelectedListener baseOnTabSelectedListener = this.currentVpSelectedListener;
        if (baseOnTabSelectedListener != null) {
            removeOnTabSelectedListener(baseOnTabSelectedListener);
            this.currentVpSelectedListener = null;
        }
        if (viewPager2 != null) {
            this.viewPager = viewPager2;
            if (this.pageChangeListener == null) {
                this.pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            this.pageChangeListener.reset();
            viewPager2.addOnPageChangeListener(this.pageChangeListener);
            this.currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager2);
            addOnTabSelectedListener(this.currentVpSelectedListener);
            PagerAdapter adapter = viewPager2.getAdapter();
            if (adapter != null) {
                setPagerAdapter(adapter, z);
            }
            if (this.adapterChangeListener == null) {
                this.adapterChangeListener = new AdapterChangeListener();
            }
            this.adapterChangeListener.setAutoRefresh(z);
            viewPager2.addOnAdapterChangeListener(this.adapterChangeListener);
            setScrollPosition(viewPager2.getCurrentItem(), 0.0f, true);
        } else {
            this.viewPager = null;
            setPagerAdapter(null, false);
        }
        this.setupViewPagerImplicitly = z2;
    }

    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable PagerAdapter pagerAdapter2) {
        setPagerAdapter(pagerAdapter2, false);
    }

    public boolean shouldDelayChildPressedState() {
        return getTabScrollRange() > 0;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.viewPager == null) {
            ViewParent parent = getParent();
            if (parent instanceof ViewPager) {
                setupWithViewPager((ViewPager) parent, true, true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.setupViewPagerImplicitly) {
            setupWithViewPager(null);
            this.setupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(0, ((this.slidingTabIndicator.getWidth() - getWidth()) - getPaddingLeft()) - getPaddingRight());
    }

    /* access modifiers changed from: 0000 */
    public void setPagerAdapter(@Nullable PagerAdapter pagerAdapter2, boolean z) {
        PagerAdapter pagerAdapter3 = this.pagerAdapter;
        if (pagerAdapter3 != null) {
            DataSetObserver dataSetObserver = this.pagerAdapterObserver;
            if (dataSetObserver != null) {
                pagerAdapter3.unregisterDataSetObserver(dataSetObserver);
            }
        }
        this.pagerAdapter = pagerAdapter2;
        if (z && pagerAdapter2 != null) {
            if (this.pagerAdapterObserver == null) {
                this.pagerAdapterObserver = new PagerAdapterObserver();
            }
            pagerAdapter2.registerDataSetObserver(this.pagerAdapterObserver);
        }
        populateFromPagerAdapter();
    }

    /* access modifiers changed from: 0000 */
    public void populateFromPagerAdapter() {
        removeAllTabs();
        PagerAdapter pagerAdapter2 = this.pagerAdapter;
        if (pagerAdapter2 != null) {
            int count = pagerAdapter2.getCount();
            for (int i = 0; i < count; i++) {
                addTab(newTab().setText(this.pagerAdapter.getPageTitle(i)), false);
            }
            ViewPager viewPager2 = this.viewPager;
            if (viewPager2 != null && count > 0) {
                int currentItem = viewPager2.getCurrentItem();
                if (currentItem != getSelectedTabPosition() && currentItem < getTabCount()) {
                    selectTab(getTabAt(currentItem));
                }
            }
        }
    }

    private void updateAllTabs() {
        int size = this.tabs.size();
        for (int i = 0; i < size; i++) {
            ((Tab) this.tabs.get(i)).updateView();
        }
    }

    private TabView createTabView(@NonNull Tab tab) {
        Pool<TabView> pool = this.tabViewPool;
        TabView tabView = pool != null ? (TabView) pool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        if (TextUtils.isEmpty(tab.contentDesc)) {
            tabView.setContentDescription(tab.text);
        } else {
            tabView.setContentDescription(tab.contentDesc);
        }
        return tabView;
    }

    private void configureTab(Tab tab, int i) {
        tab.setPosition(i);
        this.tabs.add(i, tab);
        int size = this.tabs.size();
        while (true) {
            i++;
            if (i < size) {
                ((Tab) this.tabs.get(i)).setPosition(i);
            } else {
                return;
            }
        }
    }

    private void addTabView(Tab tab) {
        this.slidingTabIndicator.addView(tab.view, tab.getPosition(), createLayoutParamsForTabs());
    }

    public void addView(View view) {
        addViewInternal(view);
    }

    public void addView(View view, int i) {
        addViewInternal(view);
    }

    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    private void addViewInternal(View view) {
        if (view instanceof TabItem) {
            addTabFromItemView((TabItem) view);
            return;
        }
        throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
    }

    private LayoutParams createLayoutParamsForTabs() {
        LayoutParams layoutParams = new LayoutParams(-2, -1);
        updateTabViewLayoutParams(layoutParams);
        return layoutParams;
    }

    private void updateTabViewLayoutParams(LayoutParams layoutParams) {
        if (this.mode == 1 && this.tabGravity == 0) {
            layoutParams.width = 0;
            layoutParams.weight = 1.0f;
            return;
        }
        layoutParams.width = -2;
        layoutParams.weight = 0.0f;
    }

    /* access modifiers changed from: 0000 */
    @Dimension(unit = 1)
    public int dpToPx(@Dimension(unit = 0) int i) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) i));
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
            View childAt = this.slidingTabIndicator.getChildAt(i);
            if (childAt instanceof TabView) {
                ((TabView) childAt).drawBackground(canvas);
            }
        }
        super.onDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int dpToPx = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
        int mode2 = MeasureSpec.getMode(i2);
        if (mode2 == Integer.MIN_VALUE) {
            i2 = MeasureSpec.makeMeasureSpec(Math.min(dpToPx, MeasureSpec.getSize(i2)), 1073741824);
        } else if (mode2 == 0) {
            i2 = MeasureSpec.makeMeasureSpec(dpToPx, 1073741824);
        }
        int size = MeasureSpec.getSize(i);
        if (MeasureSpec.getMode(i) != 0) {
            int i3 = this.requestedTabMaxWidth;
            if (i3 <= 0) {
                i3 = size - dpToPx(56);
            }
            this.tabMaxWidth = i3;
        }
        super.onMeasure(i, i2);
        if (getChildCount() == 1) {
            boolean z = false;
            View childAt = getChildAt(0);
            int i4 = this.mode;
            if (i4 == 0 ? childAt.getMeasuredWidth() < getMeasuredWidth() : !(i4 != 1 || childAt.getMeasuredWidth() == getMeasuredWidth())) {
                z = true;
            }
            if (z) {
                childAt.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), getChildMeasureSpec(i2, getPaddingTop() + getPaddingBottom(), childAt.getLayoutParams().height));
            }
        }
    }

    private void removeTabViewAt(int i) {
        TabView tabView = (TabView) this.slidingTabIndicator.getChildAt(i);
        this.slidingTabIndicator.removeViewAt(i);
        if (tabView != null) {
            tabView.reset();
            this.tabViewPool.release(tabView);
        }
        requestLayout();
    }

    private void animateToTab(int i) {
        if (i != -1) {
            if (getWindowToken() == null || !ViewCompat.isLaidOut(this) || this.slidingTabIndicator.childrenNeedLayout()) {
                setScrollPosition(i, 0.0f, true);
                return;
            }
            int scrollX = getScrollX();
            int calculateScrollXForTab = calculateScrollXForTab(i, 0.0f);
            if (scrollX != calculateScrollXForTab) {
                ensureScrollAnimator();
                this.scrollAnimator.setIntValues(new int[]{scrollX, calculateScrollXForTab});
                this.scrollAnimator.start();
            }
            this.slidingTabIndicator.animateIndicatorToPosition(i, this.tabIndicatorAnimationDuration);
        }
    }

    private void ensureScrollAnimator() {
        if (this.scrollAnimator == null) {
            this.scrollAnimator = new ValueAnimator();
            this.scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.scrollAnimator.setDuration((long) this.tabIndicatorAnimationDuration);
            this.scrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    TabLayout.this.scrollTo(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0);
                }
            });
        }
    }

    /* access modifiers changed from: 0000 */
    public void setScrollAnimatorListener(AnimatorListener animatorListener) {
        ensureScrollAnimator();
        this.scrollAnimator.addListener(animatorListener);
    }

    private void setSelectedTabView(int i) {
        int childCount = this.slidingTabIndicator.getChildCount();
        if (i < childCount) {
            int i2 = 0;
            while (i2 < childCount) {
                View childAt = this.slidingTabIndicator.getChildAt(i2);
                boolean z = true;
                childAt.setSelected(i2 == i);
                if (i2 != i) {
                    z = false;
                }
                childAt.setActivated(z);
                i2++;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    /* access modifiers changed from: 0000 */
    public void selectTab(Tab tab, boolean z) {
        Tab tab2 = this.selectedTab;
        if (tab2 != tab) {
            int position = tab != null ? tab.getPosition() : -1;
            if (z) {
                if ((tab2 == null || tab2.getPosition() == -1) && position != -1) {
                    setScrollPosition(position, 0.0f, true);
                } else {
                    animateToTab(position);
                }
                if (position != -1) {
                    setSelectedTabView(position);
                }
            }
            this.selectedTab = tab;
            if (tab2 != null) {
                dispatchTabUnselected(tab2);
            }
            if (tab != null) {
                dispatchTabSelected(tab);
            }
        } else if (tab2 != null) {
            dispatchTabReselected(tab);
            animateToTab(tab.getPosition());
        }
    }

    private void dispatchTabSelected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabReselected(tab);
        }
    }

    private int calculateScrollXForTab(int i, float f) {
        int i2 = 0;
        if (this.mode != 0) {
            return 0;
        }
        View childAt = this.slidingTabIndicator.getChildAt(i);
        int i3 = i + 1;
        View childAt2 = i3 < this.slidingTabIndicator.getChildCount() ? this.slidingTabIndicator.getChildAt(i3) : null;
        int width = childAt != null ? childAt.getWidth() : 0;
        if (childAt2 != null) {
            i2 = childAt2.getWidth();
        }
        int left = (childAt.getLeft() + (width / 2)) - (getWidth() / 2);
        int i4 = (int) (((float) (width + i2)) * 0.5f * f);
        return ViewCompat.getLayoutDirection(this) == 0 ? left + i4 : left - i4;
    }

    private void applyModeAndGravity() {
        ViewCompat.setPaddingRelative(this.slidingTabIndicator, this.mode == 0 ? Math.max(0, this.contentInsetStart - this.tabPaddingStart) : 0, 0, 0, 0);
        int i = this.mode;
        if (i == 0) {
            this.slidingTabIndicator.setGravity(GravityCompat.START);
        } else if (i == 1) {
            this.slidingTabIndicator.setGravity(1);
        }
        updateTabViews(true);
    }

    /* access modifiers changed from: 0000 */
    public void updateTabViews(boolean z) {
        for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
            View childAt = this.slidingTabIndicator.getChildAt(i);
            childAt.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LayoutParams) childAt.getLayoutParams());
            if (z) {
                childAt.requestLayout();
            }
        }
    }

    private static ColorStateList createColorStateList(int i, int i2) {
        return new ColorStateList(new int[][]{SELECTED_STATE_SET, EMPTY_STATE_SET}, new int[]{i2, i});
    }

    @Dimension(unit = 0)
    private int getDefaultHeight() {
        int size = this.tabs.size();
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            Tab tab = (Tab) this.tabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                z = true;
                break;
            }
            i++;
        }
        return (!z || this.inlineLabel) ? 48 : 72;
    }

    private int getTabMinWidth() {
        int i = this.requestedTabMinWidth;
        if (i != -1) {
            return i;
        }
        return this.mode == 0 ? this.scrollableTabMinWidth : 0;
    }

    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return generateDefaultLayoutParams();
    }

    /* access modifiers changed from: 0000 */
    public int getTabMaxWidth() {
        return this.tabMaxWidth;
    }
}
