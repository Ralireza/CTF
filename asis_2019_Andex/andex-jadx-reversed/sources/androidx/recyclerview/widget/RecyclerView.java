package androidx.recyclerview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.widget.EdgeEffect;
import android.widget.OverScroller;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.TraceCompat;
import androidx.core.util.Preconditions;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.customview.view.AbsSavedState;
import androidx.recyclerview.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild2 {
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC = (VERSION.SDK_INT >= 23);
    static final boolean ALLOW_THREAD_GAP_WORK = (VERSION.SDK_INT >= 21);
    private static final int[] CLIP_TO_PADDING_ATTR = {16842987};
    static final boolean DEBUG = false;
    static final int DEFAULT_ORIENTATION = 1;
    static final boolean DISPATCH_TEMP_DETACH = false;
    private static final boolean FORCE_ABS_FOCUS_SEARCH_DIRECTION = (VERSION.SDK_INT <= 15);
    static final boolean FORCE_INVALIDATE_DISPLAY_LIST = (VERSION.SDK_INT == 18 || VERSION.SDK_INT == 19 || VERSION.SDK_INT == 20);
    static final long FOREVER_NS = Long.MAX_VALUE;
    public static final int HORIZONTAL = 0;
    private static final boolean IGNORE_DETACHED_FOCUSED_CHILD = (VERSION.SDK_INT <= 15);
    private static final int INVALID_POINTER = -1;
    public static final int INVALID_TYPE = -1;
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = {Context.class, AttributeSet.class, Integer.TYPE, Integer.TYPE};
    static final int MAX_SCROLL_DURATION = 2000;
    private static final int[] NESTED_SCROLLING_ATTRS = {16843830};
    public static final long NO_ID = -1;
    public static final int NO_POSITION = -1;
    static final boolean POST_UPDATES_ON_ANIMATION = (VERSION.SDK_INT >= 16);
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    static final String TAG = "RecyclerView";
    public static final int TOUCH_SLOP_DEFAULT = 0;
    public static final int TOUCH_SLOP_PAGING = 1;
    static final String TRACE_BIND_VIEW_TAG = "RV OnBindView";
    static final String TRACE_CREATE_VIEW_TAG = "RV CreateView";
    private static final String TRACE_HANDLE_ADAPTER_UPDATES_TAG = "RV PartialInvalidate";
    static final String TRACE_NESTED_PREFETCH_TAG = "RV Nested Prefetch";
    private static final String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG = "RV FullInvalidate";
    private static final String TRACE_ON_LAYOUT_TAG = "RV OnLayout";
    static final String TRACE_PREFETCH_TAG = "RV Prefetch";
    static final String TRACE_SCROLL_TAG = "RV Scroll";
    static final boolean VERBOSE_TRACING = false;
    public static final int VERTICAL = 1;
    static final Interpolator sQuinticInterpolator = new Interpolator() {
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2 * f2 * f2) + 1.0f;
        }
    };
    RecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private OnItemTouchListener mActiveOnItemTouchListener;
    Adapter mAdapter;
    AdapterHelper mAdapterHelper;
    boolean mAdapterUpdateDuringMeasure;
    private EdgeEffect mBottomGlow;
    private ChildDrawingOrderCallback mChildDrawingOrderCallback;
    ChildHelper mChildHelper;
    boolean mClipToPadding;
    boolean mDataSetHasChangedAfterLayout;
    boolean mDispatchItemsChangedEvent;
    private int mDispatchScrollCounter;
    private int mEatenAccessibilityChangeFlags;
    @NonNull
    private EdgeEffectFactory mEdgeEffectFactory;
    boolean mEnableFastScroller;
    @VisibleForTesting
    boolean mFirstLayoutComplete;
    GapWorker mGapWorker;
    boolean mHasFixedSize;
    private boolean mIgnoreMotionEventTillDown;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mInterceptRequestLayoutDepth;
    boolean mIsAttached;
    ItemAnimator mItemAnimator;
    private ItemAnimatorListener mItemAnimatorListener;
    private Runnable mItemAnimatorRunner;
    final ArrayList<ItemDecoration> mItemDecorations;
    boolean mItemsAddedOrRemoved;
    boolean mItemsChanged;
    private int mLastTouchX;
    private int mLastTouchY;
    @VisibleForTesting
    LayoutManager mLayout;
    boolean mLayoutFrozen;
    private int mLayoutOrScrollCounter;
    boolean mLayoutWasDefered;
    private EdgeEffect mLeftGlow;
    private final int mMaxFlingVelocity;
    private final int mMinFlingVelocity;
    private final int[] mMinMaxLayoutPositions;
    private final int[] mNestedOffsets;
    private final RecyclerViewDataObserver mObserver;
    private List<OnChildAttachStateChangeListener> mOnChildAttachStateListeners;
    private OnFlingListener mOnFlingListener;
    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners;
    @VisibleForTesting
    final List<ViewHolder> mPendingAccessibilityImportanceChange;
    private SavedState mPendingSavedState;
    boolean mPostedAnimatorRunner;
    LayoutPrefetchRegistryImpl mPrefetchRegistry;
    private boolean mPreserveFocusAfterLayout;
    final Recycler mRecycler;
    RecyclerListener mRecyclerListener;
    private EdgeEffect mRightGlow;
    private float mScaledHorizontalScrollFactor;
    private float mScaledVerticalScrollFactor;
    final int[] mScrollConsumed;
    private OnScrollListener mScrollListener;
    private List<OnScrollListener> mScrollListeners;
    private final int[] mScrollOffset;
    private int mScrollPointerId;
    private int mScrollState;
    final int[] mScrollStepConsumed;
    private NestedScrollingChildHelper mScrollingChildHelper;
    final State mState;
    final Rect mTempRect;
    private final Rect mTempRect2;
    final RectF mTempRectF;
    private EdgeEffect mTopGlow;
    private int mTouchSlop;
    final Runnable mUpdateChildViewsRunnable;
    private VelocityTracker mVelocityTracker;
    final ViewFlinger mViewFlinger;
    private final ProcessCallback mViewInfoProcessCallback;
    final ViewInfoStore mViewInfoStore;

    public static abstract class Adapter<VH extends ViewHolder> {
        private boolean mHasStableIds = false;
        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        public abstract int getItemCount();

        public long getItemId(int i) {
            return -1;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        }

        public abstract void onBindViewHolder(@NonNull VH vh, int i);

        @NonNull
        public abstract VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i);

        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        }

        public boolean onFailedToRecycleView(@NonNull VH vh) {
            return false;
        }

        public void onViewAttachedToWindow(@NonNull VH vh) {
        }

        public void onViewDetachedFromWindow(@NonNull VH vh) {
        }

        public void onViewRecycled(@NonNull VH vh) {
        }

        public void onBindViewHolder(@NonNull VH vh, int i, @NonNull List<Object> list) {
            onBindViewHolder(vh, i);
        }

        @NonNull
        public final VH createViewHolder(@NonNull ViewGroup viewGroup, int i) {
            try {
                TraceCompat.beginSection(RecyclerView.TRACE_CREATE_VIEW_TAG);
                VH onCreateViewHolder = onCreateViewHolder(viewGroup, i);
                if (onCreateViewHolder.itemView.getParent() == null) {
                    onCreateViewHolder.mItemViewType = i;
                    return onCreateViewHolder;
                }
                throw new IllegalStateException("ViewHolder views must not be attached when created. Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
            } finally {
                TraceCompat.endSection();
            }
        }

        public final void bindViewHolder(@NonNull VH vh, int i) {
            vh.mPosition = i;
            if (hasStableIds()) {
                vh.mItemId = getItemId(i);
            }
            vh.setFlags(1, 519);
            TraceCompat.beginSection(RecyclerView.TRACE_BIND_VIEW_TAG);
            onBindViewHolder(vh, i, vh.getUnmodifiedPayloads());
            vh.clearPayload();
            android.view.ViewGroup.LayoutParams layoutParams = vh.itemView.getLayoutParams();
            if (layoutParams instanceof LayoutParams) {
                ((LayoutParams) layoutParams).mInsetsDirty = true;
            }
            TraceCompat.endSection();
        }

        public void setHasStableIds(boolean z) {
            if (!hasObservers()) {
                this.mHasStableIds = z;
                return;
            }
            throw new IllegalStateException("Cannot change whether this adapter has stable IDs while the adapter has registered observers.");
        }

        public final boolean hasStableIds() {
            return this.mHasStableIds;
        }

        public final boolean hasObservers() {
            return this.mObservable.hasObservers();
        }

        public void registerAdapterDataObserver(@NonNull AdapterDataObserver adapterDataObserver) {
            this.mObservable.registerObserver(adapterDataObserver);
        }

        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver adapterDataObserver) {
            this.mObservable.unregisterObserver(adapterDataObserver);
        }

        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }

        public final void notifyItemChanged(int i) {
            this.mObservable.notifyItemRangeChanged(i, 1);
        }

        public final void notifyItemChanged(int i, @Nullable Object obj) {
            this.mObservable.notifyItemRangeChanged(i, 1, obj);
        }

        public final void notifyItemRangeChanged(int i, int i2) {
            this.mObservable.notifyItemRangeChanged(i, i2);
        }

        public final void notifyItemRangeChanged(int i, int i2, @Nullable Object obj) {
            this.mObservable.notifyItemRangeChanged(i, i2, obj);
        }

        public final void notifyItemInserted(int i) {
            this.mObservable.notifyItemRangeInserted(i, 1);
        }

        public final void notifyItemMoved(int i, int i2) {
            this.mObservable.notifyItemMoved(i, i2);
        }

        public final void notifyItemRangeInserted(int i, int i2) {
            this.mObservable.notifyItemRangeInserted(i, i2);
        }

        public final void notifyItemRemoved(int i) {
            this.mObservable.notifyItemRangeRemoved(i, 1);
        }

        public final void notifyItemRangeRemoved(int i, int i2) {
            this.mObservable.notifyItemRangeRemoved(i, i2);
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public boolean hasObservers() {
            return !this.mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onChanged();
            }
        }

        public void notifyItemRangeChanged(int i, int i2) {
            notifyItemRangeChanged(i, i2, null);
        }

        public void notifyItemRangeChanged(int i, int i2, @Nullable Object obj) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeChanged(i, i2, obj);
            }
        }

        public void notifyItemRangeInserted(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeInserted(i, i2);
            }
        }

        public void notifyItemRangeRemoved(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeRemoved(i, i2);
            }
        }

        public void notifyItemMoved(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeMoved(i, i2, 1);
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }

        public void onItemRangeChanged(int i, int i2) {
        }

        public void onItemRangeInserted(int i, int i2) {
        }

        public void onItemRangeMoved(int i, int i2, int i3) {
        }

        public void onItemRangeRemoved(int i, int i2) {
        }

        public void onItemRangeChanged(int i, int i2, @Nullable Object obj) {
            onItemRangeChanged(i, i2);
        }
    }

    public interface ChildDrawingOrderCallback {
        int onGetChildDrawingOrder(int i, int i2);
    }

    public static class EdgeEffectFactory {
        public static final int DIRECTION_BOTTOM = 3;
        public static final int DIRECTION_LEFT = 0;
        public static final int DIRECTION_RIGHT = 2;
        public static final int DIRECTION_TOP = 1;

        @Retention(RetentionPolicy.SOURCE)
        public @interface EdgeDirection {
        }

        /* access modifiers changed from: protected */
        @NonNull
        public EdgeEffect createEdgeEffect(@NonNull RecyclerView recyclerView, int i) {
            return new EdgeEffect(recyclerView.getContext());
        }
    }

    public static abstract class ItemAnimator {
        public static final int FLAG_APPEARED_IN_PRE_LAYOUT = 4096;
        public static final int FLAG_CHANGED = 2;
        public static final int FLAG_INVALIDATED = 4;
        public static final int FLAG_MOVED = 2048;
        public static final int FLAG_REMOVED = 8;
        private long mAddDuration = 120;
        private long mChangeDuration = 250;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList<>();
        private ItemAnimatorListener mListener = null;
        private long mMoveDuration = 250;
        private long mRemoveDuration = 120;

        @Retention(RetentionPolicy.SOURCE)
        public @interface AdapterChanges {
        }

        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        interface ItemAnimatorListener {
            void onAnimationFinished(@NonNull ViewHolder viewHolder);
        }

        public static class ItemHolderInfo {
            public int bottom;
            public int changeFlags;
            public int left;
            public int right;
            public int top;

            @NonNull
            public ItemHolderInfo setFrom(@NonNull ViewHolder viewHolder) {
                return setFrom(viewHolder, 0);
            }

            @NonNull
            public ItemHolderInfo setFrom(@NonNull ViewHolder viewHolder, int i) {
                View view = viewHolder.itemView;
                this.left = view.getLeft();
                this.top = view.getTop();
                this.right = view.getRight();
                this.bottom = view.getBottom();
                return this;
            }
        }

        public abstract boolean animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public abstract boolean animateChange(@NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public abstract boolean animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @Nullable ItemHolderInfo itemHolderInfo2);

        public abstract boolean animatePersistence(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
            return true;
        }

        public abstract void endAnimation(@NonNull ViewHolder viewHolder);

        public abstract void endAnimations();

        public abstract boolean isRunning();

        public void onAnimationFinished(@NonNull ViewHolder viewHolder) {
        }

        public void onAnimationStarted(@NonNull ViewHolder viewHolder) {
        }

        public abstract void runPendingAnimations();

        public long getMoveDuration() {
            return this.mMoveDuration;
        }

        public void setMoveDuration(long j) {
            this.mMoveDuration = j;
        }

        public long getAddDuration() {
            return this.mAddDuration;
        }

        public void setAddDuration(long j) {
            this.mAddDuration = j;
        }

        public long getRemoveDuration() {
            return this.mRemoveDuration;
        }

        public void setRemoveDuration(long j) {
            this.mRemoveDuration = j;
        }

        public long getChangeDuration() {
            return this.mChangeDuration;
        }

        public void setChangeDuration(long j) {
            this.mChangeDuration = j;
        }

        /* access modifiers changed from: 0000 */
        public void setListener(ItemAnimatorListener itemAnimatorListener) {
            this.mListener = itemAnimatorListener;
        }

        @NonNull
        public ItemHolderInfo recordPreLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder, int i, @NonNull List<Object> list) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        @NonNull
        public ItemHolderInfo recordPostLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        static int buildAdapterChangeFlagsForAnimations(ViewHolder viewHolder) {
            int i = viewHolder.mFlags & 14;
            if (viewHolder.isInvalid()) {
                return 4;
            }
            if ((i & 4) == 0) {
                int oldPosition = viewHolder.getOldPosition();
                int adapterPosition = viewHolder.getAdapterPosition();
                if (!(oldPosition == -1 || adapterPosition == -1 || oldPosition == adapterPosition)) {
                    i |= 2048;
                }
            }
            return i;
        }

        public final void dispatchAnimationFinished(@NonNull ViewHolder viewHolder) {
            onAnimationFinished(viewHolder);
            ItemAnimatorListener itemAnimatorListener = this.mListener;
            if (itemAnimatorListener != null) {
                itemAnimatorListener.onAnimationFinished(viewHolder);
            }
        }

        public final void dispatchAnimationStarted(@NonNull ViewHolder viewHolder) {
            onAnimationStarted(viewHolder);
        }

        public final boolean isRunning(@Nullable ItemAnimatorFinishedListener itemAnimatorFinishedListener) {
            boolean isRunning = isRunning();
            if (itemAnimatorFinishedListener != null) {
                if (!isRunning) {
                    itemAnimatorFinishedListener.onAnimationsFinished();
                } else {
                    this.mFinishedListeners.add(itemAnimatorFinishedListener);
                }
            }
            return isRunning;
        }

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder, @NonNull List<Object> list) {
            return canReuseUpdatedViewHolder(viewHolder);
        }

        public final void dispatchAnimationsFinished() {
            int size = this.mFinishedListeners.size();
            for (int i = 0; i < size; i++) {
                ((ItemAnimatorFinishedListener) this.mFinishedListeners.get(i)).onAnimationsFinished();
            }
            this.mFinishedListeners.clear();
        }

        @NonNull
        public ItemHolderInfo obtainHolderInfo() {
            return new ItemHolderInfo();
        }
    }

    private class ItemAnimatorRestoreListener implements ItemAnimatorListener {
        ItemAnimatorRestoreListener() {
        }

        public void onAnimationFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (viewHolder.mShadowedHolder != null && viewHolder.mShadowingHolder == null) {
                viewHolder.mShadowedHolder = null;
            }
            viewHolder.mShadowingHolder = null;
            if (!viewHolder.shouldBeKeptAsChild() && !RecyclerView.this.removeAnimatingView(viewHolder.itemView) && viewHolder.isTmpDetached()) {
                RecyclerView.this.removeDetachedView(viewHolder.itemView, false);
            }
        }
    }

    public static abstract class ItemDecoration {
        @Deprecated
        public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView) {
        }

        @Deprecated
        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView) {
        }

        public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull State state) {
            onDraw(canvas, recyclerView);
        }

        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull State state) {
            onDrawOver(canvas, recyclerView);
        }

        @Deprecated
        public void getItemOffsets(@NonNull Rect rect, int i, @NonNull RecyclerView recyclerView) {
            rect.set(0, 0, 0, 0);
        }

        public void getItemOffsets(@NonNull Rect rect, @NonNull View view, @NonNull RecyclerView recyclerView, @NonNull State state) {
            getItemOffsets(rect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(), recyclerView);
        }
    }

    public static abstract class LayoutManager {
        boolean mAutoMeasure = false;
        ChildHelper mChildHelper;
        private int mHeight;
        private int mHeightMode;
        ViewBoundsCheck mHorizontalBoundCheck = new ViewBoundsCheck(this.mHorizontalBoundCheckCallback);
        private final Callback mHorizontalBoundCheckCallback = new Callback() {
            public int getChildCount() {
                return LayoutManager.this.getChildCount();
            }

            public View getParent() {
                return LayoutManager.this.mRecyclerView;
            }

            public View getChildAt(int i) {
                return LayoutManager.this.getChildAt(i);
            }

            public int getParentStart() {
                return LayoutManager.this.getPaddingLeft();
            }

            public int getParentEnd() {
                return LayoutManager.this.getWidth() - LayoutManager.this.getPaddingRight();
            }

            public int getChildStart(View view) {
                return LayoutManager.this.getDecoratedLeft(view) - ((LayoutParams) view.getLayoutParams()).leftMargin;
            }

            public int getChildEnd(View view) {
                return LayoutManager.this.getDecoratedRight(view) + ((LayoutParams) view.getLayoutParams()).rightMargin;
            }
        };
        boolean mIsAttachedToWindow = false;
        private boolean mItemPrefetchEnabled = true;
        private boolean mMeasurementCacheEnabled = true;
        int mPrefetchMaxCountObserved;
        boolean mPrefetchMaxObservedInInitialPrefetch;
        RecyclerView mRecyclerView;
        boolean mRequestedSimpleAnimations = false;
        @Nullable
        SmoothScroller mSmoothScroller;
        ViewBoundsCheck mVerticalBoundCheck = new ViewBoundsCheck(this.mVerticalBoundCheckCallback);
        private final Callback mVerticalBoundCheckCallback = new Callback() {
            public int getChildCount() {
                return LayoutManager.this.getChildCount();
            }

            public View getParent() {
                return LayoutManager.this.mRecyclerView;
            }

            public View getChildAt(int i) {
                return LayoutManager.this.getChildAt(i);
            }

            public int getParentStart() {
                return LayoutManager.this.getPaddingTop();
            }

            public int getParentEnd() {
                return LayoutManager.this.getHeight() - LayoutManager.this.getPaddingBottom();
            }

            public int getChildStart(View view) {
                return LayoutManager.this.getDecoratedTop(view) - ((LayoutParams) view.getLayoutParams()).topMargin;
            }

            public int getChildEnd(View view) {
                return LayoutManager.this.getDecoratedBottom(view) + ((LayoutParams) view.getLayoutParams()).bottomMargin;
            }
        };
        private int mWidth;
        private int mWidthMode;

        public interface LayoutPrefetchRegistry {
            void addPosition(int i, int i2);
        }

        public static class Properties {
            public int orientation;
            public boolean reverseLayout;
            public int spanCount;
            public boolean stackFromEnd;
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public boolean checkLayoutParams(LayoutParams layoutParams) {
            return layoutParams != null;
        }

        public void collectAdjacentPrefetchPositions(int i, int i2, State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        }

        public void collectInitialPrefetchPositions(int i, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        }

        public int computeHorizontalScrollExtent(@NonNull State state) {
            return 0;
        }

        public int computeHorizontalScrollOffset(@NonNull State state) {
            return 0;
        }

        public int computeHorizontalScrollRange(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollExtent(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollOffset(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollRange(@NonNull State state) {
            return 0;
        }

        public abstract LayoutParams generateDefaultLayoutParams();

        public int getBaseline() {
            return -1;
        }

        public int getSelectionModeForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            return 0;
        }

        public boolean isLayoutHierarchical(@NonNull Recycler recycler, @NonNull State state) {
            return false;
        }

        public void onAdapterChanged(@Nullable Adapter adapter, @Nullable Adapter adapter2) {
        }

        public boolean onAddFocusables(@NonNull RecyclerView recyclerView, @NonNull ArrayList<View> arrayList, int i, int i2) {
            return false;
        }

        @CallSuper
        public void onAttachedToWindow(RecyclerView recyclerView) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView recyclerView) {
        }

        @Nullable
        public View onFocusSearchFailed(@NonNull View view, int i, @NonNull Recycler recycler, @NonNull State state) {
            return null;
        }

        @Nullable
        public View onInterceptFocusSearch(@NonNull View view, int i) {
            return null;
        }

        public void onItemsAdded(@NonNull RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        }

        public void onItemsMoved(@NonNull RecyclerView recyclerView, int i, int i2, int i3) {
        }

        public void onItemsRemoved(@NonNull RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int i, int i2) {
        }

        public void onLayoutCompleted(State state) {
        }

        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        @Nullable
        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onScrollStateChanged(int i) {
        }

        public boolean performAccessibilityActionForItem(@NonNull Recycler recycler, @NonNull State state, @NonNull View view, int i, @Nullable Bundle bundle) {
            return false;
        }

        public int scrollHorizontallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        public void scrollToPosition(int i) {
        }

        public int scrollVerticallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean shouldMeasureTwice() {
            return false;
        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        /* access modifiers changed from: 0000 */
        public void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                this.mRecyclerView = null;
                this.mChildHelper = null;
                this.mWidth = 0;
                this.mHeight = 0;
            } else {
                this.mRecyclerView = recyclerView;
                this.mChildHelper = recyclerView.mChildHelper;
                this.mWidth = recyclerView.getWidth();
                this.mHeight = recyclerView.getHeight();
            }
            this.mWidthMode = 1073741824;
            this.mHeightMode = 1073741824;
        }

        /* access modifiers changed from: 0000 */
        public void setMeasureSpecs(int i, int i2) {
            this.mWidth = MeasureSpec.getSize(i);
            this.mWidthMode = MeasureSpec.getMode(i);
            if (this.mWidthMode == 0 && !RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                this.mWidth = 0;
            }
            this.mHeight = MeasureSpec.getSize(i2);
            this.mHeightMode = MeasureSpec.getMode(i2);
            if (this.mHeightMode == 0 && !RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                this.mHeight = 0;
            }
        }

        /* access modifiers changed from: 0000 */
        public void setMeasuredDimensionFromChildren(int i, int i2) {
            int childCount = getChildCount();
            if (childCount == 0) {
                this.mRecyclerView.defaultOnMeasure(i, i2);
                return;
            }
            int i3 = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            int i4 = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            int i5 = Integer.MIN_VALUE;
            int i6 = Integer.MIN_VALUE;
            for (int i7 = 0; i7 < childCount; i7++) {
                View childAt = getChildAt(i7);
                Rect rect = this.mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(childAt, rect);
                if (rect.left < i3) {
                    i3 = rect.left;
                }
                if (rect.right > i5) {
                    i5 = rect.right;
                }
                if (rect.top < i4) {
                    i4 = rect.top;
                }
                if (rect.bottom > i6) {
                    i6 = rect.bottom;
                }
            }
            this.mRecyclerView.mTempRect.set(i3, i4, i5, i6);
            setMeasuredDimension(this.mRecyclerView.mTempRect, i, i2);
        }

        public void setMeasuredDimension(Rect rect, int i, int i2) {
            setMeasuredDimension(chooseSize(i, rect.width() + getPaddingLeft() + getPaddingRight(), getMinimumWidth()), chooseSize(i2, rect.height() + getPaddingTop() + getPaddingBottom(), getMinimumHeight()));
        }

        public void requestLayout() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.requestLayout();
            }
        }

        public void assertInLayoutOrScroll(String str) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.assertInLayoutOrScroll(str);
            }
        }

        public static int chooseSize(int i, int i2, int i3) {
            int mode = MeasureSpec.getMode(i);
            int size = MeasureSpec.getSize(i);
            if (mode == Integer.MIN_VALUE) {
                return Math.min(size, Math.max(i2, i3));
            }
            if (mode != 1073741824) {
                size = Math.max(i2, i3);
            }
            return size;
        }

        public void assertNotInLayoutOrScroll(String str) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.assertNotInLayoutOrScroll(str);
            }
        }

        @Deprecated
        public void setAutoMeasureEnabled(boolean z) {
            this.mAutoMeasure = z;
        }

        public boolean isAutoMeasureEnabled() {
            return this.mAutoMeasure;
        }

        public final void setItemPrefetchEnabled(boolean z) {
            if (z != this.mItemPrefetchEnabled) {
                this.mItemPrefetchEnabled = z;
                this.mPrefetchMaxCountObserved = 0;
                RecyclerView recyclerView = this.mRecyclerView;
                if (recyclerView != null) {
                    recyclerView.mRecycler.updateViewCacheSize();
                }
            }
        }

        public final boolean isItemPrefetchEnabled() {
            return this.mItemPrefetchEnabled;
        }

        /* access modifiers changed from: 0000 */
        public void dispatchAttachedToWindow(RecyclerView recyclerView) {
            this.mIsAttachedToWindow = true;
            onAttachedToWindow(recyclerView);
        }

        /* access modifiers changed from: 0000 */
        public void dispatchDetachedFromWindow(RecyclerView recyclerView, Recycler recycler) {
            this.mIsAttachedToWindow = false;
            onDetachedFromWindow(recyclerView, recycler);
        }

        public boolean isAttachedToWindow() {
            return this.mIsAttachedToWindow;
        }

        public void postOnAnimation(Runnable runnable) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                ViewCompat.postOnAnimation(recyclerView, runnable);
            }
        }

        public boolean removeCallbacks(Runnable runnable) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.removeCallbacks(runnable);
            }
            return false;
        }

        @CallSuper
        public void onDetachedFromWindow(RecyclerView recyclerView, Recycler recycler) {
            onDetachedFromWindow(recyclerView);
        }

        public boolean getClipToPadding() {
            RecyclerView recyclerView = this.mRecyclerView;
            return recyclerView != null && recyclerView.mClipToPadding;
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(RecyclerView.TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            if (layoutParams instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) layoutParams);
            }
            if (layoutParams instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) layoutParams);
            }
            return new LayoutParams(layoutParams);
        }

        public LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
            return new LayoutParams(context, attributeSet);
        }

        public void smoothScrollToPosition(RecyclerView recyclerView, State state, int i) {
            Log.e(RecyclerView.TAG, "You must override smoothScrollToPosition to support smooth scrolling");
        }

        public void startSmoothScroll(SmoothScroller smoothScroller) {
            SmoothScroller smoothScroller2 = this.mSmoothScroller;
            if (!(smoothScroller2 == null || smoothScroller == smoothScroller2 || !smoothScroller2.isRunning())) {
                this.mSmoothScroller.stop();
            }
            this.mSmoothScroller = smoothScroller;
            this.mSmoothScroller.start(this.mRecyclerView, this);
        }

        public boolean isSmoothScrolling() {
            SmoothScroller smoothScroller = this.mSmoothScroller;
            return smoothScroller != null && smoothScroller.isRunning();
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(this.mRecyclerView);
        }

        public void endAnimation(View view) {
            if (this.mRecyclerView.mItemAnimator != null) {
                this.mRecyclerView.mItemAnimator.endAnimation(RecyclerView.getChildViewHolderInt(view));
            }
        }

        public void addDisappearingView(View view) {
            addDisappearingView(view, -1);
        }

        public void addDisappearingView(View view, int i) {
            addViewInt(view, i, true);
        }

        public void addView(View view) {
            addView(view, -1);
        }

        public void addView(View view, int i) {
            addViewInt(view, i, false);
        }

        private void addViewInt(View view, int i, boolean z) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (z || childViewHolderInt.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(childViewHolderInt);
            } else {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(childViewHolderInt);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (childViewHolderInt.wasReturnedFromScrap() || childViewHolderInt.isScrap()) {
                if (childViewHolderInt.isScrap()) {
                    childViewHolderInt.unScrap();
                } else {
                    childViewHolderInt.clearReturnedFromScrapFlag();
                }
                this.mChildHelper.attachViewToParent(view, i, view.getLayoutParams(), false);
            } else if (view.getParent() == this.mRecyclerView) {
                int indexOfChild = this.mChildHelper.indexOfChild(view);
                if (i == -1) {
                    i = this.mChildHelper.getChildCount();
                }
                if (indexOfChild == -1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Added View has RecyclerView as parent but view is not a real child. Unfiltered index:");
                    sb.append(this.mRecyclerView.indexOfChild(view));
                    sb.append(this.mRecyclerView.exceptionLabel());
                    throw new IllegalStateException(sb.toString());
                } else if (indexOfChild != i) {
                    this.mRecyclerView.mLayout.moveView(indexOfChild, i);
                }
            } else {
                this.mChildHelper.addView(view, i, false);
                layoutParams.mInsetsDirty = true;
                SmoothScroller smoothScroller = this.mSmoothScroller;
                if (smoothScroller != null && smoothScroller.isRunning()) {
                    this.mSmoothScroller.onChildAttachedToWindow(view);
                }
            }
            if (layoutParams.mPendingInvalidate) {
                childViewHolderInt.itemView.invalidate();
                layoutParams.mPendingInvalidate = false;
            }
        }

        public void removeView(View view) {
            this.mChildHelper.removeView(view);
        }

        public void removeViewAt(int i) {
            if (getChildAt(i) != null) {
                this.mChildHelper.removeViewAt(i);
            }
        }

        public void removeAllViews() {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                this.mChildHelper.removeViewAt(childCount);
            }
        }

        public int getPosition(@NonNull View view) {
            return ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }

        public int getItemViewType(@NonNull View view) {
            return RecyclerView.getChildViewHolderInt(view).getItemViewType();
        }

        @Nullable
        public View findContainingItemView(@NonNull View view) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null) {
                return null;
            }
            View findContainingItemView = recyclerView.findContainingItemView(view);
            if (findContainingItemView != null && !this.mChildHelper.isHidden(findContainingItemView)) {
                return findContainingItemView;
            }
            return null;
        }

        @Nullable
        public View findViewByPosition(int i) {
            int childCount = getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = getChildAt(i2);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt);
                if (childViewHolderInt != null && childViewHolderInt.getLayoutPosition() == i && !childViewHolderInt.shouldIgnore() && (this.mRecyclerView.mState.isPreLayout() || !childViewHolderInt.isRemoved())) {
                    return childAt;
                }
            }
            return null;
        }

        public void detachView(@NonNull View view) {
            int indexOfChild = this.mChildHelper.indexOfChild(view);
            if (indexOfChild >= 0) {
                detachViewInternal(indexOfChild, view);
            }
        }

        public void detachViewAt(int i) {
            detachViewInternal(i, getChildAt(i));
        }

        private void detachViewInternal(int i, @NonNull View view) {
            this.mChildHelper.detachViewFromParent(i);
        }

        public void attachView(@NonNull View view, int i, LayoutParams layoutParams) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(childViewHolderInt);
            } else {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(childViewHolderInt);
            }
            this.mChildHelper.attachViewToParent(view, i, layoutParams, childViewHolderInt.isRemoved());
        }

        public void attachView(@NonNull View view, int i) {
            attachView(view, i, (LayoutParams) view.getLayoutParams());
        }

        public void attachView(@NonNull View view) {
            attachView(view, -1);
        }

        public void removeDetachedView(@NonNull View view) {
            this.mRecyclerView.removeDetachedView(view, false);
        }

        public void moveView(int i, int i2) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                detachViewAt(i);
                attachView(childAt, i2);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot move a child from non-existing index:");
            sb.append(i);
            sb.append(this.mRecyclerView.toString());
            throw new IllegalArgumentException(sb.toString());
        }

        public void detachAndScrapView(@NonNull View view, @NonNull Recycler recycler) {
            scrapOrRecycleView(recycler, this.mChildHelper.indexOfChild(view), view);
        }

        public void detachAndScrapViewAt(int i, @NonNull Recycler recycler) {
            scrapOrRecycleView(recycler, i, getChildAt(i));
        }

        public void removeAndRecycleView(@NonNull View view, @NonNull Recycler recycler) {
            removeView(view);
            recycler.recycleView(view);
        }

        public void removeAndRecycleViewAt(int i, @NonNull Recycler recycler) {
            View childAt = getChildAt(i);
            removeViewAt(i);
            recycler.recycleView(childAt);
        }

        public int getChildCount() {
            ChildHelper childHelper = this.mChildHelper;
            if (childHelper != null) {
                return childHelper.getChildCount();
            }
            return 0;
        }

        @Nullable
        public View getChildAt(int i) {
            ChildHelper childHelper = this.mChildHelper;
            if (childHelper != null) {
                return childHelper.getChildAt(i);
            }
            return null;
        }

        public int getWidthMode() {
            return this.mWidthMode;
        }

        public int getHeightMode() {
            return this.mHeightMode;
        }

        @Px
        public int getWidth() {
            return this.mWidth;
        }

        @Px
        public int getHeight() {
            return this.mHeight;
        }

        @Px
        public int getPaddingLeft() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingLeft();
            }
            return 0;
        }

        @Px
        public int getPaddingTop() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingTop();
            }
            return 0;
        }

        @Px
        public int getPaddingRight() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingRight();
            }
            return 0;
        }

        @Px
        public int getPaddingBottom() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingBottom();
            }
            return 0;
        }

        @Px
        public int getPaddingStart() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return ViewCompat.getPaddingStart(recyclerView);
            }
            return 0;
        }

        @Px
        public int getPaddingEnd() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return ViewCompat.getPaddingEnd(recyclerView);
            }
            return 0;
        }

        public boolean isFocused() {
            RecyclerView recyclerView = this.mRecyclerView;
            return recyclerView != null && recyclerView.isFocused();
        }

        public boolean hasFocus() {
            RecyclerView recyclerView = this.mRecyclerView;
            return recyclerView != null && recyclerView.hasFocus();
        }

        @Nullable
        public View getFocusedChild() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null) {
                return null;
            }
            View focusedChild = recyclerView.getFocusedChild();
            if (focusedChild == null || this.mChildHelper.isHidden(focusedChild)) {
                return null;
            }
            return focusedChild;
        }

        public int getItemCount() {
            RecyclerView recyclerView = this.mRecyclerView;
            Adapter adapter = recyclerView != null ? recyclerView.getAdapter() : null;
            if (adapter != null) {
                return adapter.getItemCount();
            }
            return 0;
        }

        public void offsetChildrenHorizontal(@Px int i) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.offsetChildrenHorizontal(i);
            }
        }

        public void offsetChildrenVertical(@Px int i) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.offsetChildrenVertical(i);
            }
        }

        public void ignoreView(@NonNull View view) {
            ViewParent parent = view.getParent();
            RecyclerView recyclerView = this.mRecyclerView;
            if (parent != recyclerView || recyclerView.indexOfChild(view) == -1) {
                StringBuilder sb = new StringBuilder();
                sb.append("View should be fully attached to be ignored");
                sb.append(this.mRecyclerView.exceptionLabel());
                throw new IllegalArgumentException(sb.toString());
            }
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.addFlags(128);
            this.mRecyclerView.mViewInfoStore.removeViewHolder(childViewHolderInt);
        }

        public void stopIgnoringView(@NonNull View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.stopIgnoring();
            childViewHolderInt.resetInternal();
            childViewHolderInt.addFlags(4);
        }

        public void detachAndScrapAttachedViews(@NonNull Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                scrapOrRecycleView(recycler, childCount, getChildAt(childCount));
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int i, View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (!childViewHolderInt.shouldIgnore()) {
                if (!childViewHolderInt.isInvalid() || childViewHolderInt.isRemoved() || this.mRecyclerView.mAdapter.hasStableIds()) {
                    detachViewAt(i);
                    recycler.scrapView(view);
                    this.mRecyclerView.mViewInfoStore.onViewDetached(childViewHolderInt);
                } else {
                    removeViewAt(i);
                    recycler.recycleViewHolderInternal(childViewHolderInt);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void removeAndRecycleScrapInt(Recycler recycler) {
            int scrapCount = recycler.getScrapCount();
            for (int i = scrapCount - 1; i >= 0; i--) {
                View scrapViewAt = recycler.getScrapViewAt(i);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(scrapViewAt);
                if (!childViewHolderInt.shouldIgnore()) {
                    childViewHolderInt.setIsRecyclable(false);
                    if (childViewHolderInt.isTmpDetached()) {
                        this.mRecyclerView.removeDetachedView(scrapViewAt, false);
                    }
                    if (this.mRecyclerView.mItemAnimator != null) {
                        this.mRecyclerView.mItemAnimator.endAnimation(childViewHolderInt);
                    }
                    childViewHolderInt.setIsRecyclable(true);
                    recycler.quickRecycleScrapView(scrapViewAt);
                }
            }
            recycler.clearScrap();
            if (scrapCount > 0) {
                this.mRecyclerView.invalidate();
            }
        }

        public void measureChild(@NonNull View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect itemDecorInsetsForChild = this.mRecyclerView.getItemDecorInsetsForChild(view);
            int i3 = i2 + itemDecorInsetsForChild.top + itemDecorInsetsForChild.bottom;
            int childMeasureSpec = getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight() + i + itemDecorInsetsForChild.left + itemDecorInsetsForChild.right, layoutParams.width, canScrollHorizontally());
            int childMeasureSpec2 = getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom() + i3, layoutParams.height, canScrollVertically());
            if (shouldMeasureChild(view, childMeasureSpec, childMeasureSpec2, layoutParams)) {
                view.measure(childMeasureSpec, childMeasureSpec2);
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean shouldReMeasureChild(View view, int i, int i2, LayoutParams layoutParams) {
            return !this.mMeasurementCacheEnabled || !isMeasurementUpToDate(view.getMeasuredWidth(), i, layoutParams.width) || !isMeasurementUpToDate(view.getMeasuredHeight(), i2, layoutParams.height);
        }

        /* access modifiers changed from: 0000 */
        public boolean shouldMeasureChild(View view, int i, int i2, LayoutParams layoutParams) {
            return view.isLayoutRequested() || !this.mMeasurementCacheEnabled || !isMeasurementUpToDate(view.getWidth(), i, layoutParams.width) || !isMeasurementUpToDate(view.getHeight(), i2, layoutParams.height);
        }

        public boolean isMeasurementCacheEnabled() {
            return this.mMeasurementCacheEnabled;
        }

        public void setMeasurementCacheEnabled(boolean z) {
            this.mMeasurementCacheEnabled = z;
        }

        private static boolean isMeasurementUpToDate(int i, int i2, int i3) {
            int mode = MeasureSpec.getMode(i2);
            int size = MeasureSpec.getSize(i2);
            boolean z = false;
            if (i3 > 0 && i != i3) {
                return false;
            }
            if (mode == Integer.MIN_VALUE) {
                if (size >= i) {
                    z = true;
                }
                return z;
            } else if (mode == 0) {
                return true;
            } else {
                if (mode != 1073741824) {
                    return false;
                }
                if (size == i) {
                    z = true;
                }
                return z;
            }
        }

        public void measureChildWithMargins(@NonNull View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect itemDecorInsetsForChild = this.mRecyclerView.getItemDecorInsetsForChild(view);
            int i3 = i2 + itemDecorInsetsForChild.top + itemDecorInsetsForChild.bottom;
            int childMeasureSpec = getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin + i + itemDecorInsetsForChild.left + itemDecorInsetsForChild.right, layoutParams.width, canScrollHorizontally());
            int childMeasureSpec2 = getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin + i3, layoutParams.height, canScrollVertically());
            if (shouldMeasureChild(view, childMeasureSpec, childMeasureSpec2, layoutParams)) {
                view.measure(childMeasureSpec, childMeasureSpec2);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:2:0x000a, code lost:
            if (r3 >= 0) goto L_0x0011;
         */
        @java.lang.Deprecated
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static int getChildMeasureSpec(int r1, int r2, int r3, boolean r4) {
            /*
                int r1 = r1 - r2
                r2 = 0
                int r1 = java.lang.Math.max(r2, r1)
                r0 = 1073741824(0x40000000, float:2.0)
                if (r4 == 0) goto L_0x000f
                if (r3 < 0) goto L_0x000d
                goto L_0x0011
            L_0x000d:
                r1 = 0
                goto L_0x001e
            L_0x000f:
                if (r3 < 0) goto L_0x0015
            L_0x0011:
                r1 = r3
            L_0x0012:
                r2 = 1073741824(0x40000000, float:2.0)
                goto L_0x001e
            L_0x0015:
                r4 = -1
                if (r3 != r4) goto L_0x0019
                goto L_0x0012
            L_0x0019:
                r4 = -2
                if (r3 != r4) goto L_0x000d
                r2 = -2147483648(0xffffffff80000000, float:-0.0)
            L_0x001e:
                int r1 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r2)
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.LayoutManager.getChildMeasureSpec(int, int, int, boolean):int");
        }

        public static int getChildMeasureSpec(int i, int i2, int i3, int i4, boolean z) {
            int i5;
            int i6 = i - i3;
            int i7 = 0;
            int max = Math.max(0, i6);
            if (z) {
                if (i4 < 0) {
                    if (i4 == -1) {
                        if (i2 == Integer.MIN_VALUE || (i2 != 0 && i2 == 1073741824)) {
                            i5 = max;
                        } else {
                            i2 = 0;
                            i5 = 0;
                        }
                        i7 = i2;
                        max = i5;
                        return MeasureSpec.makeMeasureSpec(max, i7);
                    }
                    max = 0;
                    return MeasureSpec.makeMeasureSpec(max, i7);
                }
            } else if (i4 < 0) {
                if (i4 == -1) {
                    i7 = i2;
                } else {
                    if (i4 == -2) {
                        if (i2 == Integer.MIN_VALUE || i2 == 1073741824) {
                            i7 = Integer.MIN_VALUE;
                        }
                    }
                    max = 0;
                }
                return MeasureSpec.makeMeasureSpec(max, i7);
            }
            max = i4;
            i7 = 1073741824;
            return MeasureSpec.makeMeasureSpec(max, i7);
        }

        public int getDecoratedMeasuredWidth(@NonNull View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredWidth() + rect.left + rect.right;
        }

        public int getDecoratedMeasuredHeight(@NonNull View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredHeight() + rect.top + rect.bottom;
        }

        public void layoutDecorated(@NonNull View view, int i, int i2, int i3, int i4) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            view.layout(i + rect.left, i2 + rect.top, i3 - rect.right, i4 - rect.bottom);
        }

        public void layoutDecoratedWithMargins(@NonNull View view, int i, int i2, int i3, int i4) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect rect = layoutParams.mDecorInsets;
            view.layout(i + rect.left + layoutParams.leftMargin, i2 + rect.top + layoutParams.topMargin, (i3 - rect.right) - layoutParams.rightMargin, (i4 - rect.bottom) - layoutParams.bottomMargin);
        }

        public void getTransformedBoundingBox(@NonNull View view, boolean z, @NonNull Rect rect) {
            if (z) {
                Rect rect2 = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
                rect.set(-rect2.left, -rect2.top, view.getWidth() + rect2.right, view.getHeight() + rect2.bottom);
            } else {
                rect.set(0, 0, view.getWidth(), view.getHeight());
            }
            if (this.mRecyclerView != null) {
                Matrix matrix = view.getMatrix();
                if (matrix != null && !matrix.isIdentity()) {
                    RectF rectF = this.mRecyclerView.mTempRectF;
                    rectF.set(rect);
                    matrix.mapRect(rectF);
                    rect.set((int) Math.floor((double) rectF.left), (int) Math.floor((double) rectF.top), (int) Math.ceil((double) rectF.right), (int) Math.ceil((double) rectF.bottom));
                }
            }
            rect.offset(view.getLeft(), view.getTop());
        }

        public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect rect) {
            RecyclerView.getDecoratedBoundsWithMarginsInt(view, rect);
        }

        public int getDecoratedLeft(@NonNull View view) {
            return view.getLeft() - getLeftDecorationWidth(view);
        }

        public int getDecoratedTop(@NonNull View view) {
            return view.getTop() - getTopDecorationHeight(view);
        }

        public int getDecoratedRight(@NonNull View view) {
            return view.getRight() + getRightDecorationWidth(view);
        }

        public int getDecoratedBottom(@NonNull View view) {
            return view.getBottom() + getBottomDecorationHeight(view);
        }

        public void calculateItemDecorationsForChild(@NonNull View view, @NonNull Rect rect) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null) {
                rect.set(0, 0, 0, 0);
            } else {
                rect.set(recyclerView.getItemDecorInsetsForChild(view));
            }
        }

        public int getTopDecorationHeight(@NonNull View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.top;
        }

        public int getBottomDecorationHeight(@NonNull View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.bottom;
        }

        public int getLeftDecorationWidth(@NonNull View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.left;
        }

        public int getRightDecorationWidth(@NonNull View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.right;
        }

        private int[] getChildRectangleOnScreenScrollAmount(RecyclerView recyclerView, View view, Rect rect, boolean z) {
            int[] iArr = new int[2];
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int width = getWidth() - getPaddingRight();
            int height = getHeight() - getPaddingBottom();
            int left = (view.getLeft() + rect.left) - view.getScrollX();
            int top = (view.getTop() + rect.top) - view.getScrollY();
            int width2 = rect.width() + left;
            int height2 = rect.height() + top;
            int i = left - paddingLeft;
            int min = Math.min(0, i);
            int i2 = top - paddingTop;
            int min2 = Math.min(0, i2);
            int i3 = width2 - width;
            int max = Math.max(0, i3);
            int max2 = Math.max(0, height2 - height);
            if (getLayoutDirection() != 1) {
                if (min == 0) {
                    min = Math.min(i, max);
                }
                max = min;
            } else if (max == 0) {
                max = Math.max(min, i3);
            }
            if (min2 == 0) {
                min2 = Math.min(i2, max2);
            }
            iArr[0] = max;
            iArr[1] = min2;
            return iArr;
        }

        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView recyclerView, @NonNull View view, @NonNull Rect rect, boolean z) {
            return requestChildRectangleOnScreen(recyclerView, view, rect, z, false);
        }

        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView recyclerView, @NonNull View view, @NonNull Rect rect, boolean z, boolean z2) {
            int[] childRectangleOnScreenScrollAmount = getChildRectangleOnScreenScrollAmount(recyclerView, view, rect, z);
            int i = childRectangleOnScreenScrollAmount[0];
            int i2 = childRectangleOnScreenScrollAmount[1];
            if ((z2 && !isFocusedChildVisibleAfterScrolling(recyclerView, i, i2)) || (i == 0 && i2 == 0)) {
                return false;
            }
            if (z) {
                recyclerView.scrollBy(i, i2);
            } else {
                recyclerView.smoothScrollBy(i, i2);
            }
            return true;
        }

        public boolean isViewPartiallyVisible(@NonNull View view, boolean z, boolean z2) {
            boolean z3 = this.mHorizontalBoundCheck.isViewWithinBoundFlags(view, 24579) && this.mVerticalBoundCheck.isViewWithinBoundFlags(view, 24579);
            return z ? z3 : !z3;
        }

        private boolean isFocusedChildVisibleAfterScrolling(RecyclerView recyclerView, int i, int i2) {
            View focusedChild = recyclerView.getFocusedChild();
            if (focusedChild == null) {
                return false;
            }
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int width = getWidth() - getPaddingRight();
            int height = getHeight() - getPaddingBottom();
            Rect rect = this.mRecyclerView.mTempRect;
            getDecoratedBoundsWithMargins(focusedChild, rect);
            if (rect.left - i >= width || rect.right - i <= paddingLeft || rect.top - i2 >= height || rect.bottom - i2 <= paddingTop) {
                return false;
            }
            return true;
        }

        @Deprecated
        public boolean onRequestChildFocus(@NonNull RecyclerView recyclerView, @NonNull View view, @Nullable View view2) {
            return isSmoothScrolling() || recyclerView.isComputingLayout();
        }

        public boolean onRequestChildFocus(@NonNull RecyclerView recyclerView, @NonNull State state, @NonNull View view, @Nullable View view2) {
            return onRequestChildFocus(recyclerView, view, view2);
        }

        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int i, int i2, @Nullable Object obj) {
            onItemsUpdated(recyclerView, i, i2);
        }

        public void onMeasure(@NonNull Recycler recycler, @NonNull State state, int i, int i2) {
            this.mRecyclerView.defaultOnMeasure(i, i2);
        }

        public void setMeasuredDimension(int i, int i2) {
            this.mRecyclerView.setMeasuredDimension(i, i2);
        }

        @Px
        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(this.mRecyclerView);
        }

        @Px
        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(this.mRecyclerView);
        }

        /* access modifiers changed from: 0000 */
        public void stopSmoothScroller() {
            SmoothScroller smoothScroller = this.mSmoothScroller;
            if (smoothScroller != null) {
                smoothScroller.stop();
            }
        }

        /* access modifiers changed from: 0000 */
        public void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (this.mSmoothScroller == smoothScroller) {
                this.mSmoothScroller = null;
            }
        }

        public void removeAndRecycleAllViews(@NonNull Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                if (!RecyclerView.getChildViewHolderInt(getChildAt(childCount)).shouldIgnore()) {
                    removeAndRecycleViewAt(childCount, recycler);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            onInitializeAccessibilityNodeInfo(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityNodeInfoCompat);
        }

        public void onInitializeAccessibilityNodeInfo(@NonNull Recycler recycler, @NonNull State state, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            if (this.mRecyclerView.canScrollVertically(-1) || this.mRecyclerView.canScrollHorizontally(-1)) {
                accessibilityNodeInfoCompat.addAction(8192);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            if (this.mRecyclerView.canScrollVertically(1) || this.mRecyclerView.canScrollHorizontally(1)) {
                accessibilityNodeInfoCompat.addAction(4096);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            accessibilityNodeInfoCompat.setCollectionInfo(CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        }

        public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent accessibilityEvent) {
            onInitializeAccessibilityEvent(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityEvent);
        }

        public void onInitializeAccessibilityEvent(@NonNull Recycler recycler, @NonNull State state, @NonNull AccessibilityEvent accessibilityEvent) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null && accessibilityEvent != null) {
                boolean z = true;
                if (!recyclerView.canScrollVertically(1) && !this.mRecyclerView.canScrollVertically(-1) && !this.mRecyclerView.canScrollHorizontally(-1) && !this.mRecyclerView.canScrollHorizontally(1)) {
                    z = false;
                }
                accessibilityEvent.setScrollable(z);
                if (this.mRecyclerView.mAdapter != null) {
                    accessibilityEvent.setItemCount(this.mRecyclerView.mAdapter.getItemCount());
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void onInitializeAccessibilityNodeInfoForItem(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt != null && !childViewHolderInt.isRemoved() && !this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                onInitializeAccessibilityNodeInfoForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, accessibilityNodeInfoCompat);
            }
        }

        public void onInitializeAccessibilityNodeInfoForItem(@NonNull Recycler recycler, @NonNull State state, @NonNull View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(CollectionItemInfoCompat.obtain(canScrollVertically() ? getPosition(view) : 0, 1, canScrollHorizontally() ? getPosition(view) : 0, 1, false, false));
        }

        public void requestSimpleAnimationsInNextLayout() {
            this.mRequestedSimpleAnimations = true;
        }

        public int getRowCountForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || recyclerView.mAdapter == null || !canScrollVertically()) {
                return 1;
            }
            return this.mRecyclerView.mAdapter.getItemCount();
        }

        public int getColumnCountForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || recyclerView.mAdapter == null || !canScrollHorizontally()) {
                return 1;
            }
            return this.mRecyclerView.mAdapter.getItemCount();
        }

        /* access modifiers changed from: 0000 */
        public boolean performAccessibilityAction(int i, @Nullable Bundle bundle) {
            return performAccessibilityAction(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, i, bundle);
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0070 A[ADDED_TO_REGION] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performAccessibilityAction(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.Recycler r2, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.State r3, int r4, @androidx.annotation.Nullable android.os.Bundle r5) {
            /*
                r1 = this;
                androidx.recyclerview.widget.RecyclerView r2 = r1.mRecyclerView
                r3 = 0
                if (r2 != 0) goto L_0x0006
                return r3
            L_0x0006:
                r5 = 4096(0x1000, float:5.74E-42)
                r0 = 1
                if (r4 == r5) goto L_0x0042
                r5 = 8192(0x2000, float:1.14794E-41)
                if (r4 == r5) goto L_0x0012
                r2 = 0
            L_0x0010:
                r4 = 0
                goto L_0x006e
            L_0x0012:
                r4 = -1
                boolean r2 = r2.canScrollVertically(r4)
                if (r2 == 0) goto L_0x0029
                int r2 = r1.getHeight()
                int r5 = r1.getPaddingTop()
                int r2 = r2 - r5
                int r5 = r1.getPaddingBottom()
                int r2 = r2 - r5
                int r2 = -r2
                goto L_0x002a
            L_0x0029:
                r2 = 0
            L_0x002a:
                androidx.recyclerview.widget.RecyclerView r5 = r1.mRecyclerView
                boolean r4 = r5.canScrollHorizontally(r4)
                if (r4 == 0) goto L_0x0010
                int r4 = r1.getWidth()
                int r5 = r1.getPaddingLeft()
                int r4 = r4 - r5
                int r5 = r1.getPaddingRight()
                int r4 = r4 - r5
                int r4 = -r4
                goto L_0x006e
            L_0x0042:
                boolean r2 = r2.canScrollVertically(r0)
                if (r2 == 0) goto L_0x0057
                int r2 = r1.getHeight()
                int r4 = r1.getPaddingTop()
                int r2 = r2 - r4
                int r4 = r1.getPaddingBottom()
                int r2 = r2 - r4
                goto L_0x0058
            L_0x0057:
                r2 = 0
            L_0x0058:
                androidx.recyclerview.widget.RecyclerView r4 = r1.mRecyclerView
                boolean r4 = r4.canScrollHorizontally(r0)
                if (r4 == 0) goto L_0x0010
                int r4 = r1.getWidth()
                int r5 = r1.getPaddingLeft()
                int r4 = r4 - r5
                int r5 = r1.getPaddingRight()
                int r4 = r4 - r5
            L_0x006e:
                if (r2 != 0) goto L_0x0073
                if (r4 != 0) goto L_0x0073
                return r3
            L_0x0073:
                androidx.recyclerview.widget.RecyclerView r3 = r1.mRecyclerView
                r3.smoothScrollBy(r4, r2)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.LayoutManager.performAccessibilityAction(androidx.recyclerview.widget.RecyclerView$Recycler, androidx.recyclerview.widget.RecyclerView$State, int, android.os.Bundle):boolean");
        }

        /* access modifiers changed from: 0000 */
        public boolean performAccessibilityActionForItem(@NonNull View view, int i, @Nullable Bundle bundle) {
            return performAccessibilityActionForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, i, bundle);
        }

        public static Properties getProperties(@NonNull Context context, @Nullable AttributeSet attributeSet, int i, int i2) {
            Properties properties = new Properties();
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RecyclerView, i, i2);
            properties.orientation = obtainStyledAttributes.getInt(R.styleable.RecyclerView_android_orientation, 1);
            properties.spanCount = obtainStyledAttributes.getInt(R.styleable.RecyclerView_spanCount, 1);
            properties.reverseLayout = obtainStyledAttributes.getBoolean(R.styleable.RecyclerView_reverseLayout, false);
            properties.stackFromEnd = obtainStyledAttributes.getBoolean(R.styleable.RecyclerView_stackFromEnd, false);
            obtainStyledAttributes.recycle();
            return properties;
        }

        /* access modifiers changed from: 0000 */
        public void setExactMeasureSpecsFrom(RecyclerView recyclerView) {
            setMeasureSpecs(MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), 1073741824));
        }

        /* access modifiers changed from: 0000 */
        public boolean hasFlexibleChildInBothOrientations() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                android.view.ViewGroup.LayoutParams layoutParams = getChildAt(i).getLayoutParams();
                if (layoutParams.width < 0 && layoutParams.height < 0) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;
        boolean mPendingInvalidate = false;
        ViewHolder mViewHolder;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean viewNeedsUpdate() {
            return this.mViewHolder.needsUpdate();
        }

        public boolean isViewInvalid() {
            return this.mViewHolder.isInvalid();
        }

        public boolean isItemRemoved() {
            return this.mViewHolder.isRemoved();
        }

        public boolean isItemChanged() {
            return this.mViewHolder.isUpdated();
        }

        @Deprecated
        public int getViewPosition() {
            return this.mViewHolder.getPosition();
        }

        public int getViewLayoutPosition() {
            return this.mViewHolder.getLayoutPosition();
        }

        public int getViewAdapterPosition() {
            return this.mViewHolder.getAdapterPosition();
        }
    }

    public interface OnChildAttachStateChangeListener {
        void onChildViewAttachedToWindow(@NonNull View view);

        void onChildViewDetachedFromWindow(@NonNull View view);
    }

    public static abstract class OnFlingListener {
        public abstract boolean onFling(int i, int i2);
    }

    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent);

        void onRequestDisallowInterceptTouchEvent(boolean z);

        void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent);
    }

    public static abstract class OnScrollListener {
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int i) {
        }

        public void onScrolled(@NonNull RecyclerView recyclerView, int i, int i2) {
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;
        private int mAttachCount = 0;
        SparseArray<ScrapData> mScrap = new SparseArray<>();

        static class ScrapData {
            long mBindRunningAverageNs = 0;
            long mCreateRunningAverageNs = 0;
            int mMaxScrap = 5;
            final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();

            ScrapData() {
            }
        }

        public void clear() {
            for (int i = 0; i < this.mScrap.size(); i++) {
                ((ScrapData) this.mScrap.valueAt(i)).mScrapHeap.clear();
            }
        }

        public void setMaxRecycledViews(int i, int i2) {
            ScrapData scrapDataForType = getScrapDataForType(i);
            scrapDataForType.mMaxScrap = i2;
            ArrayList<ViewHolder> arrayList = scrapDataForType.mScrapHeap;
            while (arrayList.size() > i2) {
                arrayList.remove(arrayList.size() - 1);
            }
        }

        public int getRecycledViewCount(int i) {
            return getScrapDataForType(i).mScrapHeap.size();
        }

        @Nullable
        public ViewHolder getRecycledView(int i) {
            ScrapData scrapData = (ScrapData) this.mScrap.get(i);
            if (scrapData == null || scrapData.mScrapHeap.isEmpty()) {
                return null;
            }
            ArrayList<ViewHolder> arrayList = scrapData.mScrapHeap;
            return (ViewHolder) arrayList.remove(arrayList.size() - 1);
        }

        /* access modifiers changed from: 0000 */
        public int size() {
            int i = 0;
            for (int i2 = 0; i2 < this.mScrap.size(); i2++) {
                ArrayList<ViewHolder> arrayList = ((ScrapData) this.mScrap.valueAt(i2)).mScrapHeap;
                if (arrayList != null) {
                    i += arrayList.size();
                }
            }
            return i;
        }

        public void putRecycledView(ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            ArrayList<ViewHolder> arrayList = getScrapDataForType(itemViewType).mScrapHeap;
            if (((ScrapData) this.mScrap.get(itemViewType)).mMaxScrap > arrayList.size()) {
                viewHolder.resetInternal();
                arrayList.add(viewHolder);
            }
        }

        /* access modifiers changed from: 0000 */
        public long runningAverage(long j, long j2) {
            return j == 0 ? j2 : ((j / 4) * 3) + (j2 / 4);
        }

        /* access modifiers changed from: 0000 */
        public void factorInCreateTime(int i, long j) {
            ScrapData scrapDataForType = getScrapDataForType(i);
            scrapDataForType.mCreateRunningAverageNs = runningAverage(scrapDataForType.mCreateRunningAverageNs, j);
        }

        /* access modifiers changed from: 0000 */
        public void factorInBindTime(int i, long j) {
            ScrapData scrapDataForType = getScrapDataForType(i);
            scrapDataForType.mBindRunningAverageNs = runningAverage(scrapDataForType.mBindRunningAverageNs, j);
        }

        /* access modifiers changed from: 0000 */
        public boolean willCreateInTime(int i, long j, long j2) {
            long j3 = getScrapDataForType(i).mCreateRunningAverageNs;
            return j3 == 0 || j + j3 < j2;
        }

        /* access modifiers changed from: 0000 */
        public boolean willBindInTime(int i, long j, long j2) {
            long j3 = getScrapDataForType(i).mBindRunningAverageNs;
            return j3 == 0 || j + j3 < j2;
        }

        /* access modifiers changed from: 0000 */
        public void attach() {
            this.mAttachCount++;
        }

        /* access modifiers changed from: 0000 */
        public void detach() {
            this.mAttachCount--;
        }

        /* access modifiers changed from: 0000 */
        public void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            if (adapter != null) {
                detach();
            }
            if (!z && this.mAttachCount == 0) {
                clear();
            }
            if (adapter2 != null) {
                attach();
            }
        }

        private ScrapData getScrapDataForType(int i) {
            ScrapData scrapData = (ScrapData) this.mScrap.get(i);
            if (scrapData != null) {
                return scrapData;
            }
            ScrapData scrapData2 = new ScrapData();
            this.mScrap.put(i, scrapData2);
            return scrapData2;
        }
    }

    public final class Recycler {
        static final int DEFAULT_CACHE_SIZE = 2;
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        final ArrayList<ViewHolder> mCachedViews = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;
        RecycledViewPool mRecyclerPool;
        private int mRequestedCacheMax = 2;
        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(this.mAttachedScrap);
        private ViewCacheExtension mViewCacheExtension;
        int mViewCacheMax = 2;

        public Recycler() {
        }

        public void clear() {
            this.mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        public void setViewCacheSize(int i) {
            this.mRequestedCacheMax = i;
            updateViewCacheSize();
        }

        /* access modifiers changed from: 0000 */
        public void updateViewCacheSize() {
            this.mViewCacheMax = this.mRequestedCacheMax + (RecyclerView.this.mLayout != null ? RecyclerView.this.mLayout.mPrefetchMaxCountObserved : 0);
            for (int size = this.mCachedViews.size() - 1; size >= 0 && this.mCachedViews.size() > this.mViewCacheMax; size--) {
                recycleCachedViewAt(size);
            }
        }

        @NonNull
        public List<ViewHolder> getScrapList() {
            return this.mUnmodifiableAttachedScrap;
        }

        /* access modifiers changed from: 0000 */
        public boolean validateViewHolderForOffsetPosition(ViewHolder viewHolder) {
            if (viewHolder.isRemoved()) {
                return RecyclerView.this.mState.isPreLayout();
            }
            if (viewHolder.mPosition < 0 || viewHolder.mPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Inconsistency detected. Invalid view holder adapter position");
                sb.append(viewHolder);
                sb.append(RecyclerView.this.exceptionLabel());
                throw new IndexOutOfBoundsException(sb.toString());
            }
            boolean z = false;
            if (!RecyclerView.this.mState.isPreLayout() && RecyclerView.this.mAdapter.getItemViewType(viewHolder.mPosition) != viewHolder.getItemViewType()) {
                return false;
            }
            if (!RecyclerView.this.mAdapter.hasStableIds()) {
                return true;
            }
            if (viewHolder.getItemId() == RecyclerView.this.mAdapter.getItemId(viewHolder.mPosition)) {
                z = true;
            }
            return z;
        }

        private boolean tryBindViewHolderByDeadline(@NonNull ViewHolder viewHolder, int i, int i2, long j) {
            viewHolder.mOwnerRecyclerView = RecyclerView.this;
            int itemViewType = viewHolder.getItemViewType();
            long nanoTime = RecyclerView.this.getNanoTime();
            if (j != RecyclerView.FOREVER_NS && !this.mRecyclerPool.willBindInTime(itemViewType, nanoTime, j)) {
                return false;
            }
            RecyclerView.this.mAdapter.bindViewHolder(viewHolder, i);
            this.mRecyclerPool.factorInBindTime(viewHolder.getItemViewType(), RecyclerView.this.getNanoTime() - nanoTime);
            attachAccessibilityDelegateOnBind(viewHolder);
            if (RecyclerView.this.mState.isPreLayout()) {
                viewHolder.mPreLayoutPosition = i2;
            }
            return true;
        }

        public void bindViewToPosition(@NonNull View view, int i) {
            LayoutParams layoutParams;
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt != null) {
                int findPositionOffset = RecyclerView.this.mAdapterHelper.findPositionOffset(i);
                if (findPositionOffset < 0 || findPositionOffset >= RecyclerView.this.mAdapter.getItemCount()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Inconsistency detected. Invalid item position ");
                    sb.append(i);
                    sb.append("(offset:");
                    sb.append(findPositionOffset);
                    sb.append(").");
                    sb.append("state:");
                    sb.append(RecyclerView.this.mState.getItemCount());
                    sb.append(RecyclerView.this.exceptionLabel());
                    throw new IndexOutOfBoundsException(sb.toString());
                }
                tryBindViewHolderByDeadline(childViewHolderInt, findPositionOffset, i, RecyclerView.FOREVER_NS);
                android.view.ViewGroup.LayoutParams layoutParams2 = childViewHolderInt.itemView.getLayoutParams();
                if (layoutParams2 == null) {
                    layoutParams = (LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                    childViewHolderInt.itemView.setLayoutParams(layoutParams);
                } else if (!RecyclerView.this.checkLayoutParams(layoutParams2)) {
                    layoutParams = (LayoutParams) RecyclerView.this.generateLayoutParams(layoutParams2);
                    childViewHolderInt.itemView.setLayoutParams(layoutParams);
                } else {
                    layoutParams = (LayoutParams) layoutParams2;
                }
                boolean z = true;
                layoutParams.mInsetsDirty = true;
                layoutParams.mViewHolder = childViewHolderInt;
                if (childViewHolderInt.itemView.getParent() != null) {
                    z = false;
                }
                layoutParams.mPendingInvalidate = z;
                return;
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("The view does not have a ViewHolder. You cannot pass arbitrary views to this method, they should be created by the Adapter");
            sb2.append(RecyclerView.this.exceptionLabel());
            throw new IllegalArgumentException(sb2.toString());
        }

        public int convertPreLayoutPositionToPostLayout(int i) {
            if (i < 0 || i >= RecyclerView.this.mState.getItemCount()) {
                StringBuilder sb = new StringBuilder();
                sb.append("invalid position ");
                sb.append(i);
                sb.append(". State ");
                sb.append("item count is ");
                sb.append(RecyclerView.this.mState.getItemCount());
                sb.append(RecyclerView.this.exceptionLabel());
                throw new IndexOutOfBoundsException(sb.toString());
            } else if (!RecyclerView.this.mState.isPreLayout()) {
                return i;
            } else {
                return RecyclerView.this.mAdapterHelper.findPositionOffset(i);
            }
        }

        @NonNull
        public View getViewForPosition(int i) {
            return getViewForPosition(i, false);
        }

        /* access modifiers changed from: 0000 */
        public View getViewForPosition(int i, boolean z) {
            return tryGetViewHolderForPositionByDeadline(i, z, RecyclerView.FOREVER_NS).itemView;
        }

        /* access modifiers changed from: 0000 */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x005f  */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x01a8  */
        /* JADX WARNING: Removed duplicated region for block: B:81:0x01cd  */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x0206  */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x0214  */
        @androidx.annotation.Nullable
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public androidx.recyclerview.widget.RecyclerView.ViewHolder tryGetViewHolderForPositionByDeadline(int r17, boolean r18, long r19) {
            /*
                r16 = this;
                r6 = r16
                r3 = r17
                r0 = r18
                if (r3 < 0) goto L_0x0237
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r1 = r1.mState
                int r1 = r1.getItemCount()
                if (r3 >= r1) goto L_0x0237
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r1 = r1.mState
                boolean r1 = r1.isPreLayout()
                r2 = 0
                r7 = 1
                r8 = 0
                if (r1 == 0) goto L_0x0027
                androidx.recyclerview.widget.RecyclerView$ViewHolder r1 = r16.getChangedScrapViewForPosition(r17)
                if (r1 == 0) goto L_0x0028
                r4 = 1
                goto L_0x0029
            L_0x0027:
                r1 = r2
            L_0x0028:
                r4 = 0
            L_0x0029:
                if (r1 != 0) goto L_0x005d
                androidx.recyclerview.widget.RecyclerView$ViewHolder r1 = r16.getScrapOrHiddenOrCachedHolderForPosition(r17, r18)
                if (r1 == 0) goto L_0x005d
                boolean r5 = r6.validateViewHolderForOffsetPosition(r1)
                if (r5 != 0) goto L_0x005c
                if (r0 != 0) goto L_0x005a
                r5 = 4
                r1.addFlags(r5)
                boolean r5 = r1.isScrap()
                if (r5 == 0) goto L_0x004e
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                android.view.View r9 = r1.itemView
                r5.removeDetachedView(r9, r8)
                r1.unScrap()
                goto L_0x0057
            L_0x004e:
                boolean r5 = r1.wasReturnedFromScrap()
                if (r5 == 0) goto L_0x0057
                r1.clearReturnedFromScrapFlag()
            L_0x0057:
                r6.recycleViewHolderInternal(r1)
            L_0x005a:
                r1 = r2
                goto L_0x005d
            L_0x005c:
                r4 = 1
            L_0x005d:
                if (r1 != 0) goto L_0x0187
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.AdapterHelper r5 = r5.mAdapterHelper
                int r5 = r5.findPositionOffset(r3)
                if (r5 < 0) goto L_0x014a
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r9 = r9.mAdapter
                int r9 = r9.getItemCount()
                if (r5 >= r9) goto L_0x014a
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r9 = r9.mAdapter
                int r9 = r9.getItemViewType(r5)
                androidx.recyclerview.widget.RecyclerView r10 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r10 = r10.mAdapter
                boolean r10 = r10.hasStableIds()
                if (r10 == 0) goto L_0x0096
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r1 = r1.mAdapter
                long r10 = r1.getItemId(r5)
                androidx.recyclerview.widget.RecyclerView$ViewHolder r1 = r6.getScrapOrCachedViewForId(r10, r9, r0)
                if (r1 == 0) goto L_0x0096
                r1.mPosition = r5
                r4 = 1
            L_0x0096:
                if (r1 != 0) goto L_0x00eb
                androidx.recyclerview.widget.RecyclerView$ViewCacheExtension r0 = r6.mViewCacheExtension
                if (r0 == 0) goto L_0x00eb
                android.view.View r0 = r0.getViewForPositionAndType(r6, r3, r9)
                if (r0 == 0) goto L_0x00eb
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$ViewHolder r1 = r1.getChildViewHolder(r0)
                if (r1 == 0) goto L_0x00ce
                boolean r0 = r1.shouldIgnore()
                if (r0 != 0) goto L_0x00b1
                goto L_0x00eb
            L_0x00b1:
                java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view."
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                java.lang.String r2 = r2.exceptionLabel()
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                r0.<init>(r1)
                throw r0
            L_0x00ce:
                java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "getViewForPositionAndType returned a view which does not have a ViewHolder"
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                java.lang.String r2 = r2.exceptionLabel()
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                r0.<init>(r1)
                throw r0
            L_0x00eb:
                if (r1 != 0) goto L_0x0101
                androidx.recyclerview.widget.RecyclerView$RecycledViewPool r0 = r16.getRecycledViewPool()
                androidx.recyclerview.widget.RecyclerView$ViewHolder r1 = r0.getRecycledView(r9)
                if (r1 == 0) goto L_0x0101
                r1.resetInternal()
                boolean r0 = androidx.recyclerview.widget.RecyclerView.FORCE_INVALIDATE_DISPLAY_LIST
                if (r0 == 0) goto L_0x0101
                r6.invalidateDisplayListInt(r1)
            L_0x0101:
                if (r1 != 0) goto L_0x0187
                androidx.recyclerview.widget.RecyclerView r0 = androidx.recyclerview.widget.RecyclerView.this
                long r0 = r0.getNanoTime()
                r10 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r5 = (r19 > r10 ? 1 : (r19 == r10 ? 0 : -1))
                if (r5 == 0) goto L_0x011f
                androidx.recyclerview.widget.RecyclerView$RecycledViewPool r10 = r6.mRecyclerPool
                r11 = r9
                r12 = r0
                r14 = r19
                boolean r5 = r10.willCreateInTime(r11, r12, r14)
                if (r5 != 0) goto L_0x011f
                return r2
            L_0x011f:
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r2 = r2.mAdapter
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$ViewHolder r2 = r2.createViewHolder(r5, r9)
                boolean r5 = androidx.recyclerview.widget.RecyclerView.ALLOW_THREAD_GAP_WORK
                if (r5 == 0) goto L_0x013c
                android.view.View r5 = r2.itemView
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.findNestedRecyclerView(r5)
                if (r5 == 0) goto L_0x013c
                java.lang.ref.WeakReference r10 = new java.lang.ref.WeakReference
                r10.<init>(r5)
                r2.mNestedRecyclerView = r10
            L_0x013c:
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                long r10 = r5.getNanoTime()
                androidx.recyclerview.widget.RecyclerView$RecycledViewPool r5 = r6.mRecyclerPool
                long r10 = r10 - r0
                r5.factorInCreateTime(r9, r10)
                r10 = r2
                goto L_0x0188
            L_0x014a:
                java.lang.IndexOutOfBoundsException r0 = new java.lang.IndexOutOfBoundsException
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Inconsistency detected. Invalid item position "
                r1.append(r2)
                r1.append(r3)
                java.lang.String r2 = "(offset:"
                r1.append(r2)
                r1.append(r5)
                java.lang.String r2 = ")."
                r1.append(r2)
                java.lang.String r2 = "state:"
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r2 = r2.mState
                int r2 = r2.getItemCount()
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                java.lang.String r2 = r2.exceptionLabel()
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                r0.<init>(r1)
                throw r0
            L_0x0187:
                r10 = r1
            L_0x0188:
                r9 = r4
                if (r9 == 0) goto L_0x01c3
                androidx.recyclerview.widget.RecyclerView r0 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r0 = r0.mState
                boolean r0 = r0.isPreLayout()
                if (r0 != 0) goto L_0x01c3
                r0 = 8192(0x2000, float:1.14794E-41)
                boolean r1 = r10.hasAnyOfTheFlags(r0)
                if (r1 == 0) goto L_0x01c3
                r10.setFlags(r8, r0)
                androidx.recyclerview.widget.RecyclerView r0 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r0 = r0.mState
                boolean r0 = r0.mRunSimpleAnimations
                if (r0 == 0) goto L_0x01c3
                int r0 = androidx.recyclerview.widget.RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(r10)
                r0 = r0 | 4096(0x1000, float:5.74E-42)
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$ItemAnimator r1 = r1.mItemAnimator
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r2 = r2.mState
                java.util.List r4 = r10.getUnmodifiedPayloads()
                androidx.recyclerview.widget.RecyclerView$ItemAnimator$ItemHolderInfo r0 = r1.recordPreLayoutInformation(r2, r10, r0, r4)
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                r1.recordAnimationInfoIfBouncedHiddenView(r10, r0)
            L_0x01c3:
                androidx.recyclerview.widget.RecyclerView r0 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r0 = r0.mState
                boolean r0 = r0.isPreLayout()
                if (r0 == 0) goto L_0x01d6
                boolean r0 = r10.isBound()
                if (r0 == 0) goto L_0x01d6
                r10.mPreLayoutPosition = r3
                goto L_0x01e9
            L_0x01d6:
                boolean r0 = r10.isBound()
                if (r0 == 0) goto L_0x01eb
                boolean r0 = r10.needsUpdate()
                if (r0 != 0) goto L_0x01eb
                boolean r0 = r10.isInvalid()
                if (r0 == 0) goto L_0x01e9
                goto L_0x01eb
            L_0x01e9:
                r0 = 0
                goto L_0x01fe
            L_0x01eb:
                androidx.recyclerview.widget.RecyclerView r0 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.AdapterHelper r0 = r0.mAdapterHelper
                int r2 = r0.findPositionOffset(r3)
                r0 = r16
                r1 = r10
                r3 = r17
                r4 = r19
                boolean r0 = r0.tryBindViewHolderByDeadline(r1, r2, r3, r4)
            L_0x01fe:
                android.view.View r1 = r10.itemView
                android.view.ViewGroup$LayoutParams r1 = r1.getLayoutParams()
                if (r1 != 0) goto L_0x0214
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                android.view.ViewGroup$LayoutParams r1 = r1.generateDefaultLayoutParams()
                androidx.recyclerview.widget.RecyclerView$LayoutParams r1 = (androidx.recyclerview.widget.RecyclerView.LayoutParams) r1
                android.view.View r2 = r10.itemView
                r2.setLayoutParams(r1)
                goto L_0x022c
            L_0x0214:
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                boolean r2 = r2.checkLayoutParams(r1)
                if (r2 != 0) goto L_0x022a
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                android.view.ViewGroup$LayoutParams r1 = r2.generateLayoutParams(r1)
                androidx.recyclerview.widget.RecyclerView$LayoutParams r1 = (androidx.recyclerview.widget.RecyclerView.LayoutParams) r1
                android.view.View r2 = r10.itemView
                r2.setLayoutParams(r1)
                goto L_0x022c
            L_0x022a:
                androidx.recyclerview.widget.RecyclerView$LayoutParams r1 = (androidx.recyclerview.widget.RecyclerView.LayoutParams) r1
            L_0x022c:
                r1.mViewHolder = r10
                if (r9 == 0) goto L_0x0233
                if (r0 == 0) goto L_0x0233
                goto L_0x0234
            L_0x0233:
                r7 = 0
            L_0x0234:
                r1.mPendingInvalidate = r7
                return r10
            L_0x0237:
                java.lang.IndexOutOfBoundsException r0 = new java.lang.IndexOutOfBoundsException
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Invalid item position "
                r1.append(r2)
                r1.append(r3)
                java.lang.String r2 = "("
                r1.append(r2)
                r1.append(r3)
                java.lang.String r2 = "). Item count:"
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r2 = r2.mState
                int r2 = r2.getItemCount()
                r1.append(r2)
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                java.lang.String r2 = r2.exceptionLabel()
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                r0.<init>(r1)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.Recycler.tryGetViewHolderForPositionByDeadline(int, boolean, long):androidx.recyclerview.widget.RecyclerView$ViewHolder");
        }

        private void attachAccessibilityDelegateOnBind(ViewHolder viewHolder) {
            if (RecyclerView.this.isAccessibilityEnabled()) {
                View view = viewHolder.itemView;
                if (ViewCompat.getImportantForAccessibility(view) == 0) {
                    ViewCompat.setImportantForAccessibility(view, 1);
                }
                if (!ViewCompat.hasAccessibilityDelegate(view)) {
                    viewHolder.addFlags(16384);
                    ViewCompat.setAccessibilityDelegate(view, RecyclerView.this.mAccessibilityDelegate.getItemDelegate());
                }
            }
        }

        private void invalidateDisplayListInt(ViewHolder viewHolder) {
            if (viewHolder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt((ViewGroup) viewHolder.itemView, false);
            }
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean z) {
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                if (childAt instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) childAt, true);
                }
            }
            if (z) {
                if (viewGroup.getVisibility() == 4) {
                    viewGroup.setVisibility(0);
                    viewGroup.setVisibility(4);
                } else {
                    int visibility = viewGroup.getVisibility();
                    viewGroup.setVisibility(4);
                    viewGroup.setVisibility(visibility);
                }
            }
        }

        public void recycleView(@NonNull View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isTmpDetached()) {
                RecyclerView.this.removeDetachedView(view, false);
            }
            if (childViewHolderInt.isScrap()) {
                childViewHolderInt.unScrap();
            } else if (childViewHolderInt.wasReturnedFromScrap()) {
                childViewHolderInt.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(childViewHolderInt);
        }

        /* access modifiers changed from: 0000 */
        public void recycleViewInternal(View view) {
            recycleViewHolderInternal(RecyclerView.getChildViewHolderInt(view));
        }

        /* access modifiers changed from: 0000 */
        public void recycleAndClearCachedViews() {
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                recycleCachedViewAt(size);
            }
            this.mCachedViews.clear();
            if (RecyclerView.ALLOW_THREAD_GAP_WORK) {
                RecyclerView.this.mPrefetchRegistry.clearPrefetchPositions();
            }
        }

        /* access modifiers changed from: 0000 */
        public void recycleCachedViewAt(int i) {
            addViewHolderToRecycledViewPool((ViewHolder) this.mCachedViews.get(i), true);
            this.mCachedViews.remove(i);
        }

        /* access modifiers changed from: 0000 */
        public void recycleViewHolderInternal(ViewHolder viewHolder) {
            boolean z;
            boolean z2 = false;
            if (viewHolder.isScrap() || viewHolder.itemView.getParent() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Scrapped or attached views may not be recycled. isScrap:");
                sb.append(viewHolder.isScrap());
                sb.append(" isAttached:");
                if (viewHolder.itemView.getParent() != null) {
                    z2 = true;
                }
                sb.append(z2);
                sb.append(RecyclerView.this.exceptionLabel());
                throw new IllegalArgumentException(sb.toString());
            } else if (viewHolder.isTmpDetached()) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Tmp detached view should be removed from RecyclerView before it can be recycled: ");
                sb2.append(viewHolder);
                sb2.append(RecyclerView.this.exceptionLabel());
                throw new IllegalArgumentException(sb2.toString());
            } else if (!viewHolder.shouldIgnore()) {
                boolean doesTransientStatePreventRecycling = viewHolder.doesTransientStatePreventRecycling();
                if ((RecyclerView.this.mAdapter != null && doesTransientStatePreventRecycling && RecyclerView.this.mAdapter.onFailedToRecycleView(viewHolder)) || viewHolder.isRecyclable()) {
                    if (this.mViewCacheMax <= 0 || viewHolder.hasAnyOfTheFlags(526)) {
                        z = false;
                    } else {
                        int size = this.mCachedViews.size();
                        if (size >= this.mViewCacheMax && size > 0) {
                            recycleCachedViewAt(0);
                            size--;
                        }
                        if (RecyclerView.ALLOW_THREAD_GAP_WORK && size > 0 && !RecyclerView.this.mPrefetchRegistry.lastPrefetchIncludedPosition(viewHolder.mPosition)) {
                            int i = size - 1;
                            while (i >= 0) {
                                if (!RecyclerView.this.mPrefetchRegistry.lastPrefetchIncludedPosition(((ViewHolder) this.mCachedViews.get(i)).mPosition)) {
                                    break;
                                }
                                i--;
                            }
                            size = i + 1;
                        }
                        this.mCachedViews.add(size, viewHolder);
                        z = true;
                    }
                    if (!z) {
                        addViewHolderToRecycledViewPool(viewHolder, true);
                        z2 = true;
                    }
                } else {
                    z = false;
                }
                RecyclerView.this.mViewInfoStore.removeViewHolder(viewHolder);
                if (!z && !z2 && doesTransientStatePreventRecycling) {
                    viewHolder.mOwnerRecyclerView = null;
                }
            } else {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle.");
                sb3.append(RecyclerView.this.exceptionLabel());
                throw new IllegalArgumentException(sb3.toString());
            }
        }

        /* access modifiers changed from: 0000 */
        public void addViewHolderToRecycledViewPool(@NonNull ViewHolder viewHolder, boolean z) {
            RecyclerView.clearNestedRecyclerViewIfNotNested(viewHolder);
            if (viewHolder.hasAnyOfTheFlags(16384)) {
                viewHolder.setFlags(0, 16384);
                ViewCompat.setAccessibilityDelegate(viewHolder.itemView, null);
            }
            if (z) {
                dispatchViewRecycled(viewHolder);
            }
            viewHolder.mOwnerRecyclerView = null;
            getRecycledViewPool().putRecycledView(viewHolder);
        }

        /* access modifiers changed from: 0000 */
        public void quickRecycleScrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.mScrapContainer = null;
            childViewHolderInt.mInChangeScrap = false;
            childViewHolderInt.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(childViewHolderInt);
        }

        /* access modifiers changed from: 0000 */
        public void scrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (!childViewHolderInt.hasAnyOfTheFlags(12) && childViewHolderInt.isUpdated() && !RecyclerView.this.canReuseUpdatedViewHolder(childViewHolderInt)) {
                if (this.mChangedScrap == null) {
                    this.mChangedScrap = new ArrayList<>();
                }
                childViewHolderInt.setScrapContainer(this, true);
                this.mChangedScrap.add(childViewHolderInt);
            } else if (!childViewHolderInt.isInvalid() || childViewHolderInt.isRemoved() || RecyclerView.this.mAdapter.hasStableIds()) {
                childViewHolderInt.setScrapContainer(this, false);
                this.mAttachedScrap.add(childViewHolderInt);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool.");
                sb.append(RecyclerView.this.exceptionLabel());
                throw new IllegalArgumentException(sb.toString());
            }
        }

        /* access modifiers changed from: 0000 */
        public void unscrapView(ViewHolder viewHolder) {
            if (viewHolder.mInChangeScrap) {
                this.mChangedScrap.remove(viewHolder);
            } else {
                this.mAttachedScrap.remove(viewHolder);
            }
            viewHolder.mScrapContainer = null;
            viewHolder.mInChangeScrap = false;
            viewHolder.clearReturnedFromScrapFlag();
        }

        /* access modifiers changed from: 0000 */
        public int getScrapCount() {
            return this.mAttachedScrap.size();
        }

        /* access modifiers changed from: 0000 */
        public View getScrapViewAt(int i) {
            return ((ViewHolder) this.mAttachedScrap.get(i)).itemView;
        }

        /* access modifiers changed from: 0000 */
        public void clearScrap() {
            this.mAttachedScrap.clear();
            ArrayList<ViewHolder> arrayList = this.mChangedScrap;
            if (arrayList != null) {
                arrayList.clear();
            }
        }

        /* access modifiers changed from: 0000 */
        public ViewHolder getChangedScrapViewForPosition(int i) {
            ArrayList<ViewHolder> arrayList = this.mChangedScrap;
            if (arrayList != null) {
                int size = arrayList.size();
                if (size != 0) {
                    int i2 = 0;
                    int i3 = 0;
                    while (i3 < size) {
                        ViewHolder viewHolder = (ViewHolder) this.mChangedScrap.get(i3);
                        if (viewHolder.wasReturnedFromScrap() || viewHolder.getLayoutPosition() != i) {
                            i3++;
                        } else {
                            viewHolder.addFlags(32);
                            return viewHolder;
                        }
                    }
                    if (RecyclerView.this.mAdapter.hasStableIds()) {
                        int findPositionOffset = RecyclerView.this.mAdapterHelper.findPositionOffset(i);
                        if (findPositionOffset > 0 && findPositionOffset < RecyclerView.this.mAdapter.getItemCount()) {
                            long itemId = RecyclerView.this.mAdapter.getItemId(findPositionOffset);
                            while (i2 < size) {
                                ViewHolder viewHolder2 = (ViewHolder) this.mChangedScrap.get(i2);
                                if (viewHolder2.wasReturnedFromScrap() || viewHolder2.getItemId() != itemId) {
                                    i2++;
                                } else {
                                    viewHolder2.addFlags(32);
                                    return viewHolder2;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: 0000 */
        public ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int i, boolean z) {
            int size = this.mAttachedScrap.size();
            int i2 = 0;
            int i3 = 0;
            while (i3 < size) {
                ViewHolder viewHolder = (ViewHolder) this.mAttachedScrap.get(i3);
                if (viewHolder.wasReturnedFromScrap() || viewHolder.getLayoutPosition() != i || viewHolder.isInvalid() || (!RecyclerView.this.mState.mInPreLayout && viewHolder.isRemoved())) {
                    i3++;
                } else {
                    viewHolder.addFlags(32);
                    return viewHolder;
                }
            }
            if (!z) {
                View findHiddenNonRemovedView = RecyclerView.this.mChildHelper.findHiddenNonRemovedView(i);
                if (findHiddenNonRemovedView != null) {
                    ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(findHiddenNonRemovedView);
                    RecyclerView.this.mChildHelper.unhide(findHiddenNonRemovedView);
                    int indexOfChild = RecyclerView.this.mChildHelper.indexOfChild(findHiddenNonRemovedView);
                    if (indexOfChild != -1) {
                        RecyclerView.this.mChildHelper.detachViewFromParent(indexOfChild);
                        scrapView(findHiddenNonRemovedView);
                        childViewHolderInt.addFlags(8224);
                        return childViewHolderInt;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("layout index should not be -1 after unhiding a view:");
                    sb.append(childViewHolderInt);
                    sb.append(RecyclerView.this.exceptionLabel());
                    throw new IllegalStateException(sb.toString());
                }
            }
            int size2 = this.mCachedViews.size();
            while (i2 < size2) {
                ViewHolder viewHolder2 = (ViewHolder) this.mCachedViews.get(i2);
                if (viewHolder2.isInvalid() || viewHolder2.getLayoutPosition() != i) {
                    i2++;
                } else {
                    if (!z) {
                        this.mCachedViews.remove(i2);
                    }
                    return viewHolder2;
                }
            }
            return null;
        }

        /* access modifiers changed from: 0000 */
        public ViewHolder getScrapOrCachedViewForId(long j, int i, boolean z) {
            for (int size = this.mAttachedScrap.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = (ViewHolder) this.mAttachedScrap.get(size);
                if (viewHolder.getItemId() == j && !viewHolder.wasReturnedFromScrap()) {
                    if (i == viewHolder.getItemViewType()) {
                        viewHolder.addFlags(32);
                        if (viewHolder.isRemoved() && !RecyclerView.this.mState.isPreLayout()) {
                            viewHolder.setFlags(2, 14);
                        }
                        return viewHolder;
                    } else if (!z) {
                        this.mAttachedScrap.remove(size);
                        RecyclerView.this.removeDetachedView(viewHolder.itemView, false);
                        quickRecycleScrapView(viewHolder.itemView);
                    }
                }
            }
            int size2 = this.mCachedViews.size();
            while (true) {
                size2--;
                if (size2 < 0) {
                    return null;
                }
                ViewHolder viewHolder2 = (ViewHolder) this.mCachedViews.get(size2);
                if (viewHolder2.getItemId() == j) {
                    if (i == viewHolder2.getItemViewType()) {
                        if (!z) {
                            this.mCachedViews.remove(size2);
                        }
                        return viewHolder2;
                    } else if (!z) {
                        recycleCachedViewAt(size2);
                        return null;
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void dispatchViewRecycled(@NonNull ViewHolder viewHolder) {
            if (RecyclerView.this.mRecyclerListener != null) {
                RecyclerView.this.mRecyclerListener.onViewRecycled(viewHolder);
            }
            if (RecyclerView.this.mAdapter != null) {
                RecyclerView.this.mAdapter.onViewRecycled(viewHolder);
            }
            if (RecyclerView.this.mState != null) {
                RecyclerView.this.mViewInfoStore.removeViewHolder(viewHolder);
            }
        }

        /* access modifiers changed from: 0000 */
        public void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            clear();
            getRecycledViewPool().onAdapterChanged(adapter, adapter2, z);
        }

        /* access modifiers changed from: 0000 */
        public void offsetPositionRecordsForMove(int i, int i2) {
            int i3;
            int i4;
            int i5;
            if (i < i2) {
                i5 = i;
                i4 = i2;
                i3 = -1;
            } else {
                i4 = i;
                i5 = i2;
                i3 = 1;
            }
            int size = this.mCachedViews.size();
            for (int i6 = 0; i6 < size; i6++) {
                ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(i6);
                if (viewHolder != null && viewHolder.mPosition >= i5 && viewHolder.mPosition <= i4) {
                    if (viewHolder.mPosition == i) {
                        viewHolder.offsetPosition(i2 - i, false);
                    } else {
                        viewHolder.offsetPosition(i3, false);
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void offsetPositionRecordsForInsert(int i, int i2) {
            int size = this.mCachedViews.size();
            for (int i3 = 0; i3 < size; i3++) {
                ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(i3);
                if (viewHolder != null && viewHolder.mPosition >= i) {
                    viewHolder.offsetPosition(i2, true);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
            int i3 = i + i2;
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(size);
                if (viewHolder != null) {
                    if (viewHolder.mPosition >= i3) {
                        viewHolder.offsetPosition(-i2, z);
                    } else if (viewHolder.mPosition >= i) {
                        viewHolder.addFlags(8);
                        recycleCachedViewAt(size);
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void setViewCacheExtension(ViewCacheExtension viewCacheExtension) {
            this.mViewCacheExtension = viewCacheExtension;
        }

        /* access modifiers changed from: 0000 */
        public void setRecycledViewPool(RecycledViewPool recycledViewPool) {
            RecycledViewPool recycledViewPool2 = this.mRecyclerPool;
            if (recycledViewPool2 != null) {
                recycledViewPool2.detach();
            }
            this.mRecyclerPool = recycledViewPool;
            if (this.mRecyclerPool != null && RecyclerView.this.getAdapter() != null) {
                this.mRecyclerPool.attach();
            }
        }

        /* access modifiers changed from: 0000 */
        public RecycledViewPool getRecycledViewPool() {
            if (this.mRecyclerPool == null) {
                this.mRecyclerPool = new RecycledViewPool();
            }
            return this.mRecyclerPool;
        }

        /* access modifiers changed from: 0000 */
        public void viewRangeUpdate(int i, int i2) {
            int i3 = i2 + i;
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(size);
                if (viewHolder != null) {
                    int i4 = viewHolder.mPosition;
                    if (i4 >= i && i4 < i3) {
                        viewHolder.addFlags(2);
                        recycleCachedViewAt(size);
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void markKnownViewsInvalid() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(i);
                if (viewHolder != null) {
                    viewHolder.addFlags(6);
                    viewHolder.addChangePayload(null);
                }
            }
            if (RecyclerView.this.mAdapter == null || !RecyclerView.this.mAdapter.hasStableIds()) {
                recycleAndClearCachedViews();
            }
        }

        /* access modifiers changed from: 0000 */
        public void clearOldPositions() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ((ViewHolder) this.mCachedViews.get(i)).clearOldPosition();
            }
            int size2 = this.mAttachedScrap.size();
            for (int i2 = 0; i2 < size2; i2++) {
                ((ViewHolder) this.mAttachedScrap.get(i2)).clearOldPosition();
            }
            ArrayList<ViewHolder> arrayList = this.mChangedScrap;
            if (arrayList != null) {
                int size3 = arrayList.size();
                for (int i3 = 0; i3 < size3; i3++) {
                    ((ViewHolder) this.mChangedScrap.get(i3)).clearOldPosition();
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void markItemDecorInsetsDirty() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                LayoutParams layoutParams = (LayoutParams) ((ViewHolder) this.mCachedViews.get(i)).itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }
    }

    public interface RecyclerListener {
        void onViewRecycled(@NonNull ViewHolder viewHolder);
    }

    private class RecyclerViewDataObserver extends AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        public void onChanged() {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            RecyclerView.this.mState.mStructureChanged = true;
            RecyclerView.this.processDataSetCompletelyChanged(true);
            if (!RecyclerView.this.mAdapterHelper.hasPendingUpdates()) {
                RecyclerView.this.requestLayout();
            }
        }

        public void onItemRangeChanged(int i, int i2, Object obj) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeChanged(i, i2, obj)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeInserted(int i, int i2) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeInserted(i, i2)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeRemoved(int i, int i2) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeRemoved(i, i2)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeMoved(int i, int i2, int i3) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeMoved(i, i2, i3)) {
                triggerUpdateProcessor();
            }
        }

        /* access modifiers changed from: 0000 */
        public void triggerUpdateProcessor() {
            if (!RecyclerView.POST_UPDATES_ON_ANIMATION || !RecyclerView.this.mHasFixedSize || !RecyclerView.this.mIsAttached) {
                RecyclerView recyclerView = RecyclerView.this;
                recyclerView.mAdapterUpdateDuringMeasure = true;
                recyclerView.requestLayout();
                return;
            }
            RecyclerView recyclerView2 = RecyclerView.this;
            ViewCompat.postOnAnimation(recyclerView2, recyclerView2.mUpdateChildViewsRunnable);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static class SavedState extends AbsSavedState {
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, null);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        Parcelable mLayoutState;

        SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            if (classLoader == null) {
                classLoader = LayoutManager.class.getClassLoader();
            }
            this.mLayoutState = parcel.readParcelable(classLoader);
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeParcelable(this.mLayoutState, 0);
        }

        /* access modifiers changed from: 0000 */
        public void copyFrom(SavedState savedState) {
            this.mLayoutState = savedState.mLayoutState;
        }
    }

    public static class SimpleOnItemTouchListener implements OnItemTouchListener {
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            return false;
        }

        public void onRequestDisallowInterceptTouchEvent(boolean z) {
        }

        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        }
    }

    public static abstract class SmoothScroller {
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private RecyclerView mRecyclerView;
        private final Action mRecyclingAction = new Action(0, 0);
        private boolean mRunning;
        private boolean mStarted;
        private int mTargetPosition = -1;
        private View mTargetView;

        public static class Action {
            public static final int UNDEFINED_DURATION = Integer.MIN_VALUE;
            private boolean mChanged;
            private int mConsecutiveUpdates;
            private int mDuration;
            private int mDx;
            private int mDy;
            private Interpolator mInterpolator;
            private int mJumpToPosition;

            public Action(@Px int i, @Px int i2) {
                this(i, i2, Integer.MIN_VALUE, null);
            }

            public Action(@Px int i, @Px int i2, int i3) {
                this(i, i2, i3, null);
            }

            public Action(@Px int i, @Px int i2, int i3, @Nullable Interpolator interpolator) {
                this.mJumpToPosition = -1;
                this.mChanged = false;
                this.mConsecutiveUpdates = 0;
                this.mDx = i;
                this.mDy = i2;
                this.mDuration = i3;
                this.mInterpolator = interpolator;
            }

            public void jumpTo(int i) {
                this.mJumpToPosition = i;
            }

            /* access modifiers changed from: 0000 */
            public boolean hasJumpTarget() {
                return this.mJumpToPosition >= 0;
            }

            /* access modifiers changed from: 0000 */
            public void runIfNecessary(RecyclerView recyclerView) {
                int i = this.mJumpToPosition;
                if (i >= 0) {
                    this.mJumpToPosition = -1;
                    recyclerView.jumpToPositionForSmoothScroller(i);
                    this.mChanged = false;
                    return;
                }
                if (this.mChanged) {
                    validate();
                    if (this.mInterpolator != null) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration, this.mInterpolator);
                    } else if (this.mDuration == Integer.MIN_VALUE) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy);
                    } else {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration);
                    }
                    this.mConsecutiveUpdates++;
                    if (this.mConsecutiveUpdates > 10) {
                        Log.e(RecyclerView.TAG, "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                    }
                    this.mChanged = false;
                } else {
                    this.mConsecutiveUpdates = 0;
                }
            }

            private void validate() {
                if (this.mInterpolator != null && this.mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                } else if (this.mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            @Px
            public int getDx() {
                return this.mDx;
            }

            public void setDx(@Px int i) {
                this.mChanged = true;
                this.mDx = i;
            }

            @Px
            public int getDy() {
                return this.mDy;
            }

            public void setDy(@Px int i) {
                this.mChanged = true;
                this.mDy = i;
            }

            public int getDuration() {
                return this.mDuration;
            }

            public void setDuration(int i) {
                this.mChanged = true;
                this.mDuration = i;
            }

            @Nullable
            public Interpolator getInterpolator() {
                return this.mInterpolator;
            }

            public void setInterpolator(@Nullable Interpolator interpolator) {
                this.mChanged = true;
                this.mInterpolator = interpolator;
            }

            public void update(@Px int i, @Px int i2, int i3, @Nullable Interpolator interpolator) {
                this.mDx = i;
                this.mDy = i2;
                this.mDuration = i3;
                this.mInterpolator = interpolator;
                this.mChanged = true;
            }
        }

        public interface ScrollVectorProvider {
            @Nullable
            PointF computeScrollVectorForPosition(int i);
        }

        /* access modifiers changed from: protected */
        public abstract void onSeekTargetStep(@Px int i, @Px int i2, @NonNull State state, @NonNull Action action);

        /* access modifiers changed from: protected */
        public abstract void onStart();

        /* access modifiers changed from: protected */
        public abstract void onStop();

        /* access modifiers changed from: protected */
        public abstract void onTargetFound(@NonNull View view, @NonNull State state, @NonNull Action action);

        /* access modifiers changed from: 0000 */
        public void start(RecyclerView recyclerView, LayoutManager layoutManager) {
            if (this.mStarted) {
                StringBuilder sb = new StringBuilder();
                sb.append("An instance of ");
                sb.append(getClass().getSimpleName());
                sb.append(" was started ");
                sb.append("more than once. Each instance of");
                sb.append(getClass().getSimpleName());
                sb.append(" ");
                sb.append("is intended to only be used once. You should create a new instance for ");
                sb.append("each use.");
                Log.w(RecyclerView.TAG, sb.toString());
            }
            this.mRecyclerView = recyclerView;
            this.mLayoutManager = layoutManager;
            if (this.mTargetPosition != -1) {
                this.mRecyclerView.mState.mTargetPosition = this.mTargetPosition;
                this.mRunning = true;
                this.mPendingInitialRun = true;
                this.mTargetView = findViewByPosition(getTargetPosition());
                onStart();
                this.mRecyclerView.mViewFlinger.postOnAnimation();
                this.mStarted = true;
                return;
            }
            throw new IllegalArgumentException("Invalid target position");
        }

        public void setTargetPosition(int i) {
            this.mTargetPosition = i;
        }

        @Nullable
        public PointF computeScrollVectorForPosition(int i) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof ScrollVectorProvider) {
                return ((ScrollVectorProvider) layoutManager).computeScrollVectorForPosition(i);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("You should override computeScrollVectorForPosition when the LayoutManager does not implement ");
            sb.append(ScrollVectorProvider.class.getCanonicalName());
            Log.w(RecyclerView.TAG, sb.toString());
            return null;
        }

        @Nullable
        public LayoutManager getLayoutManager() {
            return this.mLayoutManager;
        }

        /* access modifiers changed from: protected */
        public final void stop() {
            if (this.mRunning) {
                this.mRunning = false;
                onStop();
                this.mRecyclerView.mState.mTargetPosition = -1;
                this.mTargetView = null;
                this.mTargetPosition = -1;
                this.mPendingInitialRun = false;
                this.mLayoutManager.onSmoothScrollerStopped(this);
                this.mLayoutManager = null;
                this.mRecyclerView = null;
            }
        }

        public boolean isPendingInitialRun() {
            return this.mPendingInitialRun;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        public int getTargetPosition() {
            return this.mTargetPosition;
        }

        /* access modifiers changed from: 0000 */
        public void onAnimation(int i, int i2) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (!this.mRunning || this.mTargetPosition == -1 || recyclerView == null) {
                stop();
            }
            if (this.mPendingInitialRun && this.mTargetView == null && this.mLayoutManager != null) {
                PointF computeScrollVectorForPosition = computeScrollVectorForPosition(this.mTargetPosition);
                if (!(computeScrollVectorForPosition == null || (computeScrollVectorForPosition.x == 0.0f && computeScrollVectorForPosition.y == 0.0f))) {
                    recyclerView.scrollStep((int) Math.signum(computeScrollVectorForPosition.x), (int) Math.signum(computeScrollVectorForPosition.y), null);
                }
            }
            this.mPendingInitialRun = false;
            View view = this.mTargetView;
            if (view != null) {
                if (getChildPosition(view) == this.mTargetPosition) {
                    onTargetFound(this.mTargetView, recyclerView.mState, this.mRecyclingAction);
                    this.mRecyclingAction.runIfNecessary(recyclerView);
                    stop();
                } else {
                    Log.e(RecyclerView.TAG, "Passed over target position while smooth scrolling.");
                    this.mTargetView = null;
                }
            }
            if (this.mRunning) {
                onSeekTargetStep(i, i2, recyclerView.mState, this.mRecyclingAction);
                boolean hasJumpTarget = this.mRecyclingAction.hasJumpTarget();
                this.mRecyclingAction.runIfNecessary(recyclerView);
                if (!hasJumpTarget) {
                    return;
                }
                if (this.mRunning) {
                    this.mPendingInitialRun = true;
                    recyclerView.mViewFlinger.postOnAnimation();
                    return;
                }
                stop();
            }
        }

        public int getChildPosition(View view) {
            return this.mRecyclerView.getChildLayoutPosition(view);
        }

        public int getChildCount() {
            return this.mRecyclerView.mLayout.getChildCount();
        }

        public View findViewByPosition(int i) {
            return this.mRecyclerView.mLayout.findViewByPosition(i);
        }

        @Deprecated
        public void instantScrollToPosition(int i) {
            this.mRecyclerView.scrollToPosition(i);
        }

        /* access modifiers changed from: protected */
        public void onChildAttachedToWindow(View view) {
            if (getChildPosition(view) == getTargetPosition()) {
                this.mTargetView = view;
            }
        }

        /* access modifiers changed from: protected */
        public void normalize(@NonNull PointF pointF) {
            float sqrt = (float) Math.sqrt((double) ((pointF.x * pointF.x) + (pointF.y * pointF.y)));
            pointF.x /= sqrt;
            pointF.y /= sqrt;
        }
    }

    public static class State {
        static final int STEP_ANIMATIONS = 4;
        static final int STEP_LAYOUT = 2;
        static final int STEP_START = 1;
        private SparseArray<Object> mData;
        int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        long mFocusedItemId;
        int mFocusedItemPosition;
        int mFocusedSubChildId;
        boolean mInPreLayout = false;
        boolean mIsMeasuring = false;
        int mItemCount = 0;
        int mLayoutStep = 1;
        int mPreviousLayoutItemCount = 0;
        int mRemainingScrollHorizontal;
        int mRemainingScrollVertical;
        boolean mRunPredictiveAnimations = false;
        boolean mRunSimpleAnimations = false;
        boolean mStructureChanged = false;
        int mTargetPosition = -1;
        boolean mTrackOldChangeHolders = false;

        /* access modifiers changed from: 0000 */
        public void assertLayoutStep(int i) {
            if ((this.mLayoutStep & i) == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Layout state should be one of ");
                sb.append(Integer.toBinaryString(i));
                sb.append(" but it is ");
                sb.append(Integer.toBinaryString(this.mLayoutStep));
                throw new IllegalStateException(sb.toString());
            }
        }

        /* access modifiers changed from: 0000 */
        public State reset() {
            this.mTargetPosition = -1;
            SparseArray<Object> sparseArray = this.mData;
            if (sparseArray != null) {
                sparseArray.clear();
            }
            this.mItemCount = 0;
            this.mStructureChanged = false;
            this.mIsMeasuring = false;
            return this;
        }

        /* access modifiers changed from: 0000 */
        public void prepareForNestedPrefetch(Adapter adapter) {
            this.mLayoutStep = 1;
            this.mItemCount = adapter.getItemCount();
            this.mInPreLayout = false;
            this.mTrackOldChangeHolders = false;
            this.mIsMeasuring = false;
        }

        public boolean isMeasuring() {
            return this.mIsMeasuring;
        }

        public boolean isPreLayout() {
            return this.mInPreLayout;
        }

        public boolean willRunPredictiveAnimations() {
            return this.mRunPredictiveAnimations;
        }

        public boolean willRunSimpleAnimations() {
            return this.mRunSimpleAnimations;
        }

        public void remove(int i) {
            SparseArray<Object> sparseArray = this.mData;
            if (sparseArray != null) {
                sparseArray.remove(i);
            }
        }

        public <T> T get(int i) {
            SparseArray<Object> sparseArray = this.mData;
            if (sparseArray == null) {
                return null;
            }
            return sparseArray.get(i);
        }

        public void put(int i, Object obj) {
            if (this.mData == null) {
                this.mData = new SparseArray<>();
            }
            this.mData.put(i, obj);
        }

        public int getTargetScrollPosition() {
            return this.mTargetPosition;
        }

        public boolean hasTargetScrollPosition() {
            return this.mTargetPosition != -1;
        }

        public boolean didStructureChange() {
            return this.mStructureChanged;
        }

        public int getItemCount() {
            return this.mInPreLayout ? this.mPreviousLayoutItemCount - this.mDeletedInvisibleItemCountSincePreviousLayout : this.mItemCount;
        }

        public int getRemainingScrollHorizontal() {
            return this.mRemainingScrollHorizontal;
        }

        public int getRemainingScrollVertical() {
            return this.mRemainingScrollVertical;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("State{mTargetPosition=");
            sb.append(this.mTargetPosition);
            sb.append(", mData=");
            sb.append(this.mData);
            sb.append(", mItemCount=");
            sb.append(this.mItemCount);
            sb.append(", mIsMeasuring=");
            sb.append(this.mIsMeasuring);
            sb.append(", mPreviousLayoutItemCount=");
            sb.append(this.mPreviousLayoutItemCount);
            sb.append(", mDeletedInvisibleItemCountSincePreviousLayout=");
            sb.append(this.mDeletedInvisibleItemCountSincePreviousLayout);
            sb.append(", mStructureChanged=");
            sb.append(this.mStructureChanged);
            sb.append(", mInPreLayout=");
            sb.append(this.mInPreLayout);
            sb.append(", mRunSimpleAnimations=");
            sb.append(this.mRunSimpleAnimations);
            sb.append(", mRunPredictiveAnimations=");
            sb.append(this.mRunPredictiveAnimations);
            sb.append('}');
            return sb.toString();
        }
    }

    public static abstract class ViewCacheExtension {
        @Nullable
        public abstract View getViewForPositionAndType(@NonNull Recycler recycler, int i, int i2);
    }

    class ViewFlinger implements Runnable {
        private boolean mEatRunOnAnimationRequest = false;
        Interpolator mInterpolator = RecyclerView.sQuinticInterpolator;
        private int mLastFlingX;
        private int mLastFlingY;
        private boolean mReSchedulePostAnimationCallback = false;
        OverScroller mScroller;

        ViewFlinger() {
            this.mScroller = new OverScroller(RecyclerView.this.getContext(), RecyclerView.sQuinticInterpolator);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f1, code lost:
            if (r8 > 0) goto L_0x00f5;
         */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00ed  */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x00fd  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r22 = this;
                r0 = r22
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$LayoutManager r1 = r1.mLayout
                if (r1 != 0) goto L_0x000c
                r22.stop()
                return
            L_0x000c:
                r22.disableRunOnAnimationRequests()
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                r1.consumePendingUpdateOperations()
                android.widget.OverScroller r1 = r0.mScroller
                androidx.recyclerview.widget.RecyclerView r2 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$LayoutManager r2 = r2.mLayout
                androidx.recyclerview.widget.RecyclerView$SmoothScroller r2 = r2.mSmoothScroller
                boolean r3 = r1.computeScrollOffset()
                r4 = 0
                if (r3 == 0) goto L_0x0197
                androidx.recyclerview.widget.RecyclerView r3 = androidx.recyclerview.widget.RecyclerView.this
                int[] r3 = r3.mScrollConsumed
                int r11 = r1.getCurrX()
                int r12 = r1.getCurrY()
                int r5 = r0.mLastFlingX
                int r13 = r11 - r5
                int r5 = r0.mLastFlingY
                int r14 = r12 - r5
                r0.mLastFlingX = r11
                r0.mLastFlingY = r12
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                r9 = 0
                r10 = 1
                r6 = r13
                r7 = r14
                r8 = r3
                boolean r5 = r5.dispatchNestedPreScroll(r6, r7, r8, r9, r10)
                r6 = 1
                if (r5 == 0) goto L_0x004f
                r5 = r3[r4]
                int r13 = r13 - r5
                r3 = r3[r6]
                int r14 = r14 - r3
            L_0x004f:
                androidx.recyclerview.widget.RecyclerView r3 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$Adapter r3 = r3.mAdapter
                if (r3 == 0) goto L_0x00a2
                androidx.recyclerview.widget.RecyclerView r3 = androidx.recyclerview.widget.RecyclerView.this
                int[] r5 = r3.mScrollStepConsumed
                r3.scrollStep(r13, r14, r5)
                androidx.recyclerview.widget.RecyclerView r3 = androidx.recyclerview.widget.RecyclerView.this
                int[] r3 = r3.mScrollStepConsumed
                r3 = r3[r4]
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                int[] r5 = r5.mScrollStepConsumed
                r5 = r5[r6]
                int r7 = r13 - r3
                int r8 = r14 - r5
                if (r2 == 0) goto L_0x00a6
                boolean r9 = r2.isPendingInitialRun()
                if (r9 != 0) goto L_0x00a6
                boolean r9 = r2.isRunning()
                if (r9 == 0) goto L_0x00a6
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$State r9 = r9.mState
                int r9 = r9.getItemCount()
                if (r9 != 0) goto L_0x0088
                r2.stop()
                goto L_0x00a6
            L_0x0088:
                int r10 = r2.getTargetPosition()
                if (r10 < r9) goto L_0x009a
                int r9 = r9 - r6
                r2.setTargetPosition(r9)
                int r9 = r13 - r7
                int r10 = r14 - r8
                r2.onAnimation(r9, r10)
                goto L_0x00a6
            L_0x009a:
                int r9 = r13 - r7
                int r10 = r14 - r8
                r2.onAnimation(r9, r10)
                goto L_0x00a6
            L_0x00a2:
                r3 = 0
                r5 = 0
                r7 = 0
                r8 = 0
            L_0x00a6:
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                java.util.ArrayList<androidx.recyclerview.widget.RecyclerView$ItemDecoration> r9 = r9.mItemDecorations
                boolean r9 = r9.isEmpty()
                if (r9 != 0) goto L_0x00b5
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                r9.invalidate()
            L_0x00b5:
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                int r9 = r9.getOverScrollMode()
                r10 = 2
                if (r9 == r10) goto L_0x00c3
                androidx.recyclerview.widget.RecyclerView r9 = androidx.recyclerview.widget.RecyclerView.this
                r9.considerReleasingGlowsOnScroll(r13, r14)
            L_0x00c3:
                androidx.recyclerview.widget.RecyclerView r15 = androidx.recyclerview.widget.RecyclerView.this
                r20 = 0
                r21 = 1
                r16 = r3
                r17 = r5
                r18 = r7
                r19 = r8
                boolean r9 = r15.dispatchNestedScroll(r16, r17, r18, r19, r20, r21)
                if (r9 != 0) goto L_0x0119
                if (r7 != 0) goto L_0x00db
                if (r8 == 0) goto L_0x0119
            L_0x00db:
                float r9 = r1.getCurrVelocity()
                int r9 = (int) r9
                if (r7 == r11) goto L_0x00ea
                if (r7 >= 0) goto L_0x00e6
                int r15 = -r9
                goto L_0x00eb
            L_0x00e6:
                if (r7 <= 0) goto L_0x00ea
                r15 = r9
                goto L_0x00eb
            L_0x00ea:
                r15 = 0
            L_0x00eb:
                if (r8 == r12) goto L_0x00f4
                if (r8 >= 0) goto L_0x00f1
                int r9 = -r9
                goto L_0x00f5
            L_0x00f1:
                if (r8 <= 0) goto L_0x00f4
                goto L_0x00f5
            L_0x00f4:
                r9 = 0
            L_0x00f5:
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                int r4 = r4.getOverScrollMode()
                if (r4 == r10) goto L_0x0102
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                r4.absorbGlows(r15, r9)
            L_0x0102:
                if (r15 != 0) goto L_0x010c
                if (r7 == r11) goto L_0x010c
                int r4 = r1.getFinalX()
                if (r4 != 0) goto L_0x0119
            L_0x010c:
                if (r9 != 0) goto L_0x0116
                if (r8 == r12) goto L_0x0116
                int r4 = r1.getFinalY()
                if (r4 != 0) goto L_0x0119
            L_0x0116:
                r1.abortAnimation()
            L_0x0119:
                if (r3 != 0) goto L_0x011d
                if (r5 == 0) goto L_0x0122
            L_0x011d:
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                r4.dispatchOnScrolled(r3, r5)
            L_0x0122:
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                boolean r4 = r4.awakenScrollBars()
                if (r4 != 0) goto L_0x012f
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                r4.invalidate()
            L_0x012f:
                if (r14 == 0) goto L_0x013f
                androidx.recyclerview.widget.RecyclerView r4 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$LayoutManager r4 = r4.mLayout
                boolean r4 = r4.canScrollVertically()
                if (r4 == 0) goto L_0x013f
                if (r5 != r14) goto L_0x013f
                r4 = 1
                goto L_0x0140
            L_0x013f:
                r4 = 0
            L_0x0140:
                if (r13 == 0) goto L_0x0150
                androidx.recyclerview.widget.RecyclerView r5 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.RecyclerView$LayoutManager r5 = r5.mLayout
                boolean r5 = r5.canScrollHorizontally()
                if (r5 == 0) goto L_0x0150
                if (r3 != r13) goto L_0x0150
                r3 = 1
                goto L_0x0151
            L_0x0150:
                r3 = 0
            L_0x0151:
                if (r13 != 0) goto L_0x0155
                if (r14 == 0) goto L_0x015c
            L_0x0155:
                if (r3 != 0) goto L_0x015c
                if (r4 == 0) goto L_0x015a
                goto L_0x015c
            L_0x015a:
                r3 = 0
                goto L_0x015d
            L_0x015c:
                r3 = 1
            L_0x015d:
                boolean r1 = r1.isFinished()
                if (r1 != 0) goto L_0x0181
                if (r3 != 0) goto L_0x016e
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                boolean r1 = r1.hasNestedScrollingParent(r6)
                if (r1 != 0) goto L_0x016e
                goto L_0x0181
            L_0x016e:
                r22.postOnAnimation()
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.GapWorker r1 = r1.mGapWorker
                if (r1 == 0) goto L_0x0197
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.GapWorker r1 = r1.mGapWorker
                androidx.recyclerview.widget.RecyclerView r3 = androidx.recyclerview.widget.RecyclerView.this
                r1.postFromTraversal(r3, r13, r14)
                goto L_0x0197
            L_0x0181:
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                r3 = 0
                r1.setScrollState(r3)
                boolean r1 = androidx.recyclerview.widget.RecyclerView.ALLOW_THREAD_GAP_WORK
                if (r1 == 0) goto L_0x0192
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                androidx.recyclerview.widget.GapWorker$LayoutPrefetchRegistryImpl r1 = r1.mPrefetchRegistry
                r1.clearPrefetchPositions()
            L_0x0192:
                androidx.recyclerview.widget.RecyclerView r1 = androidx.recyclerview.widget.RecyclerView.this
                r1.stopNestedScroll(r6)
            L_0x0197:
                if (r2 == 0) goto L_0x01aa
                boolean r1 = r2.isPendingInitialRun()
                if (r1 == 0) goto L_0x01a3
                r1 = 0
                r2.onAnimation(r1, r1)
            L_0x01a3:
                boolean r1 = r0.mReSchedulePostAnimationCallback
                if (r1 != 0) goto L_0x01aa
                r2.stop()
            L_0x01aa:
                r22.enableRunOnAnimationRequests()
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.ViewFlinger.run():void");
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        /* access modifiers changed from: 0000 */
        public void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
                return;
            }
            RecyclerView.this.removeCallbacks(this);
            ViewCompat.postOnAnimation(RecyclerView.this, this);
        }

        public void fling(int i, int i2) {
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, i, i2, Integer.MIN_VALUE, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, Integer.MIN_VALUE, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
            postOnAnimation();
        }

        public void smoothScrollBy(int i, int i2) {
            smoothScrollBy(i, i2, 0, 0);
        }

        public void smoothScrollBy(int i, int i2, int i3, int i4) {
            smoothScrollBy(i, i2, computeScrollDuration(i, i2, i3, i4));
        }

        private float distanceInfluenceForSnapDuration(float f) {
            return (float) Math.sin((double) ((f - 0.5f) * 0.47123894f));
        }

        private int computeScrollDuration(int i, int i2, int i3, int i4) {
            int i5;
            int abs = Math.abs(i);
            int abs2 = Math.abs(i2);
            boolean z = abs > abs2;
            int sqrt = (int) Math.sqrt((double) ((i3 * i3) + (i4 * i4)));
            int sqrt2 = (int) Math.sqrt((double) ((i * i) + (i2 * i2)));
            int width = z ? RecyclerView.this.getWidth() : RecyclerView.this.getHeight();
            int i6 = width / 2;
            float f = (float) width;
            float f2 = (float) i6;
            float distanceInfluenceForSnapDuration = f2 + (distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) sqrt2) * 1.0f) / f)) * f2);
            if (sqrt > 0) {
                i5 = Math.round(Math.abs(distanceInfluenceForSnapDuration / ((float) sqrt)) * 1000.0f) * 4;
            } else {
                if (!z) {
                    abs = abs2;
                }
                i5 = (int) (((((float) abs) / f) + 1.0f) * 300.0f);
            }
            return Math.min(i5, RecyclerView.MAX_SCROLL_DURATION);
        }

        public void smoothScrollBy(int i, int i2, int i3) {
            smoothScrollBy(i, i2, i3, RecyclerView.sQuinticInterpolator);
        }

        public void smoothScrollBy(int i, int i2, Interpolator interpolator) {
            int computeScrollDuration = computeScrollDuration(i, i2, 0, 0);
            if (interpolator == null) {
                interpolator = RecyclerView.sQuinticInterpolator;
            }
            smoothScrollBy(i, i2, computeScrollDuration, interpolator);
        }

        public void smoothScrollBy(int i, int i2, int i3, Interpolator interpolator) {
            if (this.mInterpolator != interpolator) {
                this.mInterpolator = interpolator;
                this.mScroller = new OverScroller(RecyclerView.this.getContext(), interpolator);
            }
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.startScroll(0, 0, i, i2, i3);
            if (VERSION.SDK_INT < 23) {
                this.mScroller.computeScrollOffset();
            }
            postOnAnimation();
        }

        public void stop() {
            RecyclerView.this.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }

    public static abstract class ViewHolder {
        static final int FLAG_ADAPTER_FULLUPDATE = 1024;
        static final int FLAG_ADAPTER_POSITION_UNKNOWN = 512;
        static final int FLAG_APPEARED_IN_PRE_LAYOUT = 4096;
        static final int FLAG_BOUNCED_FROM_HIDDEN_LIST = 8192;
        static final int FLAG_BOUND = 1;
        static final int FLAG_IGNORE = 128;
        static final int FLAG_INVALID = 4;
        static final int FLAG_MOVED = 2048;
        static final int FLAG_NOT_RECYCLABLE = 16;
        static final int FLAG_REMOVED = 8;
        static final int FLAG_RETURNED_FROM_SCRAP = 32;
        static final int FLAG_SET_A11Y_ITEM_DELEGATE = 16384;
        static final int FLAG_TMP_DETACHED = 256;
        static final int FLAG_UPDATE = 2;
        private static final List<Object> FULLUPDATE_PAYLOADS = Collections.emptyList();
        static final int PENDING_ACCESSIBILITY_STATE_NOT_SET = -1;
        @NonNull
        public final View itemView;
        int mFlags;
        boolean mInChangeScrap = false;
        private int mIsRecyclableCount = 0;
        long mItemId = -1;
        int mItemViewType = -1;
        WeakReference<RecyclerView> mNestedRecyclerView;
        int mOldPosition = -1;
        RecyclerView mOwnerRecyclerView;
        List<Object> mPayloads = null;
        @VisibleForTesting
        int mPendingAccessibilityState = -1;
        int mPosition = -1;
        int mPreLayoutPosition = -1;
        Recycler mScrapContainer = null;
        ViewHolder mShadowedHolder = null;
        ViewHolder mShadowingHolder = null;
        List<Object> mUnmodifiedPayloads = null;
        private int mWasImportantForAccessibilityBeforeHidden = 0;

        public ViewHolder(@NonNull View view) {
            if (view != null) {
                this.itemView = view;
                return;
            }
            throw new IllegalArgumentException("itemView may not be null");
        }

        /* access modifiers changed from: 0000 */
        public void flagRemovedAndOffsetPosition(int i, int i2, boolean z) {
            addFlags(8);
            offsetPosition(i2, z);
            this.mPosition = i;
        }

        /* access modifiers changed from: 0000 */
        public void offsetPosition(int i, boolean z) {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
            if (this.mPreLayoutPosition == -1) {
                this.mPreLayoutPosition = this.mPosition;
            }
            if (z) {
                this.mPreLayoutPosition += i;
            }
            this.mPosition += i;
            if (this.itemView.getLayoutParams() != null) {
                ((LayoutParams) this.itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        /* access modifiers changed from: 0000 */
        public void clearOldPosition() {
            this.mOldPosition = -1;
            this.mPreLayoutPosition = -1;
        }

        /* access modifiers changed from: 0000 */
        public void saveOldPosition() {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean shouldIgnore() {
            return (this.mFlags & 128) != 0;
        }

        @Deprecated
        public final int getPosition() {
            int i = this.mPreLayoutPosition;
            return i == -1 ? this.mPosition : i;
        }

        public final int getLayoutPosition() {
            int i = this.mPreLayoutPosition;
            return i == -1 ? this.mPosition : i;
        }

        public final int getAdapterPosition() {
            RecyclerView recyclerView = this.mOwnerRecyclerView;
            if (recyclerView == null) {
                return -1;
            }
            return recyclerView.getAdapterPositionFor(this);
        }

        public final int getOldPosition() {
            return this.mOldPosition;
        }

        public final long getItemId() {
            return this.mItemId;
        }

        public final int getItemViewType() {
            return this.mItemViewType;
        }

        /* access modifiers changed from: 0000 */
        public boolean isScrap() {
            return this.mScrapContainer != null;
        }

        /* access modifiers changed from: 0000 */
        public void unScrap() {
            this.mScrapContainer.unscrapView(this);
        }

        /* access modifiers changed from: 0000 */
        public boolean wasReturnedFromScrap() {
            return (this.mFlags & 32) != 0;
        }

        /* access modifiers changed from: 0000 */
        public void clearReturnedFromScrapFlag() {
            this.mFlags &= -33;
        }

        /* access modifiers changed from: 0000 */
        public void clearTmpDetachFlag() {
            this.mFlags &= -257;
        }

        /* access modifiers changed from: 0000 */
        public void stopIgnoring() {
            this.mFlags &= -129;
        }

        /* access modifiers changed from: 0000 */
        public void setScrapContainer(Recycler recycler, boolean z) {
            this.mScrapContainer = recycler;
            this.mInChangeScrap = z;
        }

        /* access modifiers changed from: 0000 */
        public boolean isInvalid() {
            return (this.mFlags & 4) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean needsUpdate() {
            return (this.mFlags & 2) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean isBound() {
            return (this.mFlags & 1) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean isRemoved() {
            return (this.mFlags & 8) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean hasAnyOfTheFlags(int i) {
            return (i & this.mFlags) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean isTmpDetached() {
            return (this.mFlags & 256) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean isAdapterPositionUnknown() {
            return (this.mFlags & 512) != 0 || isInvalid();
        }

        /* access modifiers changed from: 0000 */
        public void setFlags(int i, int i2) {
            this.mFlags = (i & i2) | (this.mFlags & (~i2));
        }

        /* access modifiers changed from: 0000 */
        public void addFlags(int i) {
            this.mFlags = i | this.mFlags;
        }

        /* access modifiers changed from: 0000 */
        public void addChangePayload(Object obj) {
            if (obj == null) {
                addFlags(1024);
            } else if ((1024 & this.mFlags) == 0) {
                createPayloadsIfNeeded();
                this.mPayloads.add(obj);
            }
        }

        private void createPayloadsIfNeeded() {
            if (this.mPayloads == null) {
                this.mPayloads = new ArrayList();
                this.mUnmodifiedPayloads = Collections.unmodifiableList(this.mPayloads);
            }
        }

        /* access modifiers changed from: 0000 */
        public void clearPayload() {
            List<Object> list = this.mPayloads;
            if (list != null) {
                list.clear();
            }
            this.mFlags &= -1025;
        }

        /* access modifiers changed from: 0000 */
        public List<Object> getUnmodifiedPayloads() {
            if ((this.mFlags & 1024) != 0) {
                return FULLUPDATE_PAYLOADS;
            }
            List<Object> list = this.mPayloads;
            if (list == null || list.size() == 0) {
                return FULLUPDATE_PAYLOADS;
            }
            return this.mUnmodifiedPayloads;
        }

        /* access modifiers changed from: 0000 */
        public void resetInternal() {
            this.mFlags = 0;
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1;
            this.mPreLayoutPosition = -1;
            this.mIsRecyclableCount = 0;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
            clearPayload();
            this.mWasImportantForAccessibilityBeforeHidden = 0;
            this.mPendingAccessibilityState = -1;
            RecyclerView.clearNestedRecyclerViewIfNotNested(this);
        }

        /* access modifiers changed from: 0000 */
        public void onEnteredHiddenState(RecyclerView recyclerView) {
            int i = this.mPendingAccessibilityState;
            if (i != -1) {
                this.mWasImportantForAccessibilityBeforeHidden = i;
            } else {
                this.mWasImportantForAccessibilityBeforeHidden = ViewCompat.getImportantForAccessibility(this.itemView);
            }
            recyclerView.setChildImportantForAccessibilityInternal(this, 4);
        }

        /* access modifiers changed from: 0000 */
        public void onLeftHiddenState(RecyclerView recyclerView) {
            recyclerView.setChildImportantForAccessibilityInternal(this, this.mWasImportantForAccessibilityBeforeHidden);
            this.mWasImportantForAccessibilityBeforeHidden = 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ViewHolder{");
            sb.append(Integer.toHexString(hashCode()));
            sb.append(" position=");
            sb.append(this.mPosition);
            sb.append(" id=");
            sb.append(this.mItemId);
            sb.append(", oldPos=");
            sb.append(this.mOldPosition);
            sb.append(", pLpos:");
            sb.append(this.mPreLayoutPosition);
            StringBuilder sb2 = new StringBuilder(sb.toString());
            if (isScrap()) {
                sb2.append(" scrap ");
                sb2.append(this.mInChangeScrap ? "[changeScrap]" : "[attachedScrap]");
            }
            if (isInvalid()) {
                sb2.append(" invalid");
            }
            if (!isBound()) {
                sb2.append(" unbound");
            }
            if (needsUpdate()) {
                sb2.append(" update");
            }
            if (isRemoved()) {
                sb2.append(" removed");
            }
            if (shouldIgnore()) {
                sb2.append(" ignored");
            }
            if (isTmpDetached()) {
                sb2.append(" tmpDetached");
            }
            if (!isRecyclable()) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append(" not recyclable(");
                sb3.append(this.mIsRecyclableCount);
                sb3.append(")");
                sb2.append(sb3.toString());
            }
            if (isAdapterPositionUnknown()) {
                sb2.append(" undefined adapter position");
            }
            if (this.itemView.getParent() == null) {
                sb2.append(" no parent");
            }
            sb2.append("}");
            return sb2.toString();
        }

        public final void setIsRecyclable(boolean z) {
            this.mIsRecyclableCount = z ? this.mIsRecyclableCount - 1 : this.mIsRecyclableCount + 1;
            int i = this.mIsRecyclableCount;
            if (i < 0) {
                this.mIsRecyclableCount = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for ");
                sb.append(this);
                Log.e("View", sb.toString());
            } else if (!z && i == 1) {
                this.mFlags |= 16;
            } else if (z && this.mIsRecyclableCount == 0) {
                this.mFlags &= -17;
            }
        }

        public final boolean isRecyclable() {
            return (this.mFlags & 16) == 0 && !ViewCompat.hasTransientState(this.itemView);
        }

        /* access modifiers changed from: 0000 */
        public boolean shouldBeKeptAsChild() {
            return (this.mFlags & 16) != 0;
        }

        /* access modifiers changed from: 0000 */
        public boolean doesTransientStatePreventRecycling() {
            return (this.mFlags & 16) == 0 && ViewCompat.hasTransientState(this.itemView);
        }

        /* access modifiers changed from: 0000 */
        public boolean isUpdated() {
            return (this.mFlags & 2) != 0;
        }
    }

    public void onChildAttachedToWindow(@NonNull View view) {
    }

    public void onChildDetachedFromWindow(@NonNull View view) {
    }

    public void onScrollStateChanged(int i) {
    }

    public void onScrolled(@Px int i, @Px int i2) {
    }

    public RecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mObserver = new RecyclerViewDataObserver();
        this.mRecycler = new Recycler();
        this.mViewInfoStore = new ViewInfoStore();
        this.mUpdateChildViewsRunnable = new Runnable() {
            public void run() {
                if (RecyclerView.this.mFirstLayoutComplete && !RecyclerView.this.isLayoutRequested()) {
                    if (!RecyclerView.this.mIsAttached) {
                        RecyclerView.this.requestLayout();
                    } else if (RecyclerView.this.mLayoutFrozen) {
                        RecyclerView.this.mLayoutWasDefered = true;
                    } else {
                        RecyclerView.this.consumePendingUpdateOperations();
                    }
                }
            }
        };
        this.mTempRect = new Rect();
        this.mTempRect2 = new Rect();
        this.mTempRectF = new RectF();
        this.mItemDecorations = new ArrayList<>();
        this.mOnItemTouchListeners = new ArrayList<>();
        this.mInterceptRequestLayoutDepth = 0;
        this.mDataSetHasChangedAfterLayout = false;
        this.mDispatchItemsChangedEvent = false;
        this.mLayoutOrScrollCounter = 0;
        this.mDispatchScrollCounter = 0;
        this.mEdgeEffectFactory = new EdgeEffectFactory();
        this.mItemAnimator = new DefaultItemAnimator();
        this.mScrollState = 0;
        this.mScrollPointerId = -1;
        this.mScaledHorizontalScrollFactor = Float.MIN_VALUE;
        this.mScaledVerticalScrollFactor = Float.MIN_VALUE;
        boolean z = true;
        this.mPreserveFocusAfterLayout = true;
        this.mViewFlinger = new ViewFlinger();
        this.mPrefetchRegistry = ALLOW_THREAD_GAP_WORK ? new LayoutPrefetchRegistryImpl() : null;
        this.mState = new State();
        this.mItemsAddedOrRemoved = false;
        this.mItemsChanged = false;
        this.mItemAnimatorListener = new ItemAnimatorRestoreListener();
        this.mPostedAnimatorRunner = false;
        this.mMinMaxLayoutPositions = new int[2];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mNestedOffsets = new int[2];
        this.mScrollStepConsumed = new int[2];
        this.mPendingAccessibilityImportanceChange = new ArrayList();
        this.mItemAnimatorRunner = new Runnable() {
            public void run() {
                if (RecyclerView.this.mItemAnimator != null) {
                    RecyclerView.this.mItemAnimator.runPendingAnimations();
                }
                RecyclerView.this.mPostedAnimatorRunner = false;
            }
        };
        this.mViewInfoProcessCallback = new ProcessCallback() {
            public void processDisappeared(ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @Nullable ItemHolderInfo itemHolderInfo2) {
                RecyclerView.this.mRecycler.unscrapView(viewHolder);
                RecyclerView.this.animateDisappearance(viewHolder, itemHolderInfo, itemHolderInfo2);
            }

            public void processAppeared(ViewHolder viewHolder, ItemHolderInfo itemHolderInfo, ItemHolderInfo itemHolderInfo2) {
                RecyclerView.this.animateAppearance(viewHolder, itemHolderInfo, itemHolderInfo2);
            }

            public void processPersistent(ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2) {
                viewHolder.setIsRecyclable(false);
                if (RecyclerView.this.mDataSetHasChangedAfterLayout) {
                    if (RecyclerView.this.mItemAnimator.animateChange(viewHolder, viewHolder, itemHolderInfo, itemHolderInfo2)) {
                        RecyclerView.this.postAnimationRunner();
                    }
                } else if (RecyclerView.this.mItemAnimator.animatePersistence(viewHolder, itemHolderInfo, itemHolderInfo2)) {
                    RecyclerView.this.postAnimationRunner();
                }
            }

            public void unused(ViewHolder viewHolder) {
                RecyclerView.this.mLayout.removeAndRecycleView(viewHolder.itemView, RecyclerView.this.mRecycler);
            }
        };
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, CLIP_TO_PADDING_ATTR, i, 0);
            this.mClipToPadding = obtainStyledAttributes.getBoolean(0, true);
            obtainStyledAttributes.recycle();
        } else {
            this.mClipToPadding = true;
        }
        setScrollContainer(true);
        setFocusableInTouchMode(true);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mScaledHorizontalScrollFactor = ViewConfigurationCompat.getScaledHorizontalScrollFactor(viewConfiguration, context);
        this.mScaledVerticalScrollFactor = ViewConfigurationCompat.getScaledVerticalScrollFactor(viewConfiguration, context);
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        setWillNotDraw(getOverScrollMode() == 2);
        this.mItemAnimator.setListener(this.mItemAnimatorListener);
        initAdapterManager();
        initChildrenHelper();
        initAutofill();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(this));
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.RecyclerView, i, 0);
            String string = obtainStyledAttributes2.getString(R.styleable.RecyclerView_layoutManager);
            if (obtainStyledAttributes2.getInt(R.styleable.RecyclerView_android_descendantFocusability, -1) == -1) {
                setDescendantFocusability(262144);
            }
            this.mEnableFastScroller = obtainStyledAttributes2.getBoolean(R.styleable.RecyclerView_fastScrollEnabled, false);
            if (this.mEnableFastScroller) {
                initFastScroller((StateListDrawable) obtainStyledAttributes2.getDrawable(R.styleable.RecyclerView_fastScrollVerticalThumbDrawable), obtainStyledAttributes2.getDrawable(R.styleable.RecyclerView_fastScrollVerticalTrackDrawable), (StateListDrawable) obtainStyledAttributes2.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable), obtainStyledAttributes2.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable));
            }
            obtainStyledAttributes2.recycle();
            createLayoutManager(context, string, attributeSet, i, 0);
            if (VERSION.SDK_INT >= 21) {
                TypedArray obtainStyledAttributes3 = context.obtainStyledAttributes(attributeSet, NESTED_SCROLLING_ATTRS, i, 0);
                boolean z2 = obtainStyledAttributes3.getBoolean(0, true);
                obtainStyledAttributes3.recycle();
                z = z2;
            }
        } else {
            setDescendantFocusability(262144);
        }
        setNestedScrollingEnabled(z);
    }

    /* access modifiers changed from: 0000 */
    public String exceptionLabel() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(super.toString());
        sb.append(", adapter:");
        sb.append(this.mAdapter);
        sb.append(", layout:");
        sb.append(this.mLayout);
        sb.append(", context:");
        sb.append(getContext());
        return sb.toString();
    }

    @SuppressLint({"InlinedApi"})
    private void initAutofill() {
        if (ViewCompat.getImportantForAutofill(this) == 0) {
            ViewCompat.setImportantForAutofill(this, 8);
        }
    }

    @Nullable
    public RecyclerViewAccessibilityDelegate getCompatAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public void setAccessibilityDelegateCompat(@Nullable RecyclerViewAccessibilityDelegate recyclerViewAccessibilityDelegate) {
        this.mAccessibilityDelegate = recyclerViewAccessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityDelegate);
    }

    private void createLayoutManager(Context context, String str, AttributeSet attributeSet, int i, int i2) {
        ClassLoader classLoader;
        Constructor constructor;
        String str2 = ": Could not instantiate the LayoutManager: ";
        if (str != null) {
            String trim = str.trim();
            if (!trim.isEmpty()) {
                String fullClassName = getFullClassName(context, trim);
                try {
                    if (isInEditMode()) {
                        classLoader = getClass().getClassLoader();
                    } else {
                        classLoader = context.getClassLoader();
                    }
                    Class asSubclass = classLoader.loadClass(fullClassName).asSubclass(LayoutManager.class);
                    Object[] objArr = null;
                    try {
                        constructor = asSubclass.getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        objArr = new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)};
                    } catch (NoSuchMethodException e) {
                        constructor = asSubclass.getConstructor(new Class[0]);
                    }
                    constructor.setAccessible(true);
                    setLayoutManager((LayoutManager) constructor.newInstance(objArr));
                } catch (NoSuchMethodException e2) {
                    e2.initCause(e);
                    StringBuilder sb = new StringBuilder();
                    sb.append(attributeSet.getPositionDescription());
                    sb.append(": Error creating LayoutManager ");
                    sb.append(fullClassName);
                    throw new IllegalStateException(sb.toString(), e2);
                } catch (ClassNotFoundException e3) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(attributeSet.getPositionDescription());
                    sb2.append(": Unable to find LayoutManager ");
                    sb2.append(fullClassName);
                    throw new IllegalStateException(sb2.toString(), e3);
                } catch (InvocationTargetException e4) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(attributeSet.getPositionDescription());
                    sb3.append(str2);
                    sb3.append(fullClassName);
                    throw new IllegalStateException(sb3.toString(), e4);
                } catch (InstantiationException e5) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append(attributeSet.getPositionDescription());
                    sb4.append(str2);
                    sb4.append(fullClassName);
                    throw new IllegalStateException(sb4.toString(), e5);
                } catch (IllegalAccessException e6) {
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append(attributeSet.getPositionDescription());
                    sb5.append(": Cannot access non-public constructor ");
                    sb5.append(fullClassName);
                    throw new IllegalStateException(sb5.toString(), e6);
                } catch (ClassCastException e7) {
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append(attributeSet.getPositionDescription());
                    sb6.append(": Class is not a LayoutManager ");
                    sb6.append(fullClassName);
                    throw new IllegalStateException(sb6.toString(), e7);
                }
            }
        }
    }

    private String getFullClassName(Context context, String str) {
        if (str.charAt(0) == '.') {
            StringBuilder sb = new StringBuilder();
            sb.append(context.getPackageName());
            sb.append(str);
            return sb.toString();
        } else if (str.contains(".")) {
            return str;
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(RecyclerView.class.getPackage().getName());
            sb2.append('.');
            sb2.append(str);
            return sb2.toString();
        }
    }

    private void initChildrenHelper() {
        this.mChildHelper = new ChildHelper(new Callback() {
            public int getChildCount() {
                return RecyclerView.this.getChildCount();
            }

            public void addView(View view, int i) {
                RecyclerView.this.addView(view, i);
                RecyclerView.this.dispatchChildAttached(view);
            }

            public int indexOfChild(View view) {
                return RecyclerView.this.indexOfChild(view);
            }

            public void removeViewAt(int i) {
                View childAt = RecyclerView.this.getChildAt(i);
                if (childAt != null) {
                    RecyclerView.this.dispatchChildDetached(childAt);
                    childAt.clearAnimation();
                }
                RecyclerView.this.removeViewAt(i);
            }

            public View getChildAt(int i) {
                return RecyclerView.this.getChildAt(i);
            }

            public void removeAllViews() {
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = getChildAt(i);
                    RecyclerView.this.dispatchChildDetached(childAt);
                    childAt.clearAnimation();
                }
                RecyclerView.this.removeAllViews();
            }

            public ViewHolder getChildViewHolder(View view) {
                return RecyclerView.getChildViewHolderInt(view);
            }

            public void attachViewToParent(View view, int i, android.view.ViewGroup.LayoutParams layoutParams) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    if (childViewHolderInt.isTmpDetached() || childViewHolderInt.shouldIgnore()) {
                        childViewHolderInt.clearTmpDetachFlag();
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Called attach on a child which is not detached: ");
                        sb.append(childViewHolderInt);
                        sb.append(RecyclerView.this.exceptionLabel());
                        throw new IllegalArgumentException(sb.toString());
                    }
                }
                RecyclerView.this.attachViewToParent(view, i, layoutParams);
            }

            public void detachViewFromParent(int i) {
                View childAt = getChildAt(i);
                if (childAt != null) {
                    ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt);
                    if (childViewHolderInt != null) {
                        if (!childViewHolderInt.isTmpDetached() || childViewHolderInt.shouldIgnore()) {
                            childViewHolderInt.addFlags(256);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("called detach on an already detached child ");
                            sb.append(childViewHolderInt);
                            sb.append(RecyclerView.this.exceptionLabel());
                            throw new IllegalArgumentException(sb.toString());
                        }
                    }
                }
                RecyclerView.this.detachViewFromParent(i);
            }

            public void onEnteredHiddenState(View view) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    childViewHolderInt.onEnteredHiddenState(RecyclerView.this);
                }
            }

            public void onLeftHiddenState(View view) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    childViewHolderInt.onLeftHiddenState(RecyclerView.this);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void initAdapterManager() {
        this.mAdapterHelper = new AdapterHelper(new Callback() {
            public ViewHolder findViewHolder(int i) {
                ViewHolder findViewHolderForPosition = RecyclerView.this.findViewHolderForPosition(i, true);
                if (findViewHolderForPosition != null && !RecyclerView.this.mChildHelper.isHidden(findViewHolderForPosition.itemView)) {
                    return findViewHolderForPosition;
                }
                return null;
            }

            public void offsetPositionsForRemovingInvisible(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForRemove(i, i2, true);
                RecyclerView recyclerView = RecyclerView.this;
                recyclerView.mItemsAddedOrRemoved = true;
                recyclerView.mState.mDeletedInvisibleItemCountSincePreviousLayout += i2;
            }

            public void offsetPositionsForRemovingLaidOutOrNewView(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForRemove(i, i2, false);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void markViewHoldersUpdated(int i, int i2, Object obj) {
                RecyclerView.this.viewRangeUpdate(i, i2, obj);
                RecyclerView.this.mItemsChanged = true;
            }

            public void onDispatchFirstPass(UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }

            /* access modifiers changed from: 0000 */
            public void dispatchUpdate(UpdateOp updateOp) {
                int i = updateOp.cmd;
                if (i == 1) {
                    RecyclerView.this.mLayout.onItemsAdded(RecyclerView.this, updateOp.positionStart, updateOp.itemCount);
                } else if (i == 2) {
                    RecyclerView.this.mLayout.onItemsRemoved(RecyclerView.this, updateOp.positionStart, updateOp.itemCount);
                } else if (i == 4) {
                    RecyclerView.this.mLayout.onItemsUpdated(RecyclerView.this, updateOp.positionStart, updateOp.itemCount, updateOp.payload);
                } else if (i == 8) {
                    RecyclerView.this.mLayout.onItemsMoved(RecyclerView.this, updateOp.positionStart, updateOp.itemCount, 1);
                }
            }

            public void onDispatchSecondPass(UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }

            public void offsetPositionsForAdd(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForInsert(i, i2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void offsetPositionsForMove(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForMove(i, i2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }
        });
    }

    public void setHasFixedSize(boolean z) {
        this.mHasFixedSize = z;
    }

    public boolean hasFixedSize() {
        return this.mHasFixedSize;
    }

    public void setClipToPadding(boolean z) {
        if (z != this.mClipToPadding) {
            invalidateGlows();
        }
        this.mClipToPadding = z;
        super.setClipToPadding(z);
        if (this.mFirstLayoutComplete) {
            requestLayout();
        }
    }

    public boolean getClipToPadding() {
        return this.mClipToPadding;
    }

    public void setScrollingTouchSlop(int i) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        if (i != 0) {
            if (i != 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("setScrollingTouchSlop(): bad argument constant ");
                sb.append(i);
                sb.append("; using default value");
                Log.w(TAG, sb.toString());
            } else {
                this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
                return;
            }
        }
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    public void swapAdapter(@Nullable Adapter adapter, boolean z) {
        setLayoutFrozen(false);
        setAdapterInternal(adapter, true, z);
        processDataSetCompletelyChanged(true);
        requestLayout();
    }

    public void setAdapter(@Nullable Adapter adapter) {
        setLayoutFrozen(false);
        setAdapterInternal(adapter, false, true);
        processDataSetCompletelyChanged(false);
        requestLayout();
    }

    /* access modifiers changed from: 0000 */
    public void removeAndRecycleViews() {
        ItemAnimator itemAnimator = this.mItemAnimator;
        if (itemAnimator != null) {
            itemAnimator.endAnimations();
        }
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.removeAndRecycleAllViews(this.mRecycler);
            this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        }
        this.mRecycler.clear();
    }

    private void setAdapterInternal(@Nullable Adapter adapter, boolean z, boolean z2) {
        Adapter adapter2 = this.mAdapter;
        if (adapter2 != null) {
            adapter2.unregisterAdapterDataObserver(this.mObserver);
            this.mAdapter.onDetachedFromRecyclerView(this);
        }
        if (!z || z2) {
            removeAndRecycleViews();
        }
        this.mAdapterHelper.reset();
        Adapter adapter3 = this.mAdapter;
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.mObserver);
            adapter.onAttachedToRecyclerView(this);
        }
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.onAdapterChanged(adapter3, this.mAdapter);
        }
        this.mRecycler.onAdapterChanged(adapter3, this.mAdapter, z);
        this.mState.mStructureChanged = true;
    }

    @Nullable
    public Adapter getAdapter() {
        return this.mAdapter;
    }

    public void setRecyclerListener(@Nullable RecyclerListener recyclerListener) {
        this.mRecyclerListener = recyclerListener;
    }

    public int getBaseline() {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.getBaseline();
        }
        return super.getBaseline();
    }

    public void addOnChildAttachStateChangeListener(@NonNull OnChildAttachStateChangeListener onChildAttachStateChangeListener) {
        if (this.mOnChildAttachStateListeners == null) {
            this.mOnChildAttachStateListeners = new ArrayList();
        }
        this.mOnChildAttachStateListeners.add(onChildAttachStateChangeListener);
    }

    public void removeOnChildAttachStateChangeListener(@NonNull OnChildAttachStateChangeListener onChildAttachStateChangeListener) {
        List<OnChildAttachStateChangeListener> list = this.mOnChildAttachStateListeners;
        if (list != null) {
            list.remove(onChildAttachStateChangeListener);
        }
    }

    public void clearOnChildAttachStateChangeListeners() {
        List<OnChildAttachStateChangeListener> list = this.mOnChildAttachStateListeners;
        if (list != null) {
            list.clear();
        }
    }

    public void setLayoutManager(@Nullable LayoutManager layoutManager) {
        if (layoutManager != this.mLayout) {
            stopScroll();
            if (this.mLayout != null) {
                ItemAnimator itemAnimator = this.mItemAnimator;
                if (itemAnimator != null) {
                    itemAnimator.endAnimations();
                }
                this.mLayout.removeAndRecycleAllViews(this.mRecycler);
                this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
                this.mRecycler.clear();
                if (this.mIsAttached) {
                    this.mLayout.dispatchDetachedFromWindow(this, this.mRecycler);
                }
                this.mLayout.setRecyclerView(null);
                this.mLayout = null;
            } else {
                this.mRecycler.clear();
            }
            this.mChildHelper.removeAllViewsUnfiltered();
            this.mLayout = layoutManager;
            if (layoutManager != null) {
                if (layoutManager.mRecyclerView == null) {
                    this.mLayout.setRecyclerView(this);
                    if (this.mIsAttached) {
                        this.mLayout.dispatchAttachedToWindow(this);
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("LayoutManager ");
                    sb.append(layoutManager);
                    sb.append(" is already attached to a RecyclerView:");
                    sb.append(layoutManager.mRecyclerView.exceptionLabel());
                    throw new IllegalArgumentException(sb.toString());
                }
            }
            this.mRecycler.updateViewCacheSize();
            requestLayout();
        }
    }

    public void setOnFlingListener(@Nullable OnFlingListener onFlingListener) {
        this.mOnFlingListener = onFlingListener;
    }

    @Nullable
    public OnFlingListener getOnFlingListener() {
        return this.mOnFlingListener;
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        SavedState savedState2 = this.mPendingSavedState;
        if (savedState2 != null) {
            savedState.copyFrom(savedState2);
        } else {
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager != null) {
                savedState.mLayoutState = layoutManager.onSaveInstanceState();
            } else {
                savedState.mLayoutState = null;
            }
        }
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        this.mPendingSavedState = (SavedState) parcelable;
        super.onRestoreInstanceState(this.mPendingSavedState.getSuperState());
        if (!(this.mLayout == null || this.mPendingSavedState.mLayoutState == null)) {
            this.mLayout.onRestoreInstanceState(this.mPendingSavedState.mLayoutState);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchFreezeSelfOnly(sparseArray);
    }

    /* access modifiers changed from: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchThawSelfOnly(sparseArray);
    }

    private void addAnimatingView(ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        boolean z = view.getParent() == this;
        this.mRecycler.unscrapView(getChildViewHolder(view));
        if (viewHolder.isTmpDetached()) {
            this.mChildHelper.attachViewToParent(view, -1, view.getLayoutParams(), true);
        } else if (!z) {
            this.mChildHelper.addView(view, true);
        } else {
            this.mChildHelper.hide(view);
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean removeAnimatingView(View view) {
        startInterceptRequestLayout();
        boolean removeViewIfHidden = this.mChildHelper.removeViewIfHidden(view);
        if (removeViewIfHidden) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(view);
            this.mRecycler.unscrapView(childViewHolderInt);
            this.mRecycler.recycleViewHolderInternal(childViewHolderInt);
        }
        stopInterceptRequestLayout(!removeViewIfHidden);
        return removeViewIfHidden;
    }

    @Nullable
    public LayoutManager getLayoutManager() {
        return this.mLayout;
    }

    @NonNull
    public RecycledViewPool getRecycledViewPool() {
        return this.mRecycler.getRecycledViewPool();
    }

    public void setRecycledViewPool(@Nullable RecycledViewPool recycledViewPool) {
        this.mRecycler.setRecycledViewPool(recycledViewPool);
    }

    public void setViewCacheExtension(@Nullable ViewCacheExtension viewCacheExtension) {
        this.mRecycler.setViewCacheExtension(viewCacheExtension);
    }

    public void setItemViewCacheSize(int i) {
        this.mRecycler.setViewCacheSize(i);
    }

    public int getScrollState() {
        return this.mScrollState;
    }

    /* access modifiers changed from: 0000 */
    public void setScrollState(int i) {
        if (i != this.mScrollState) {
            this.mScrollState = i;
            if (i != 2) {
                stopScrollersInternal();
            }
            dispatchOnScrollStateChanged(i);
        }
    }

    public void addItemDecoration(@NonNull ItemDecoration itemDecoration, int i) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or layout");
        }
        if (this.mItemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (i < 0) {
            this.mItemDecorations.add(itemDecoration);
        } else {
            this.mItemDecorations.add(i, itemDecoration);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void addItemDecoration(@NonNull ItemDecoration itemDecoration) {
        addItemDecoration(itemDecoration, -1);
    }

    @NonNull
    public ItemDecoration getItemDecorationAt(int i) {
        int itemDecorationCount = getItemDecorationCount();
        if (i >= 0 && i < itemDecorationCount) {
            return (ItemDecoration) this.mItemDecorations.get(i);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        sb.append(" is an invalid index for size ");
        sb.append(itemDecorationCount);
        throw new IndexOutOfBoundsException(sb.toString());
    }

    public int getItemDecorationCount() {
        return this.mItemDecorations.size();
    }

    public void removeItemDecorationAt(int i) {
        int itemDecorationCount = getItemDecorationCount();
        if (i < 0 || i >= itemDecorationCount) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(" is an invalid index for size ");
            sb.append(itemDecorationCount);
            throw new IndexOutOfBoundsException(sb.toString());
        }
        removeItemDecoration(getItemDecorationAt(i));
    }

    public void removeItemDecoration(@NonNull ItemDecoration itemDecoration) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.assertNotInLayoutOrScroll("Cannot remove item decoration during a scroll  or layout");
        }
        this.mItemDecorations.remove(itemDecoration);
        if (this.mItemDecorations.isEmpty()) {
            setWillNotDraw(getOverScrollMode() == 2);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void setChildDrawingOrderCallback(@Nullable ChildDrawingOrderCallback childDrawingOrderCallback) {
        if (childDrawingOrderCallback != this.mChildDrawingOrderCallback) {
            this.mChildDrawingOrderCallback = childDrawingOrderCallback;
            setChildrenDrawingOrderEnabled(this.mChildDrawingOrderCallback != null);
        }
    }

    @Deprecated
    public void setOnScrollListener(@Nullable OnScrollListener onScrollListener) {
        this.mScrollListener = onScrollListener;
    }

    public void addOnScrollListener(@NonNull OnScrollListener onScrollListener) {
        if (this.mScrollListeners == null) {
            this.mScrollListeners = new ArrayList();
        }
        this.mScrollListeners.add(onScrollListener);
    }

    public void removeOnScrollListener(@NonNull OnScrollListener onScrollListener) {
        List<OnScrollListener> list = this.mScrollListeners;
        if (list != null) {
            list.remove(onScrollListener);
        }
    }

    public void clearOnScrollListeners() {
        List<OnScrollListener> list = this.mScrollListeners;
        if (list != null) {
            list.clear();
        }
    }

    public void scrollToPosition(int i) {
        if (!this.mLayoutFrozen) {
            stopScroll();
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager == null) {
                Log.e(TAG, "Cannot scroll to position a LayoutManager set. Call setLayoutManager with a non-null argument.");
                return;
            }
            layoutManager.scrollToPosition(i);
            awakenScrollBars();
        }
    }

    /* access modifiers changed from: 0000 */
    public void jumpToPositionForSmoothScroller(int i) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.scrollToPosition(i);
            awakenScrollBars();
        }
    }

    public void smoothScrollToPosition(int i) {
        if (!this.mLayoutFrozen) {
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager == null) {
                Log.e(TAG, "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            } else {
                layoutManager.smoothScrollToPosition(this, this.mState, i);
            }
        }
    }

    public void scrollTo(int i, int i2) {
        Log.w(TAG, "RecyclerView does not support scrolling to an absolute position. Use scrollToPosition instead");
    }

    public void scrollBy(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e(TAG, "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (!this.mLayoutFrozen) {
            boolean canScrollHorizontally = layoutManager.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (canScrollHorizontally || canScrollVertically) {
                if (!canScrollHorizontally) {
                    i = 0;
                }
                if (!canScrollVertically) {
                    i2 = 0;
                }
                scrollByInternal(i, i2, null);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void scrollStep(int i, int i2, @Nullable int[] iArr) {
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();
        TraceCompat.beginSection(TRACE_SCROLL_TAG);
        fillRemainingScrollValues(this.mState);
        int scrollHorizontallyBy = i != 0 ? this.mLayout.scrollHorizontallyBy(i, this.mRecycler, this.mState) : 0;
        int scrollVerticallyBy = i2 != 0 ? this.mLayout.scrollVerticallyBy(i2, this.mRecycler, this.mState) : 0;
        TraceCompat.endSection();
        repositionShadowingViews();
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
        if (iArr != null) {
            iArr[0] = scrollHorizontallyBy;
            iArr[1] = scrollVerticallyBy;
        }
    }

    /* access modifiers changed from: 0000 */
    public void consumePendingUpdateOperations() {
        boolean z = this.mFirstLayoutComplete;
        String str = TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG;
        if (!z || this.mDataSetHasChangedAfterLayout) {
            TraceCompat.beginSection(str);
            dispatchLayout();
            TraceCompat.endSection();
        } else if (this.mAdapterHelper.hasPendingUpdates()) {
            if (this.mAdapterHelper.hasAnyUpdateTypes(4) && !this.mAdapterHelper.hasAnyUpdateTypes(11)) {
                TraceCompat.beginSection(TRACE_HANDLE_ADAPTER_UPDATES_TAG);
                startInterceptRequestLayout();
                onEnterLayoutOrScroll();
                this.mAdapterHelper.preProcess();
                if (!this.mLayoutWasDefered) {
                    if (hasUpdatedView()) {
                        dispatchLayout();
                    } else {
                        this.mAdapterHelper.consumePostponedUpdates();
                    }
                }
                stopInterceptRequestLayout(true);
                onExitLayoutOrScroll();
                TraceCompat.endSection();
            } else if (this.mAdapterHelper.hasPendingUpdates()) {
                TraceCompat.beginSection(str);
                dispatchLayout();
                TraceCompat.endSection();
            }
        }
    }

    private boolean hasUpdatedView() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.isUpdated()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x009b, code lost:
        if (r0 != 0) goto L_0x00a0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean scrollByInternal(int r19, int r20, android.view.MotionEvent r21) {
        /*
            r18 = this;
            r7 = r18
            r8 = r19
            r9 = r20
            r10 = r21
            r18.consumePendingUpdateOperations()
            androidx.recyclerview.widget.RecyclerView$Adapter r0 = r7.mAdapter
            r11 = 1
            r12 = 0
            if (r0 == 0) goto L_0x0025
            int[] r0 = r7.mScrollStepConsumed
            r7.scrollStep(r8, r9, r0)
            int[] r0 = r7.mScrollStepConsumed
            r1 = r0[r12]
            r0 = r0[r11]
            int r2 = r8 - r1
            int r3 = r9 - r0
            r6 = r0
            r15 = r1
            r13 = r2
            r14 = r3
            goto L_0x0029
        L_0x0025:
            r6 = 0
            r13 = 0
            r14 = 0
            r15 = 0
        L_0x0029:
            java.util.ArrayList<androidx.recyclerview.widget.RecyclerView$ItemDecoration> r0 = r7.mItemDecorations
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0034
            r18.invalidate()
        L_0x0034:
            int[] r5 = r7.mScrollOffset
            r16 = 0
            r0 = r18
            r1 = r15
            r2 = r6
            r3 = r13
            r4 = r14
            r17 = r6
            r6 = r16
            boolean r0 = r0.dispatchNestedScroll(r1, r2, r3, r4, r5, r6)
            if (r0 == 0) goto L_0x0076
            int r0 = r7.mLastTouchX
            int[] r1 = r7.mScrollOffset
            r2 = r1[r12]
            int r0 = r0 - r2
            r7.mLastTouchX = r0
            int r0 = r7.mLastTouchY
            r2 = r1[r11]
            int r0 = r0 - r2
            r7.mLastTouchY = r0
            if (r10 == 0) goto L_0x0063
            r0 = r1[r12]
            float r0 = (float) r0
            r1 = r1[r11]
            float r1 = (float) r1
            r10.offsetLocation(r0, r1)
        L_0x0063:
            int[] r0 = r7.mNestedOffsets
            r1 = r0[r12]
            int[] r2 = r7.mScrollOffset
            r3 = r2[r12]
            int r1 = r1 + r3
            r0[r12] = r1
            r1 = r0[r11]
            r2 = r2[r11]
            int r1 = r1 + r2
            r0[r11] = r1
            goto L_0x0097
        L_0x0076:
            int r0 = r18.getOverScrollMode()
            r1 = 2
            if (r0 == r1) goto L_0x0097
            if (r10 == 0) goto L_0x0094
            r0 = 8194(0x2002, float:1.1482E-41)
            boolean r0 = androidx.core.view.MotionEventCompat.isFromSource(r10, r0)
            if (r0 != 0) goto L_0x0094
            float r0 = r21.getX()
            float r1 = (float) r13
            float r2 = r21.getY()
            float r3 = (float) r14
            r7.pullGlows(r0, r1, r2, r3)
        L_0x0094:
            r18.considerReleasingGlowsOnScroll(r19, r20)
        L_0x0097:
            if (r15 != 0) goto L_0x009e
            r0 = r17
            if (r0 == 0) goto L_0x00a3
            goto L_0x00a0
        L_0x009e:
            r0 = r17
        L_0x00a0:
            r7.dispatchOnScrolled(r15, r0)
        L_0x00a3:
            boolean r1 = r18.awakenScrollBars()
            if (r1 != 0) goto L_0x00ac
            r18.invalidate()
        L_0x00ac:
            if (r15 != 0) goto L_0x00b0
            if (r0 == 0) goto L_0x00b1
        L_0x00b0:
            r12 = 1
        L_0x00b1:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.scrollByInternal(int, int, android.view.MotionEvent):boolean");
    }

    public int computeHorizontalScrollOffset() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollOffset(this.mState);
        }
        return i;
    }

    public int computeHorizontalScrollExtent() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollExtent(this.mState);
        }
        return i;
    }

    public int computeHorizontalScrollRange() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollRange(this.mState);
        }
        return i;
    }

    public int computeVerticalScrollOffset() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollOffset(this.mState);
        }
        return i;
    }

    public int computeVerticalScrollExtent() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollExtent(this.mState);
        }
        return i;
    }

    public int computeVerticalScrollRange() {
        LayoutManager layoutManager = this.mLayout;
        int i = 0;
        if (layoutManager == null) {
            return 0;
        }
        if (layoutManager.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollRange(this.mState);
        }
        return i;
    }

    /* access modifiers changed from: 0000 */
    public void startInterceptRequestLayout() {
        this.mInterceptRequestLayoutDepth++;
        if (this.mInterceptRequestLayoutDepth == 1 && !this.mLayoutFrozen) {
            this.mLayoutWasDefered = false;
        }
    }

    /* access modifiers changed from: 0000 */
    public void stopInterceptRequestLayout(boolean z) {
        if (this.mInterceptRequestLayoutDepth < 1) {
            this.mInterceptRequestLayoutDepth = 1;
        }
        if (!z && !this.mLayoutFrozen) {
            this.mLayoutWasDefered = false;
        }
        if (this.mInterceptRequestLayoutDepth == 1) {
            if (z && this.mLayoutWasDefered && !this.mLayoutFrozen && this.mLayout != null && this.mAdapter != null) {
                dispatchLayout();
            }
            if (!this.mLayoutFrozen) {
                this.mLayoutWasDefered = false;
            }
        }
        this.mInterceptRequestLayoutDepth--;
    }

    public void setLayoutFrozen(boolean z) {
        if (z != this.mLayoutFrozen) {
            assertNotInLayoutOrScroll("Do not setLayoutFrozen in layout or scroll");
            if (!z) {
                this.mLayoutFrozen = false;
                if (!(!this.mLayoutWasDefered || this.mLayout == null || this.mAdapter == null)) {
                    requestLayout();
                }
                this.mLayoutWasDefered = false;
                return;
            }
            long uptimeMillis = SystemClock.uptimeMillis();
            onTouchEvent(MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0));
            this.mLayoutFrozen = true;
            this.mIgnoreMotionEventTillDown = true;
            stopScroll();
        }
    }

    public boolean isLayoutFrozen() {
        return this.mLayoutFrozen;
    }

    public void smoothScrollBy(@Px int i, @Px int i2) {
        smoothScrollBy(i, i2, null);
    }

    public void smoothScrollBy(@Px int i, @Px int i2, @Nullable Interpolator interpolator) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e(TAG, "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (!this.mLayoutFrozen) {
            if (!layoutManager.canScrollHorizontally()) {
                i = 0;
            }
            if (!this.mLayout.canScrollVertically()) {
                i2 = 0;
            }
            if (!(i == 0 && i2 == 0)) {
                this.mViewFlinger.smoothScrollBy(i, i2, interpolator);
            }
        }
    }

    public boolean fling(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        int i3 = 0;
        if (layoutManager == null) {
            Log.e(TAG, "Cannot fling without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return false;
        } else if (this.mLayoutFrozen) {
            return false;
        } else {
            boolean canScrollHorizontally = layoutManager.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (!canScrollHorizontally || Math.abs(i) < this.mMinFlingVelocity) {
                i = 0;
            }
            if (!canScrollVertically || Math.abs(i2) < this.mMinFlingVelocity) {
                i2 = 0;
            }
            if (i == 0 && i2 == 0) {
                return false;
            }
            float f = (float) i;
            float f2 = (float) i2;
            if (!dispatchNestedPreFling(f, f2)) {
                boolean z = canScrollHorizontally || canScrollVertically;
                dispatchNestedFling(f, f2, z);
                OnFlingListener onFlingListener = this.mOnFlingListener;
                if (onFlingListener != null && onFlingListener.onFling(i, i2)) {
                    return true;
                }
                if (z) {
                    if (canScrollHorizontally) {
                        i3 = 1;
                    }
                    if (canScrollVertically) {
                        i3 |= 2;
                    }
                    startNestedScroll(i3, 1);
                    int i4 = this.mMaxFlingVelocity;
                    int max = Math.max(-i4, Math.min(i, i4));
                    int i5 = this.mMaxFlingVelocity;
                    this.mViewFlinger.fling(max, Math.max(-i5, Math.min(i2, i5)));
                    return true;
                }
            }
            return false;
        }
    }

    public void stopScroll() {
        setScrollState(0);
        stopScrollersInternal();
    }

    private void stopScrollersInternal() {
        this.mViewFlinger.stop();
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.stopSmoothScroller();
        }
    }

    public int getMinFlingVelocity() {
        return this.mMinFlingVelocity;
    }

    public int getMaxFlingVelocity() {
        return this.mMaxFlingVelocity;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0056  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pullGlows(float r7, float r8, float r9, float r10) {
        /*
            r6 = this;
            r0 = 1065353216(0x3f800000, float:1.0)
            r1 = 1
            r2 = 0
            int r3 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r3 >= 0) goto L_0x0021
            r6.ensureLeftGlow()
            android.widget.EdgeEffect r3 = r6.mLeftGlow
            float r4 = -r8
            int r5 = r6.getWidth()
            float r5 = (float) r5
            float r4 = r4 / r5
            int r5 = r6.getHeight()
            float r5 = (float) r5
            float r9 = r9 / r5
            float r9 = r0 - r9
            androidx.core.widget.EdgeEffectCompat.onPull(r3, r4, r9)
        L_0x001f:
            r9 = 1
            goto L_0x003c
        L_0x0021:
            int r3 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r3 <= 0) goto L_0x003b
            r6.ensureRightGlow()
            android.widget.EdgeEffect r3 = r6.mRightGlow
            int r4 = r6.getWidth()
            float r4 = (float) r4
            float r4 = r8 / r4
            int r5 = r6.getHeight()
            float r5 = (float) r5
            float r9 = r9 / r5
            androidx.core.widget.EdgeEffectCompat.onPull(r3, r4, r9)
            goto L_0x001f
        L_0x003b:
            r9 = 0
        L_0x003c:
            int r3 = (r10 > r2 ? 1 : (r10 == r2 ? 0 : -1))
            if (r3 >= 0) goto L_0x0056
            r6.ensureTopGlow()
            android.widget.EdgeEffect r9 = r6.mTopGlow
            float r0 = -r10
            int r3 = r6.getHeight()
            float r3 = (float) r3
            float r0 = r0 / r3
            int r3 = r6.getWidth()
            float r3 = (float) r3
            float r7 = r7 / r3
            androidx.core.widget.EdgeEffectCompat.onPull(r9, r0, r7)
            goto L_0x0072
        L_0x0056:
            int r3 = (r10 > r2 ? 1 : (r10 == r2 ? 0 : -1))
            if (r3 <= 0) goto L_0x0071
            r6.ensureBottomGlow()
            android.widget.EdgeEffect r9 = r6.mBottomGlow
            int r3 = r6.getHeight()
            float r3 = (float) r3
            float r3 = r10 / r3
            int r4 = r6.getWidth()
            float r4 = (float) r4
            float r7 = r7 / r4
            float r0 = r0 - r7
            androidx.core.widget.EdgeEffectCompat.onPull(r9, r3, r0)
            goto L_0x0072
        L_0x0071:
            r1 = r9
        L_0x0072:
            if (r1 != 0) goto L_0x007c
            int r7 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r7 != 0) goto L_0x007c
            int r7 = (r10 > r2 ? 1 : (r10 == r2 ? 0 : -1))
            if (r7 == 0) goto L_0x007f
        L_0x007c:
            androidx.core.view.ViewCompat.postInvalidateOnAnimation(r6)
        L_0x007f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.pullGlows(float, float, float, float):void");
    }

    private void releaseGlows() {
        boolean z;
        EdgeEffect edgeEffect = this.mLeftGlow;
        if (edgeEffect != null) {
            edgeEffect.onRelease();
            z = this.mLeftGlow.isFinished();
        } else {
            z = false;
        }
        EdgeEffect edgeEffect2 = this.mTopGlow;
        if (edgeEffect2 != null) {
            edgeEffect2.onRelease();
            z |= this.mTopGlow.isFinished();
        }
        EdgeEffect edgeEffect3 = this.mRightGlow;
        if (edgeEffect3 != null) {
            edgeEffect3.onRelease();
            z |= this.mRightGlow.isFinished();
        }
        EdgeEffect edgeEffect4 = this.mBottomGlow;
        if (edgeEffect4 != null) {
            edgeEffect4.onRelease();
            z |= this.mBottomGlow.isFinished();
        }
        if (z) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: 0000 */
    public void considerReleasingGlowsOnScroll(int i, int i2) {
        boolean z;
        EdgeEffect edgeEffect = this.mLeftGlow;
        if (edgeEffect == null || edgeEffect.isFinished() || i <= 0) {
            z = false;
        } else {
            this.mLeftGlow.onRelease();
            z = this.mLeftGlow.isFinished();
        }
        EdgeEffect edgeEffect2 = this.mRightGlow;
        if (edgeEffect2 != null && !edgeEffect2.isFinished() && i < 0) {
            this.mRightGlow.onRelease();
            z |= this.mRightGlow.isFinished();
        }
        EdgeEffect edgeEffect3 = this.mTopGlow;
        if (edgeEffect3 != null && !edgeEffect3.isFinished() && i2 > 0) {
            this.mTopGlow.onRelease();
            z |= this.mTopGlow.isFinished();
        }
        EdgeEffect edgeEffect4 = this.mBottomGlow;
        if (edgeEffect4 != null && !edgeEffect4.isFinished() && i2 < 0) {
            this.mBottomGlow.onRelease();
            z |= this.mBottomGlow.isFinished();
        }
        if (z) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: 0000 */
    public void absorbGlows(int i, int i2) {
        if (i < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onAbsorb(-i);
        } else if (i > 0) {
            ensureRightGlow();
            this.mRightGlow.onAbsorb(i);
        }
        if (i2 < 0) {
            ensureTopGlow();
            this.mTopGlow.onAbsorb(-i2);
        } else if (i2 > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onAbsorb(i2);
        }
        if (i != 0 || i2 != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: 0000 */
    public void ensureLeftGlow() {
        if (this.mLeftGlow == null) {
            this.mLeftGlow = this.mEdgeEffectFactory.createEdgeEffect(this, 0);
            if (this.mClipToPadding) {
                this.mLeftGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
            } else {
                this.mLeftGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void ensureRightGlow() {
        if (this.mRightGlow == null) {
            this.mRightGlow = this.mEdgeEffectFactory.createEdgeEffect(this, 2);
            if (this.mClipToPadding) {
                this.mRightGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
            } else {
                this.mRightGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void ensureTopGlow() {
        if (this.mTopGlow == null) {
            this.mTopGlow = this.mEdgeEffectFactory.createEdgeEffect(this, 1);
            if (this.mClipToPadding) {
                this.mTopGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void ensureBottomGlow() {
        if (this.mBottomGlow == null) {
            this.mBottomGlow = this.mEdgeEffectFactory.createEdgeEffect(this, 3);
            if (this.mClipToPadding) {
                this.mBottomGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void invalidateGlows() {
        this.mBottomGlow = null;
        this.mTopGlow = null;
        this.mRightGlow = null;
        this.mLeftGlow = null;
    }

    public void setEdgeEffectFactory(@NonNull EdgeEffectFactory edgeEffectFactory) {
        Preconditions.checkNotNull(edgeEffectFactory);
        this.mEdgeEffectFactory = edgeEffectFactory;
        invalidateGlows();
    }

    @NonNull
    public EdgeEffectFactory getEdgeEffectFactory() {
        return this.mEdgeEffectFactory;
    }

    public View focusSearch(View view, int i) {
        View view2;
        boolean z;
        View onInterceptFocusSearch = this.mLayout.onInterceptFocusSearch(view, i);
        if (onInterceptFocusSearch != null) {
            return onInterceptFocusSearch;
        }
        boolean z2 = this.mAdapter != null && this.mLayout != null && !isComputingLayout() && !this.mLayoutFrozen;
        FocusFinder instance = FocusFinder.getInstance();
        if (!z2 || !(i == 2 || i == 1)) {
            View findNextFocus = instance.findNextFocus(this, view, i);
            if (findNextFocus != null || !z2) {
                view2 = findNextFocus;
            } else {
                consumePendingUpdateOperations();
                if (findContainingItemView(view) == null) {
                    return null;
                }
                startInterceptRequestLayout();
                view2 = this.mLayout.onFocusSearchFailed(view, i, this.mRecycler, this.mState);
                stopInterceptRequestLayout(false);
            }
        } else {
            if (this.mLayout.canScrollVertically()) {
                int i2 = i == 2 ? 130 : 33;
                z = instance.findNextFocus(this, view, i2) == null;
                if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                    i = i2;
                }
            } else {
                z = false;
            }
            if (!z && this.mLayout.canScrollHorizontally()) {
                int i3 = (this.mLayout.getLayoutDirection() == 1) ^ (i == 2) ? 66 : 17;
                z = instance.findNextFocus(this, view, i3) == null;
                if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                    i = i3;
                }
            }
            if (z) {
                consumePendingUpdateOperations();
                if (findContainingItemView(view) == null) {
                    return null;
                }
                startInterceptRequestLayout();
                this.mLayout.onFocusSearchFailed(view, i, this.mRecycler, this.mState);
                stopInterceptRequestLayout(false);
            }
            view2 = instance.findNextFocus(this, view, i);
        }
        if (view2 == null || view2.hasFocusable()) {
            if (!isPreferredNextFocus(view, view2, i)) {
                view2 = super.focusSearch(view, i);
            }
            return view2;
        } else if (getFocusedChild() == null) {
            return super.focusSearch(view, i);
        } else {
            requestChildOnScreen(view2, null);
            return view;
        }
    }

    private boolean isPreferredNextFocus(View view, View view2, int i) {
        boolean z = false;
        if (!(view2 == null || view2 == this)) {
            if (findContainingItemView(view2) == null) {
                return false;
            }
            if (view == null || findContainingItemView(view) == null) {
                return true;
            }
            this.mTempRect.set(0, 0, view.getWidth(), view.getHeight());
            this.mTempRect2.set(0, 0, view2.getWidth(), view2.getHeight());
            offsetDescendantRectToMyCoords(view, this.mTempRect);
            offsetDescendantRectToMyCoords(view2, this.mTempRect2);
            char c = 65535;
            int i2 = this.mLayout.getLayoutDirection() == 1 ? -1 : 1;
            int i3 = ((this.mTempRect.left < this.mTempRect2.left || this.mTempRect.right <= this.mTempRect2.left) && this.mTempRect.right < this.mTempRect2.right) ? 1 : ((this.mTempRect.right > this.mTempRect2.right || this.mTempRect.left >= this.mTempRect2.right) && this.mTempRect.left > this.mTempRect2.left) ? -1 : 0;
            if ((this.mTempRect.top < this.mTempRect2.top || this.mTempRect.bottom <= this.mTempRect2.top) && this.mTempRect.bottom < this.mTempRect2.bottom) {
                c = 1;
            } else if ((this.mTempRect.bottom <= this.mTempRect2.bottom && this.mTempRect.top < this.mTempRect2.bottom) || this.mTempRect.top <= this.mTempRect2.top) {
                c = 0;
            }
            if (i != 1) {
                if (i == 2) {
                    if (c > 0 || (c == 0 && i3 * i2 >= 0)) {
                        z = true;
                    }
                    return z;
                } else if (i == 17) {
                    if (i3 < 0) {
                        z = true;
                    }
                    return z;
                } else if (i == 33) {
                    if (c < 0) {
                        z = true;
                    }
                    return z;
                } else if (i == 66) {
                    if (i3 > 0) {
                        z = true;
                    }
                    return z;
                } else if (i == 130) {
                    if (c > 0) {
                        z = true;
                    }
                    return z;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Invalid direction: ");
                    sb.append(i);
                    sb.append(exceptionLabel());
                    throw new IllegalArgumentException(sb.toString());
                }
            } else if (c < 0 || (c == 0 && i3 * i2 <= 0)) {
                z = true;
            }
        }
        return z;
    }

    public void requestChildFocus(View view, View view2) {
        if (!this.mLayout.onRequestChildFocus(this, this.mState, view, view2) && view2 != null) {
            requestChildOnScreen(view, view2);
        }
        super.requestChildFocus(view, view2);
    }

    private void requestChildOnScreen(@NonNull View view, @Nullable View view2) {
        View view3 = view2 != null ? view2 : view;
        this.mTempRect.set(0, 0, view3.getWidth(), view3.getHeight());
        android.view.ViewGroup.LayoutParams layoutParams = view3.getLayoutParams();
        if (layoutParams instanceof LayoutParams) {
            LayoutParams layoutParams2 = (LayoutParams) layoutParams;
            if (!layoutParams2.mInsetsDirty) {
                Rect rect = layoutParams2.mDecorInsets;
                this.mTempRect.left -= rect.left;
                this.mTempRect.right += rect.right;
                this.mTempRect.top -= rect.top;
                this.mTempRect.bottom += rect.bottom;
            }
        }
        if (view2 != null) {
            offsetDescendantRectToMyCoords(view2, this.mTempRect);
            offsetRectIntoDescendantCoords(view, this.mTempRect);
        }
        this.mLayout.requestChildRectangleOnScreen(this, view, this.mTempRect, !this.mFirstLayoutComplete, view2 == null);
    }

    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        return this.mLayout.requestChildRectangleOnScreen(this, view, rect, z);
    }

    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null || !layoutManager.onAddFocusables(this, arrayList, i, i2)) {
            super.addFocusables(arrayList, i, i2);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        if (isComputingLayout()) {
            return false;
        }
        return super.onRequestFocusInDescendants(i, rect);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        if (r0 >= 30.0f) goto L_0x0054;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAttachedToWindow() {
        /*
            r4 = this;
            super.onAttachedToWindow()
            r0 = 0
            r4.mLayoutOrScrollCounter = r0
            r1 = 1
            r4.mIsAttached = r1
            boolean r2 = r4.mFirstLayoutComplete
            if (r2 == 0) goto L_0x0014
            boolean r2 = r4.isLayoutRequested()
            if (r2 != 0) goto L_0x0014
            goto L_0x0015
        L_0x0014:
            r1 = 0
        L_0x0015:
            r4.mFirstLayoutComplete = r1
            androidx.recyclerview.widget.RecyclerView$LayoutManager r1 = r4.mLayout
            if (r1 == 0) goto L_0x001e
            r1.dispatchAttachedToWindow(r4)
        L_0x001e:
            r4.mPostedAnimatorRunner = r0
            boolean r0 = ALLOW_THREAD_GAP_WORK
            if (r0 == 0) goto L_0x0069
            java.lang.ThreadLocal<androidx.recyclerview.widget.GapWorker> r0 = androidx.recyclerview.widget.GapWorker.sGapWorker
            java.lang.Object r0 = r0.get()
            androidx.recyclerview.widget.GapWorker r0 = (androidx.recyclerview.widget.GapWorker) r0
            r4.mGapWorker = r0
            androidx.recyclerview.widget.GapWorker r0 = r4.mGapWorker
            if (r0 != 0) goto L_0x0064
            androidx.recyclerview.widget.GapWorker r0 = new androidx.recyclerview.widget.GapWorker
            r0.<init>()
            r4.mGapWorker = r0
            android.view.Display r0 = androidx.core.view.ViewCompat.getDisplay(r4)
            r1 = 1114636288(0x42700000, float:60.0)
            boolean r2 = r4.isInEditMode()
            if (r2 != 0) goto L_0x0052
            if (r0 == 0) goto L_0x0052
            float r0 = r0.getRefreshRate()
            r2 = 1106247680(0x41f00000, float:30.0)
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 < 0) goto L_0x0052
            goto L_0x0054
        L_0x0052:
            r0 = 1114636288(0x42700000, float:60.0)
        L_0x0054:
            androidx.recyclerview.widget.GapWorker r1 = r4.mGapWorker
            r2 = 1315859240(0x4e6e6b28, float:1.0E9)
            float r2 = r2 / r0
            long r2 = (long) r2
            r1.mFrameIntervalNs = r2
            java.lang.ThreadLocal<androidx.recyclerview.widget.GapWorker> r0 = androidx.recyclerview.widget.GapWorker.sGapWorker
            androidx.recyclerview.widget.GapWorker r1 = r4.mGapWorker
            r0.set(r1)
        L_0x0064:
            androidx.recyclerview.widget.GapWorker r0 = r4.mGapWorker
            r0.add(r4)
        L_0x0069:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.onAttachedToWindow():void");
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ItemAnimator itemAnimator = this.mItemAnimator;
        if (itemAnimator != null) {
            itemAnimator.endAnimations();
        }
        stopScroll();
        this.mIsAttached = false;
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.dispatchDetachedFromWindow(this, this.mRecycler);
        }
        this.mPendingAccessibilityImportanceChange.clear();
        removeCallbacks(this.mItemAnimatorRunner);
        this.mViewInfoStore.onDetach();
        if (ALLOW_THREAD_GAP_WORK) {
            GapWorker gapWorker = this.mGapWorker;
            if (gapWorker != null) {
                gapWorker.remove(this);
                this.mGapWorker = null;
            }
        }
    }

    public boolean isAttachedToWindow() {
        return this.mIsAttached;
    }

    /* access modifiers changed from: 0000 */
    public void assertInLayoutOrScroll(String str) {
        if (isComputingLayout()) {
            return;
        }
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot call this method unless RecyclerView is computing a layout or scrolling");
            sb.append(exceptionLabel());
            throw new IllegalStateException(sb.toString());
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str);
        sb2.append(exceptionLabel());
        throw new IllegalStateException(sb2.toString());
    }

    /* access modifiers changed from: 0000 */
    public void assertNotInLayoutOrScroll(String str) {
        if (isComputingLayout()) {
            if (str == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot call this method while RecyclerView is computing a layout or scrolling");
                sb.append(exceptionLabel());
                throw new IllegalStateException(sb.toString());
            }
            throw new IllegalStateException(str);
        } else if (this.mDispatchScrollCounter > 0) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("");
            sb2.append(exceptionLabel());
            Log.w(TAG, "Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the RecyclerView or the adapter contents should be postponed tothe next frame.", new IllegalStateException(sb2.toString()));
        }
    }

    public void addOnItemTouchListener(@NonNull OnItemTouchListener onItemTouchListener) {
        this.mOnItemTouchListeners.add(onItemTouchListener);
    }

    public void removeOnItemTouchListener(@NonNull OnItemTouchListener onItemTouchListener) {
        this.mOnItemTouchListeners.remove(onItemTouchListener);
        if (this.mActiveOnItemTouchListener == onItemTouchListener) {
            this.mActiveOnItemTouchListener = null;
        }
    }

    private boolean dispatchOnItemTouchIntercept(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 3 || action == 0) {
            this.mActiveOnItemTouchListener = null;
        }
        int size = this.mOnItemTouchListeners.size();
        int i = 0;
        while (i < size) {
            OnItemTouchListener onItemTouchListener = (OnItemTouchListener) this.mOnItemTouchListeners.get(i);
            if (!onItemTouchListener.onInterceptTouchEvent(this, motionEvent) || action == 3) {
                i++;
            } else {
                this.mActiveOnItemTouchListener = onItemTouchListener;
                return true;
            }
        }
        return false;
    }

    private boolean dispatchOnItemTouch(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        OnItemTouchListener onItemTouchListener = this.mActiveOnItemTouchListener;
        if (onItemTouchListener != null) {
            if (action == 0) {
                this.mActiveOnItemTouchListener = null;
            } else {
                onItemTouchListener.onTouchEvent(this, motionEvent);
                if (action == 3 || action == 1) {
                    this.mActiveOnItemTouchListener = null;
                }
                return true;
            }
        }
        if (action != 0) {
            int size = this.mOnItemTouchListeners.size();
            for (int i = 0; i < size; i++) {
                OnItemTouchListener onItemTouchListener2 = (OnItemTouchListener) this.mOnItemTouchListeners.get(i);
                if (onItemTouchListener2.onInterceptTouchEvent(this, motionEvent)) {
                    this.mActiveOnItemTouchListener = onItemTouchListener2;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z;
        boolean z2 = false;
        if (this.mLayoutFrozen) {
            return false;
        }
        if (dispatchOnItemTouchIntercept(motionEvent)) {
            cancelTouch();
            return true;
        }
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            return false;
        }
        boolean canScrollHorizontally = layoutManager.canScrollHorizontally();
        boolean canScrollVertically = this.mLayout.canScrollVertically();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        if (actionMasked == 0) {
            if (this.mIgnoreMotionEventTillDown) {
                this.mIgnoreMotionEventTillDown = false;
            }
            this.mScrollPointerId = motionEvent.getPointerId(0);
            int x = (int) (motionEvent.getX() + 0.5f);
            this.mLastTouchX = x;
            this.mInitialTouchX = x;
            int y = (int) (motionEvent.getY() + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
            if (this.mScrollState == 2) {
                getParent().requestDisallowInterceptTouchEvent(true);
                setScrollState(1);
            }
            int[] iArr = this.mNestedOffsets;
            iArr[1] = 0;
            iArr[0] = 0;
            int i = canScrollHorizontally ? 1 : 0;
            if (canScrollVertically) {
                i |= 2;
            }
            startNestedScroll(i, 0);
        } else if (actionMasked == 1) {
            this.mVelocityTracker.clear();
            stopNestedScroll(0);
        } else if (actionMasked == 2) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mScrollPointerId);
            if (findPointerIndex < 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Error processing scroll; pointer index for id ");
                sb.append(this.mScrollPointerId);
                sb.append(" not found. Did any MotionEvents get skipped?");
                Log.e(TAG, sb.toString());
                return false;
            }
            int x2 = (int) (motionEvent.getX(findPointerIndex) + 0.5f);
            int y2 = (int) (motionEvent.getY(findPointerIndex) + 0.5f);
            if (this.mScrollState != 1) {
                int i2 = x2 - this.mInitialTouchX;
                int i3 = y2 - this.mInitialTouchY;
                if (!canScrollHorizontally || Math.abs(i2) <= this.mTouchSlop) {
                    z = false;
                } else {
                    this.mLastTouchX = x2;
                    z = true;
                }
                if (canScrollVertically && Math.abs(i3) > this.mTouchSlop) {
                    this.mLastTouchY = y2;
                    z = true;
                }
                if (z) {
                    setScrollState(1);
                }
            }
        } else if (actionMasked == 3) {
            cancelTouch();
        } else if (actionMasked == 5) {
            this.mScrollPointerId = motionEvent.getPointerId(actionIndex);
            int x3 = (int) (motionEvent.getX(actionIndex) + 0.5f);
            this.mLastTouchX = x3;
            this.mInitialTouchX = x3;
            int y3 = (int) (motionEvent.getY(actionIndex) + 0.5f);
            this.mLastTouchY = y3;
            this.mInitialTouchY = y3;
        } else if (actionMasked == 6) {
            onPointerUp(motionEvent);
        }
        if (this.mScrollState == 1) {
            z2 = true;
        }
        return z2;
    }

    public void requestDisallowInterceptTouchEvent(boolean z) {
        int size = this.mOnItemTouchListeners.size();
        for (int i = 0; i < size; i++) {
            ((OnItemTouchListener) this.mOnItemTouchListeners.get(i)).onRequestDisallowInterceptTouchEvent(z);
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x011c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r15) {
        /*
            r14 = this;
            boolean r0 = r14.mLayoutFrozen
            r1 = 0
            if (r0 != 0) goto L_0x01c7
            boolean r0 = r14.mIgnoreMotionEventTillDown
            if (r0 == 0) goto L_0x000b
            goto L_0x01c7
        L_0x000b:
            boolean r0 = r14.dispatchOnItemTouch(r15)
            r2 = 1
            if (r0 == 0) goto L_0x0016
            r14.cancelTouch()
            return r2
        L_0x0016:
            androidx.recyclerview.widget.RecyclerView$LayoutManager r0 = r14.mLayout
            if (r0 != 0) goto L_0x001b
            return r1
        L_0x001b:
            boolean r0 = r0.canScrollHorizontally()
            androidx.recyclerview.widget.RecyclerView$LayoutManager r3 = r14.mLayout
            boolean r3 = r3.canScrollVertically()
            android.view.VelocityTracker r4 = r14.mVelocityTracker
            if (r4 != 0) goto L_0x002f
            android.view.VelocityTracker r4 = android.view.VelocityTracker.obtain()
            r14.mVelocityTracker = r4
        L_0x002f:
            android.view.MotionEvent r4 = android.view.MotionEvent.obtain(r15)
            int r5 = r15.getActionMasked()
            int r6 = r15.getActionIndex()
            if (r5 != 0) goto L_0x0043
            int[] r7 = r14.mNestedOffsets
            r7[r2] = r1
            r7[r1] = r1
        L_0x0043:
            int[] r7 = r14.mNestedOffsets
            r8 = r7[r1]
            float r8 = (float) r8
            r7 = r7[r2]
            float r7 = (float) r7
            r4.offsetLocation(r8, r7)
            r7 = 1056964608(0x3f000000, float:0.5)
            if (r5 == 0) goto L_0x0196
            if (r5 == r2) goto L_0x0154
            r8 = 2
            if (r5 == r8) goto L_0x0088
            r0 = 3
            if (r5 == r0) goto L_0x0083
            r0 = 5
            if (r5 == r0) goto L_0x0067
            r0 = 6
            if (r5 == r0) goto L_0x0062
            goto L_0x01bc
        L_0x0062:
            r14.onPointerUp(r15)
            goto L_0x01bc
        L_0x0067:
            int r0 = r15.getPointerId(r6)
            r14.mScrollPointerId = r0
            float r0 = r15.getX(r6)
            float r0 = r0 + r7
            int r0 = (int) r0
            r14.mLastTouchX = r0
            r14.mInitialTouchX = r0
            float r15 = r15.getY(r6)
            float r15 = r15 + r7
            int r15 = (int) r15
            r14.mLastTouchY = r15
            r14.mInitialTouchY = r15
            goto L_0x01bc
        L_0x0083:
            r14.cancelTouch()
            goto L_0x01bc
        L_0x0088:
            int r5 = r14.mScrollPointerId
            int r5 = r15.findPointerIndex(r5)
            if (r5 >= 0) goto L_0x00ae
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r0 = "Error processing scroll; pointer index for id "
            r15.append(r0)
            int r0 = r14.mScrollPointerId
            r15.append(r0)
            java.lang.String r0 = " not found. Did any MotionEvents get skipped?"
            r15.append(r0)
            java.lang.String r15 = r15.toString()
            java.lang.String r0 = "RecyclerView"
            android.util.Log.e(r0, r15)
            return r1
        L_0x00ae:
            float r6 = r15.getX(r5)
            float r6 = r6 + r7
            int r6 = (int) r6
            float r15 = r15.getY(r5)
            float r15 = r15 + r7
            int r15 = (int) r15
            int r5 = r14.mLastTouchX
            int r5 = r5 - r6
            int r7 = r14.mLastTouchY
            int r13 = r7 - r15
            int[] r10 = r14.mScrollConsumed
            int[] r11 = r14.mScrollOffset
            r12 = 0
            r7 = r14
            r8 = r5
            r9 = r13
            boolean r7 = r7.dispatchNestedPreScroll(r8, r9, r10, r11, r12)
            if (r7 == 0) goto L_0x00f4
            int[] r7 = r14.mScrollConsumed
            r8 = r7[r1]
            int r5 = r5 - r8
            r7 = r7[r2]
            int r13 = r13 - r7
            int[] r7 = r14.mScrollOffset
            r8 = r7[r1]
            float r8 = (float) r8
            r7 = r7[r2]
            float r7 = (float) r7
            r4.offsetLocation(r8, r7)
            int[] r7 = r14.mNestedOffsets
            r8 = r7[r1]
            int[] r9 = r14.mScrollOffset
            r10 = r9[r1]
            int r8 = r8 + r10
            r7[r1] = r8
            r8 = r7[r2]
            r9 = r9[r2]
            int r8 = r8 + r9
            r7[r2] = r8
        L_0x00f4:
            int r7 = r14.mScrollState
            if (r7 == r2) goto L_0x011f
            if (r0 == 0) goto L_0x0109
            int r7 = java.lang.Math.abs(r5)
            int r8 = r14.mTouchSlop
            if (r7 <= r8) goto L_0x0109
            if (r5 <= 0) goto L_0x0106
            int r5 = r5 - r8
            goto L_0x0107
        L_0x0106:
            int r5 = r5 + r8
        L_0x0107:
            r7 = 1
            goto L_0x010a
        L_0x0109:
            r7 = 0
        L_0x010a:
            if (r3 == 0) goto L_0x011a
            int r8 = java.lang.Math.abs(r13)
            int r9 = r14.mTouchSlop
            if (r8 <= r9) goto L_0x011a
            if (r13 <= 0) goto L_0x0118
            int r13 = r13 - r9
            goto L_0x0119
        L_0x0118:
            int r13 = r13 + r9
        L_0x0119:
            r7 = 1
        L_0x011a:
            if (r7 == 0) goto L_0x011f
            r14.setScrollState(r2)
        L_0x011f:
            int r7 = r14.mScrollState
            if (r7 != r2) goto L_0x01bc
            int[] r7 = r14.mScrollOffset
            r8 = r7[r1]
            int r6 = r6 - r8
            r14.mLastTouchX = r6
            r6 = r7[r2]
            int r15 = r15 - r6
            r14.mLastTouchY = r15
            if (r0 == 0) goto L_0x0133
            r15 = r5
            goto L_0x0134
        L_0x0133:
            r15 = 0
        L_0x0134:
            if (r3 == 0) goto L_0x0138
            r0 = r13
            goto L_0x0139
        L_0x0138:
            r0 = 0
        L_0x0139:
            boolean r15 = r14.scrollByInternal(r15, r0, r4)
            if (r15 == 0) goto L_0x0146
            android.view.ViewParent r15 = r14.getParent()
            r15.requestDisallowInterceptTouchEvent(r2)
        L_0x0146:
            androidx.recyclerview.widget.GapWorker r15 = r14.mGapWorker
            if (r15 == 0) goto L_0x01bc
            if (r5 != 0) goto L_0x014e
            if (r13 == 0) goto L_0x01bc
        L_0x014e:
            androidx.recyclerview.widget.GapWorker r15 = r14.mGapWorker
            r15.postFromTraversal(r14, r5, r13)
            goto L_0x01bc
        L_0x0154:
            android.view.VelocityTracker r15 = r14.mVelocityTracker
            r15.addMovement(r4)
            android.view.VelocityTracker r15 = r14.mVelocityTracker
            r5 = 1000(0x3e8, float:1.401E-42)
            int r6 = r14.mMaxFlingVelocity
            float r6 = (float) r6
            r15.computeCurrentVelocity(r5, r6)
            r15 = 0
            if (r0 == 0) goto L_0x0170
            android.view.VelocityTracker r0 = r14.mVelocityTracker
            int r5 = r14.mScrollPointerId
            float r0 = r0.getXVelocity(r5)
            float r0 = -r0
            goto L_0x0171
        L_0x0170:
            r0 = 0
        L_0x0171:
            if (r3 == 0) goto L_0x017d
            android.view.VelocityTracker r3 = r14.mVelocityTracker
            int r5 = r14.mScrollPointerId
            float r3 = r3.getYVelocity(r5)
            float r3 = -r3
            goto L_0x017e
        L_0x017d:
            r3 = 0
        L_0x017e:
            int r5 = (r0 > r15 ? 1 : (r0 == r15 ? 0 : -1))
            if (r5 != 0) goto L_0x0186
            int r15 = (r3 > r15 ? 1 : (r3 == r15 ? 0 : -1))
            if (r15 == 0) goto L_0x018e
        L_0x0186:
            int r15 = (int) r0
            int r0 = (int) r3
            boolean r15 = r14.fling(r15, r0)
            if (r15 != 0) goto L_0x0191
        L_0x018e:
            r14.setScrollState(r1)
        L_0x0191:
            r14.resetTouch()
            r1 = 1
            goto L_0x01bc
        L_0x0196:
            int r5 = r15.getPointerId(r1)
            r14.mScrollPointerId = r5
            float r5 = r15.getX()
            float r5 = r5 + r7
            int r5 = (int) r5
            r14.mLastTouchX = r5
            r14.mInitialTouchX = r5
            float r15 = r15.getY()
            float r15 = r15 + r7
            int r15 = (int) r15
            r14.mLastTouchY = r15
            r14.mInitialTouchY = r15
            if (r0 == 0) goto L_0x01b4
            r15 = 1
            goto L_0x01b5
        L_0x01b4:
            r15 = 0
        L_0x01b5:
            if (r3 == 0) goto L_0x01b9
            r15 = r15 | 2
        L_0x01b9:
            r14.startNestedScroll(r15, r1)
        L_0x01bc:
            if (r1 != 0) goto L_0x01c3
            android.view.VelocityTracker r15 = r14.mVelocityTracker
            r15.addMovement(r4)
        L_0x01c3:
            r4.recycle()
            return r2
        L_0x01c7:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void resetTouch() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
        stopNestedScroll(0);
        releaseGlows();
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(0);
    }

    private void onPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mScrollPointerId) {
            int i = actionIndex == 0 ? 1 : 0;
            this.mScrollPointerId = motionEvent.getPointerId(i);
            int x = (int) (motionEvent.getX(i) + 0.5f);
            this.mLastTouchX = x;
            this.mInitialTouchX = x;
            int y = (int) (motionEvent.getY(i) + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
        }
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        float f;
        float f2;
        if (this.mLayout != null && !this.mLayoutFrozen && motionEvent.getAction() == 8) {
            if ((motionEvent.getSource() & 2) != 0) {
                f2 = this.mLayout.canScrollVertically() ? -motionEvent.getAxisValue(9) : 0.0f;
                if (this.mLayout.canScrollHorizontally()) {
                    f = motionEvent.getAxisValue(10);
                    if (!(f2 == 0.0f && f == 0.0f)) {
                        scrollByInternal((int) (f * this.mScaledHorizontalScrollFactor), (int) (f2 * this.mScaledVerticalScrollFactor), motionEvent);
                    }
                }
            } else {
                if ((motionEvent.getSource() & 4194304) != 0) {
                    float axisValue = motionEvent.getAxisValue(26);
                    if (this.mLayout.canScrollVertically()) {
                        f2 = -axisValue;
                    } else if (this.mLayout.canScrollHorizontally()) {
                        f = axisValue;
                        f2 = 0.0f;
                        scrollByInternal((int) (f * this.mScaledHorizontalScrollFactor), (int) (f2 * this.mScaledVerticalScrollFactor), motionEvent);
                    }
                }
                f2 = 0.0f;
            }
            f = 0.0f;
            scrollByInternal((int) (f * this.mScaledHorizontalScrollFactor), (int) (f2 * this.mScaledVerticalScrollFactor), motionEvent);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            defaultOnMeasure(i, i2);
            return;
        }
        boolean z = false;
        if (layoutManager.isAutoMeasureEnabled()) {
            int mode = MeasureSpec.getMode(i);
            int mode2 = MeasureSpec.getMode(i2);
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
            if (mode == 1073741824 && mode2 == 1073741824) {
                z = true;
            }
            if (!z && this.mAdapter != null) {
                if (this.mState.mLayoutStep == 1) {
                    dispatchLayoutStep1();
                }
                this.mLayout.setMeasureSpecs(i, i2);
                this.mState.mIsMeasuring = true;
                dispatchLayoutStep2();
                this.mLayout.setMeasuredDimensionFromChildren(i, i2);
                if (this.mLayout.shouldMeasureTwice()) {
                    this.mLayout.setMeasureSpecs(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
                    this.mState.mIsMeasuring = true;
                    dispatchLayoutStep2();
                    this.mLayout.setMeasuredDimensionFromChildren(i, i2);
                }
            }
        } else if (this.mHasFixedSize) {
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
        } else {
            if (this.mAdapterUpdateDuringMeasure) {
                startInterceptRequestLayout();
                onEnterLayoutOrScroll();
                processAdapterUpdatesAndSetAnimationFlags();
                onExitLayoutOrScroll();
                if (this.mState.mRunPredictiveAnimations) {
                    this.mState.mInPreLayout = true;
                } else {
                    this.mAdapterHelper.consumeUpdatesInOnePass();
                    this.mState.mInPreLayout = false;
                }
                this.mAdapterUpdateDuringMeasure = false;
                stopInterceptRequestLayout(false);
            } else if (this.mState.mRunPredictiveAnimations) {
                setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
                return;
            }
            Adapter adapter = this.mAdapter;
            if (adapter != null) {
                this.mState.mItemCount = adapter.getItemCount();
            } else {
                this.mState.mItemCount = 0;
            }
            startInterceptRequestLayout();
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
            stopInterceptRequestLayout(false);
            this.mState.mInPreLayout = false;
        }
    }

    /* access modifiers changed from: 0000 */
    public void defaultOnMeasure(int i, int i2) {
        setMeasuredDimension(LayoutManager.chooseSize(i, getPaddingLeft() + getPaddingRight(), ViewCompat.getMinimumWidth(this)), LayoutManager.chooseSize(i2, getPaddingTop() + getPaddingBottom(), ViewCompat.getMinimumHeight(this)));
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3 || i2 != i4) {
            invalidateGlows();
        }
    }

    public void setItemAnimator(@Nullable ItemAnimator itemAnimator) {
        ItemAnimator itemAnimator2 = this.mItemAnimator;
        if (itemAnimator2 != null) {
            itemAnimator2.endAnimations();
            this.mItemAnimator.setListener(null);
        }
        this.mItemAnimator = itemAnimator;
        ItemAnimator itemAnimator3 = this.mItemAnimator;
        if (itemAnimator3 != null) {
            itemAnimator3.setListener(this.mItemAnimatorListener);
        }
    }

    /* access modifiers changed from: 0000 */
    public void onEnterLayoutOrScroll() {
        this.mLayoutOrScrollCounter++;
    }

    /* access modifiers changed from: 0000 */
    public void onExitLayoutOrScroll() {
        onExitLayoutOrScroll(true);
    }

    /* access modifiers changed from: 0000 */
    public void onExitLayoutOrScroll(boolean z) {
        this.mLayoutOrScrollCounter--;
        if (this.mLayoutOrScrollCounter < 1) {
            this.mLayoutOrScrollCounter = 0;
            if (z) {
                dispatchContentChangedIfNecessary();
                dispatchPendingImportantForAccessibilityChanges();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isAccessibilityEnabled() {
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        return accessibilityManager != null && accessibilityManager.isEnabled();
    }

    private void dispatchContentChangedIfNecessary() {
        int i = this.mEatenAccessibilityChangeFlags;
        this.mEatenAccessibilityChangeFlags = 0;
        if (i != 0 && isAccessibilityEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain();
            obtain.setEventType(2048);
            AccessibilityEventCompat.setContentChangeTypes(obtain, i);
            sendAccessibilityEventUnchecked(obtain);
        }
    }

    public boolean isComputingLayout() {
        return this.mLayoutOrScrollCounter > 0;
    }

    /* access modifiers changed from: 0000 */
    public boolean shouldDeferAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (!isComputingLayout()) {
            return false;
        }
        int contentChangeTypes = accessibilityEvent != null ? AccessibilityEventCompat.getContentChangeTypes(accessibilityEvent) : 0;
        if (contentChangeTypes == 0) {
            contentChangeTypes = 0;
        }
        this.mEatenAccessibilityChangeFlags = contentChangeTypes | this.mEatenAccessibilityChangeFlags;
        return true;
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent accessibilityEvent) {
        if (!shouldDeferAccessibilityEvent(accessibilityEvent)) {
            super.sendAccessibilityEventUnchecked(accessibilityEvent);
        }
    }

    @Nullable
    public ItemAnimator getItemAnimator() {
        return this.mItemAnimator;
    }

    /* access modifiers changed from: 0000 */
    public void postAnimationRunner() {
        if (!this.mPostedAnimatorRunner && this.mIsAttached) {
            ViewCompat.postOnAnimation(this, this.mItemAnimatorRunner);
            this.mPostedAnimatorRunner = true;
        }
    }

    private boolean predictiveItemAnimationsEnabled() {
        return this.mItemAnimator != null && this.mLayout.supportsPredictiveItemAnimations();
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            if (this.mDispatchItemsChangedEvent) {
                this.mLayout.onItemsChanged(this);
            }
        }
        if (predictiveItemAnimationsEnabled()) {
            this.mAdapterHelper.preProcess();
        } else {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        }
        boolean z = false;
        boolean z2 = this.mItemsAddedOrRemoved || this.mItemsChanged;
        this.mState.mRunSimpleAnimations = this.mFirstLayoutComplete && this.mItemAnimator != null && (this.mDataSetHasChangedAfterLayout || z2 || this.mLayout.mRequestedSimpleAnimations) && (!this.mDataSetHasChangedAfterLayout || this.mAdapter.hasStableIds());
        State state = this.mState;
        if (state.mRunSimpleAnimations && z2 && !this.mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled()) {
            z = true;
        }
        state.mRunPredictiveAnimations = z;
    }

    /* access modifiers changed from: 0000 */
    public void dispatchLayout() {
        Adapter adapter = this.mAdapter;
        String str = TAG;
        if (adapter == null) {
            Log.e(str, "No adapter attached; skipping layout");
        } else if (this.mLayout == null) {
            Log.e(str, "No layout manager attached; skipping layout");
        } else {
            State state = this.mState;
            state.mIsMeasuring = false;
            if (state.mLayoutStep == 1) {
                dispatchLayoutStep1();
                this.mLayout.setExactMeasureSpecsFrom(this);
                dispatchLayoutStep2();
            } else if (!this.mAdapterHelper.hasUpdates() && this.mLayout.getWidth() == getWidth() && this.mLayout.getHeight() == getHeight()) {
                this.mLayout.setExactMeasureSpecsFrom(this);
            } else {
                this.mLayout.setExactMeasureSpecsFrom(this);
                dispatchLayoutStep2();
            }
            dispatchLayoutStep3();
        }
    }

    private void saveFocusInfo() {
        int i;
        ViewHolder viewHolder = null;
        View focusedChild = (!this.mPreserveFocusAfterLayout || !hasFocus() || this.mAdapter == null) ? null : getFocusedChild();
        if (focusedChild != null) {
            viewHolder = findContainingViewHolder(focusedChild);
        }
        if (viewHolder == null) {
            resetFocusInfo();
            return;
        }
        this.mState.mFocusedItemId = this.mAdapter.hasStableIds() ? viewHolder.getItemId() : -1;
        State state = this.mState;
        if (this.mDataSetHasChangedAfterLayout) {
            i = -1;
        } else if (viewHolder.isRemoved()) {
            i = viewHolder.mOldPosition;
        } else {
            i = viewHolder.getAdapterPosition();
        }
        state.mFocusedItemPosition = i;
        this.mState.mFocusedSubChildId = getDeepestFocusedViewWithId(viewHolder.itemView);
    }

    private void resetFocusInfo() {
        State state = this.mState;
        state.mFocusedItemId = -1;
        state.mFocusedItemPosition = -1;
        state.mFocusedSubChildId = -1;
    }

    @Nullable
    private View findNextViewToFocus() {
        int i = this.mState.mFocusedItemPosition != -1 ? this.mState.mFocusedItemPosition : 0;
        int itemCount = this.mState.getItemCount();
        int i2 = i;
        while (i2 < itemCount) {
            ViewHolder findViewHolderForAdapterPosition = findViewHolderForAdapterPosition(i2);
            if (findViewHolderForAdapterPosition == null) {
                break;
            } else if (findViewHolderForAdapterPosition.itemView.hasFocusable()) {
                return findViewHolderForAdapterPosition.itemView;
            } else {
                i2++;
            }
        }
        int min = Math.min(itemCount, i);
        while (true) {
            min--;
            if (min < 0) {
                return null;
            }
            ViewHolder findViewHolderForAdapterPosition2 = findViewHolderForAdapterPosition(min);
            if (findViewHolderForAdapterPosition2 == null) {
                return null;
            }
            if (findViewHolderForAdapterPosition2.itemView.hasFocusable()) {
                return findViewHolderForAdapterPosition2.itemView;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00af, code lost:
        if (r0.isFocusable() != false) goto L_0x00b3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void recoverFocusFromState() {
        /*
            r6 = this;
            boolean r0 = r6.mPreserveFocusAfterLayout
            if (r0 == 0) goto L_0x00b6
            androidx.recyclerview.widget.RecyclerView$Adapter r0 = r6.mAdapter
            if (r0 == 0) goto L_0x00b6
            boolean r0 = r6.hasFocus()
            if (r0 == 0) goto L_0x00b6
            int r0 = r6.getDescendantFocusability()
            r1 = 393216(0x60000, float:5.51013E-40)
            if (r0 == r1) goto L_0x00b6
            int r0 = r6.getDescendantFocusability()
            r1 = 131072(0x20000, float:1.83671E-40)
            if (r0 != r1) goto L_0x0026
            boolean r0 = r6.isFocused()
            if (r0 == 0) goto L_0x0026
            goto L_0x00b6
        L_0x0026:
            boolean r0 = r6.isFocused()
            if (r0 != 0) goto L_0x0055
            android.view.View r0 = r6.getFocusedChild()
            boolean r1 = IGNORE_DETACHED_FOCUSED_CHILD
            if (r1 == 0) goto L_0x004c
            android.view.ViewParent r1 = r0.getParent()
            if (r1 == 0) goto L_0x0040
            boolean r1 = r0.hasFocus()
            if (r1 != 0) goto L_0x004c
        L_0x0040:
            androidx.recyclerview.widget.ChildHelper r0 = r6.mChildHelper
            int r0 = r0.getChildCount()
            if (r0 != 0) goto L_0x0055
            r6.requestFocus()
            return
        L_0x004c:
            androidx.recyclerview.widget.ChildHelper r1 = r6.mChildHelper
            boolean r0 = r1.isHidden(r0)
            if (r0 != 0) goto L_0x0055
            return
        L_0x0055:
            androidx.recyclerview.widget.RecyclerView$State r0 = r6.mState
            long r0 = r0.mFocusedItemId
            r2 = -1
            r4 = 0
            int r5 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r5 == 0) goto L_0x0071
            androidx.recyclerview.widget.RecyclerView$Adapter r0 = r6.mAdapter
            boolean r0 = r0.hasStableIds()
            if (r0 == 0) goto L_0x0071
            androidx.recyclerview.widget.RecyclerView$State r0 = r6.mState
            long r0 = r0.mFocusedItemId
            androidx.recyclerview.widget.RecyclerView$ViewHolder r0 = r6.findViewHolderForItemId(r0)
            goto L_0x0072
        L_0x0071:
            r0 = r4
        L_0x0072:
            if (r0 == 0) goto L_0x008a
            androidx.recyclerview.widget.ChildHelper r1 = r6.mChildHelper
            android.view.View r5 = r0.itemView
            boolean r1 = r1.isHidden(r5)
            if (r1 != 0) goto L_0x008a
            android.view.View r1 = r0.itemView
            boolean r1 = r1.hasFocusable()
            if (r1 != 0) goto L_0x0087
            goto L_0x008a
        L_0x0087:
            android.view.View r4 = r0.itemView
            goto L_0x0096
        L_0x008a:
            androidx.recyclerview.widget.ChildHelper r0 = r6.mChildHelper
            int r0 = r0.getChildCount()
            if (r0 <= 0) goto L_0x0096
            android.view.View r4 = r6.findNextViewToFocus()
        L_0x0096:
            if (r4 == 0) goto L_0x00b6
            androidx.recyclerview.widget.RecyclerView$State r0 = r6.mState
            int r0 = r0.mFocusedSubChildId
            long r0 = (long) r0
            int r5 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r5 == 0) goto L_0x00b2
            androidx.recyclerview.widget.RecyclerView$State r0 = r6.mState
            int r0 = r0.mFocusedSubChildId
            android.view.View r0 = r4.findViewById(r0)
            if (r0 == 0) goto L_0x00b2
            boolean r1 = r0.isFocusable()
            if (r1 == 0) goto L_0x00b2
            goto L_0x00b3
        L_0x00b2:
            r0 = r4
        L_0x00b3:
            r0.requestFocus()
        L_0x00b6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.recyclerview.widget.RecyclerView.recoverFocusFromState():void");
    }

    private int getDeepestFocusedViewWithId(View view) {
        int id = view.getId();
        while (!view.isFocused() && (view instanceof ViewGroup) && view.hasFocus()) {
            view = ((ViewGroup) view).getFocusedChild();
            if (view.getId() != -1) {
                id = view.getId();
            }
        }
        return id;
    }

    /* access modifiers changed from: 0000 */
    public final void fillRemainingScrollValues(State state) {
        if (getScrollState() == 2) {
            OverScroller overScroller = this.mViewFlinger.mScroller;
            state.mRemainingScrollHorizontal = overScroller.getFinalX() - overScroller.getCurrX();
            state.mRemainingScrollVertical = overScroller.getFinalY() - overScroller.getCurrY();
            return;
        }
        state.mRemainingScrollHorizontal = 0;
        state.mRemainingScrollVertical = 0;
    }

    private void dispatchLayoutStep1() {
        boolean z = true;
        this.mState.assertLayoutStep(1);
        fillRemainingScrollValues(this.mState);
        this.mState.mIsMeasuring = false;
        startInterceptRequestLayout();
        this.mViewInfoStore.clear();
        onEnterLayoutOrScroll();
        processAdapterUpdatesAndSetAnimationFlags();
        saveFocusInfo();
        State state = this.mState;
        if (!state.mRunSimpleAnimations || !this.mItemsChanged) {
            z = false;
        }
        state.mTrackOldChangeHolders = z;
        this.mItemsChanged = false;
        this.mItemsAddedOrRemoved = false;
        State state2 = this.mState;
        state2.mInPreLayout = state2.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        if (this.mState.mRunSimpleAnimations) {
            int childCount = this.mChildHelper.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!childViewHolderInt.shouldIgnore() && (!childViewHolderInt.isInvalid() || this.mAdapter.hasStableIds())) {
                    this.mViewInfoStore.addToPreLayout(childViewHolderInt, this.mItemAnimator.recordPreLayoutInformation(this.mState, childViewHolderInt, ItemAnimator.buildAdapterChangeFlagsForAnimations(childViewHolderInt), childViewHolderInt.getUnmodifiedPayloads()));
                    if (this.mState.mTrackOldChangeHolders && childViewHolderInt.isUpdated() && !childViewHolderInt.isRemoved() && !childViewHolderInt.shouldIgnore() && !childViewHolderInt.isInvalid()) {
                        this.mViewInfoStore.addToOldChangeHolders(getChangedHolderKey(childViewHolderInt), childViewHolderInt);
                    }
                }
            }
        }
        if (this.mState.mRunPredictiveAnimations) {
            saveOldPositions();
            boolean z2 = this.mState.mStructureChanged;
            State state3 = this.mState;
            state3.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, state3);
            this.mState.mStructureChanged = z2;
            for (int i2 = 0; i2 < this.mChildHelper.getChildCount(); i2++) {
                ViewHolder childViewHolderInt2 = getChildViewHolderInt(this.mChildHelper.getChildAt(i2));
                if (!childViewHolderInt2.shouldIgnore() && !this.mViewInfoStore.isInPreLayout(childViewHolderInt2)) {
                    int buildAdapterChangeFlagsForAnimations = ItemAnimator.buildAdapterChangeFlagsForAnimations(childViewHolderInt2);
                    boolean hasAnyOfTheFlags = childViewHolderInt2.hasAnyOfTheFlags(8192);
                    if (!hasAnyOfTheFlags) {
                        buildAdapterChangeFlagsForAnimations |= 4096;
                    }
                    ItemHolderInfo recordPreLayoutInformation = this.mItemAnimator.recordPreLayoutInformation(this.mState, childViewHolderInt2, buildAdapterChangeFlagsForAnimations, childViewHolderInt2.getUnmodifiedPayloads());
                    if (hasAnyOfTheFlags) {
                        recordAnimationInfoIfBouncedHiddenView(childViewHolderInt2, recordPreLayoutInformation);
                    } else {
                        this.mViewInfoStore.addToAppearedInPreLayoutHolders(childViewHolderInt2, recordPreLayoutInformation);
                    }
                }
            }
            clearOldPositions();
        } else {
            clearOldPositions();
        }
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
        this.mState.mLayoutStep = 2;
    }

    private void dispatchLayoutStep2() {
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();
        this.mState.assertLayoutStep(6);
        this.mAdapterHelper.consumeUpdatesInOnePass();
        this.mState.mItemCount = this.mAdapter.getItemCount();
        State state = this.mState;
        state.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        state.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, state);
        State state2 = this.mState;
        state2.mStructureChanged = false;
        this.mPendingSavedState = null;
        state2.mRunSimpleAnimations = state2.mRunSimpleAnimations && this.mItemAnimator != null;
        this.mState.mLayoutStep = 4;
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
    }

    private void dispatchLayoutStep3() {
        this.mState.assertLayoutStep(4);
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();
        State state = this.mState;
        state.mLayoutStep = 1;
        if (state.mRunSimpleAnimations) {
            for (int childCount = this.mChildHelper.getChildCount() - 1; childCount >= 0; childCount--) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(childCount));
                if (!childViewHolderInt.shouldIgnore()) {
                    long changedHolderKey = getChangedHolderKey(childViewHolderInt);
                    ItemHolderInfo recordPostLayoutInformation = this.mItemAnimator.recordPostLayoutInformation(this.mState, childViewHolderInt);
                    ViewHolder fromOldChangeHolders = this.mViewInfoStore.getFromOldChangeHolders(changedHolderKey);
                    if (fromOldChangeHolders == null || fromOldChangeHolders.shouldIgnore()) {
                        this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                    } else {
                        boolean isDisappearing = this.mViewInfoStore.isDisappearing(fromOldChangeHolders);
                        boolean isDisappearing2 = this.mViewInfoStore.isDisappearing(childViewHolderInt);
                        if (!isDisappearing || fromOldChangeHolders != childViewHolderInt) {
                            ItemHolderInfo popFromPreLayout = this.mViewInfoStore.popFromPreLayout(fromOldChangeHolders);
                            this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                            ItemHolderInfo popFromPostLayout = this.mViewInfoStore.popFromPostLayout(childViewHolderInt);
                            if (popFromPreLayout == null) {
                                handleMissingPreInfoForChangeError(changedHolderKey, childViewHolderInt, fromOldChangeHolders);
                            } else {
                                animateChange(fromOldChangeHolders, childViewHolderInt, popFromPreLayout, popFromPostLayout, isDisappearing, isDisappearing2);
                            }
                        } else {
                            this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                        }
                    }
                }
            }
            this.mViewInfoStore.process(this.mViewInfoProcessCallback);
        }
        this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        State state2 = this.mState;
        state2.mPreviousLayoutItemCount = state2.mItemCount;
        this.mDataSetHasChangedAfterLayout = false;
        this.mDispatchItemsChangedEvent = false;
        State state3 = this.mState;
        state3.mRunSimpleAnimations = false;
        state3.mRunPredictiveAnimations = false;
        this.mLayout.mRequestedSimpleAnimations = false;
        if (this.mRecycler.mChangedScrap != null) {
            this.mRecycler.mChangedScrap.clear();
        }
        if (this.mLayout.mPrefetchMaxObservedInInitialPrefetch) {
            LayoutManager layoutManager = this.mLayout;
            layoutManager.mPrefetchMaxCountObserved = 0;
            layoutManager.mPrefetchMaxObservedInInitialPrefetch = false;
            this.mRecycler.updateViewCacheSize();
        }
        this.mLayout.onLayoutCompleted(this.mState);
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
        this.mViewInfoStore.clear();
        int[] iArr = this.mMinMaxLayoutPositions;
        if (didChildRangeChange(iArr[0], iArr[1])) {
            dispatchOnScrolled(0, 0);
        }
        recoverFocusFromState();
        resetFocusInfo();
    }

    private void handleMissingPreInfoForChangeError(long j, ViewHolder viewHolder, ViewHolder viewHolder2) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (childViewHolderInt != viewHolder && getChangedHolderKey(childViewHolderInt) == j) {
                Adapter adapter = this.mAdapter;
                String str = " \n View Holder 2:";
                if (adapter == null || !adapter.hasStableIds()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Two different ViewHolders have the same change ID. This might happen due to inconsistent Adapter update events or if the LayoutManager lays out the same View multiple times.\n ViewHolder 1:");
                    sb.append(childViewHolderInt);
                    sb.append(str);
                    sb.append(viewHolder);
                    sb.append(exceptionLabel());
                    throw new IllegalStateException(sb.toString());
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Two different ViewHolders have the same stable ID. Stable IDs in your adapter MUST BE unique and SHOULD NOT change.\n ViewHolder 1:");
                sb2.append(childViewHolderInt);
                sb2.append(str);
                sb2.append(viewHolder);
                sb2.append(exceptionLabel());
                throw new IllegalStateException(sb2.toString());
            }
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("Problem while matching changed view holders with the newones. The pre-layout information for the change holder ");
        sb3.append(viewHolder2);
        sb3.append(" cannot be found but it is necessary for ");
        sb3.append(viewHolder);
        sb3.append(exceptionLabel());
        Log.e(TAG, sb3.toString());
    }

    /* access modifiers changed from: 0000 */
    public void recordAnimationInfoIfBouncedHiddenView(ViewHolder viewHolder, ItemHolderInfo itemHolderInfo) {
        viewHolder.setFlags(0, 8192);
        if (this.mState.mTrackOldChangeHolders && viewHolder.isUpdated() && !viewHolder.isRemoved() && !viewHolder.shouldIgnore()) {
            this.mViewInfoStore.addToOldChangeHolders(getChangedHolderKey(viewHolder), viewHolder);
        }
        this.mViewInfoStore.addToPreLayout(viewHolder, itemHolderInfo);
    }

    private void findMinMaxChildLayoutPositions(int[] iArr) {
        int childCount = this.mChildHelper.getChildCount();
        if (childCount == 0) {
            iArr[0] = -1;
            iArr[1] = -1;
            return;
        }
        int i = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        int i2 = Integer.MIN_VALUE;
        for (int i3 = 0; i3 < childCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i3));
            if (!childViewHolderInt.shouldIgnore()) {
                int layoutPosition = childViewHolderInt.getLayoutPosition();
                if (layoutPosition < i) {
                    i = layoutPosition;
                }
                if (layoutPosition > i2) {
                    i2 = layoutPosition;
                }
            }
        }
        iArr[0] = i;
        iArr[1] = i2;
    }

    private boolean didChildRangeChange(int i, int i2) {
        findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        int[] iArr = this.mMinMaxLayoutPositions;
        return (iArr[0] == i && iArr[1] == i2) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void removeDetachedView(View view, boolean z) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            if (childViewHolderInt.isTmpDetached()) {
                childViewHolderInt.clearTmpDetachFlag();
            } else if (!childViewHolderInt.shouldIgnore()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Called removeDetachedView with a view which is not flagged as tmp detached.");
                sb.append(childViewHolderInt);
                sb.append(exceptionLabel());
                throw new IllegalArgumentException(sb.toString());
            }
        }
        view.clearAnimation();
        dispatchChildDetached(view);
        super.removeDetachedView(view, z);
    }

    /* access modifiers changed from: 0000 */
    public long getChangedHolderKey(ViewHolder viewHolder) {
        return this.mAdapter.hasStableIds() ? viewHolder.getItemId() : (long) viewHolder.mPosition;
    }

    /* access modifiers changed from: 0000 */
    public void animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2) {
        viewHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateAppearance(viewHolder, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    /* access modifiers changed from: 0000 */
    public void animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @Nullable ItemHolderInfo itemHolderInfo2) {
        addAnimatingView(viewHolder);
        viewHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateDisappearance(viewHolder, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    private void animateChange(@NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2, boolean z, boolean z2) {
        viewHolder.setIsRecyclable(false);
        if (z) {
            addAnimatingView(viewHolder);
        }
        if (viewHolder != viewHolder2) {
            if (z2) {
                addAnimatingView(viewHolder2);
            }
            viewHolder.mShadowedHolder = viewHolder2;
            addAnimatingView(viewHolder);
            this.mRecycler.unscrapView(viewHolder);
            viewHolder2.setIsRecyclable(false);
            viewHolder2.mShadowingHolder = viewHolder;
        }
        if (this.mItemAnimator.animateChange(viewHolder, viewHolder2, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        TraceCompat.beginSection(TRACE_ON_LAYOUT_TAG);
        dispatchLayout();
        TraceCompat.endSection();
        this.mFirstLayoutComplete = true;
    }

    public void requestLayout() {
        if (this.mInterceptRequestLayoutDepth != 0 || this.mLayoutFrozen) {
            this.mLayoutWasDefered = true;
        } else {
            super.requestLayout();
        }
    }

    /* access modifiers changed from: 0000 */
    public void markItemDecorInsetsDirty() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ((LayoutParams) this.mChildHelper.getUnfilteredChildAt(i).getLayoutParams()).mInsetsDirty = true;
        }
        this.mRecycler.markItemDecorInsetsDirty();
    }

    public void draw(Canvas canvas) {
        boolean z;
        boolean z2;
        super.draw(canvas);
        int size = this.mItemDecorations.size();
        boolean z3 = false;
        for (int i = 0; i < size; i++) {
            ((ItemDecoration) this.mItemDecorations.get(i)).onDrawOver(canvas, this, this.mState);
        }
        EdgeEffect edgeEffect = this.mLeftGlow;
        if (edgeEffect == null || edgeEffect.isFinished()) {
            z = false;
        } else {
            int save = canvas.save();
            int paddingBottom = this.mClipToPadding ? getPaddingBottom() : 0;
            canvas.rotate(270.0f);
            canvas.translate((float) ((-getHeight()) + paddingBottom), 0.0f);
            EdgeEffect edgeEffect2 = this.mLeftGlow;
            z = edgeEffect2 != null && edgeEffect2.draw(canvas);
            canvas.restoreToCount(save);
        }
        EdgeEffect edgeEffect3 = this.mTopGlow;
        if (edgeEffect3 != null && !edgeEffect3.isFinished()) {
            int save2 = canvas.save();
            if (this.mClipToPadding) {
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            }
            EdgeEffect edgeEffect4 = this.mTopGlow;
            z |= edgeEffect4 != null && edgeEffect4.draw(canvas);
            canvas.restoreToCount(save2);
        }
        EdgeEffect edgeEffect5 = this.mRightGlow;
        if (edgeEffect5 != null && !edgeEffect5.isFinished()) {
            int save3 = canvas.save();
            int width = getWidth();
            int paddingTop = this.mClipToPadding ? getPaddingTop() : 0;
            canvas.rotate(90.0f);
            canvas.translate((float) (-paddingTop), (float) (-width));
            EdgeEffect edgeEffect6 = this.mRightGlow;
            z |= edgeEffect6 != null && edgeEffect6.draw(canvas);
            canvas.restoreToCount(save3);
        }
        EdgeEffect edgeEffect7 = this.mBottomGlow;
        if (edgeEffect7 == null || edgeEffect7.isFinished()) {
            z2 = z;
        } else {
            int save4 = canvas.save();
            canvas.rotate(180.0f);
            if (this.mClipToPadding) {
                canvas.translate((float) ((-getWidth()) + getPaddingRight()), (float) ((-getHeight()) + getPaddingBottom()));
            } else {
                canvas.translate((float) (-getWidth()), (float) (-getHeight()));
            }
            EdgeEffect edgeEffect8 = this.mBottomGlow;
            if (edgeEffect8 != null && edgeEffect8.draw(canvas)) {
                z3 = true;
            }
            z2 = z3 | z;
            canvas.restoreToCount(save4);
        }
        if (!z2 && this.mItemAnimator != null && this.mItemDecorations.size() > 0 && this.mItemAnimator.isRunning()) {
            z2 = true;
        }
        if (z2) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            ((ItemDecoration) this.mItemDecorations.get(i)).onDraw(canvas, this, this.mState);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && this.mLayout.checkLayoutParams((LayoutParams) layoutParams);
    }

    /* access modifiers changed from: protected */
    public android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateDefaultLayoutParams();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RecyclerView has no LayoutManager");
        sb.append(exceptionLabel());
        throw new IllegalStateException(sb.toString());
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateLayoutParams(getContext(), attributeSet);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RecyclerView has no LayoutManager");
        sb.append(exceptionLabel());
        throw new IllegalStateException(sb.toString());
    }

    /* access modifiers changed from: protected */
    public android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateLayoutParams(layoutParams);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RecyclerView has no LayoutManager");
        sb.append(exceptionLabel());
        throw new IllegalStateException(sb.toString());
    }

    public boolean isAnimating() {
        ItemAnimator itemAnimator = this.mItemAnimator;
        return itemAnimator != null && itemAnimator.isRunning();
    }

    /* access modifiers changed from: 0000 */
    public void saveOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.saveOldPosition();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void clearOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.clearOldPosition();
            }
        }
        this.mRecycler.clearOldPositions();
    }

    /* access modifiers changed from: 0000 */
    public void offsetPositionRecordsForMove(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        if (i < i2) {
            i5 = i;
            i4 = i2;
            i3 = -1;
        } else {
            i4 = i;
            i5 = i2;
            i3 = 1;
        }
        for (int i6 = 0; i6 < unfilteredChildCount; i6++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i6));
            if (childViewHolderInt != null && childViewHolderInt.mPosition >= i5 && childViewHolderInt.mPosition <= i4) {
                if (childViewHolderInt.mPosition == i) {
                    childViewHolderInt.offsetPosition(i2 - i, false);
                } else {
                    childViewHolderInt.offsetPosition(i3, false);
                }
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForMove(i, i2);
        requestLayout();
    }

    /* access modifiers changed from: 0000 */
    public void offsetPositionRecordsForInsert(int i, int i2) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i3 = 0; i3 < unfilteredChildCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i3));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i) {
                childViewHolderInt.offsetPosition(i2, false);
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForInsert(i, i2);
        requestLayout();
    }

    /* access modifiers changed from: 0000 */
    public void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
        int i3 = i + i2;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i4 = 0; i4 < unfilteredChildCount; i4++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i4));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                if (childViewHolderInt.mPosition >= i3) {
                    childViewHolderInt.offsetPosition(-i2, z);
                    this.mState.mStructureChanged = true;
                } else if (childViewHolderInt.mPosition >= i) {
                    childViewHolderInt.flagRemovedAndOffsetPosition(i - 1, -i2, z);
                    this.mState.mStructureChanged = true;
                }
            }
        }
        this.mRecycler.offsetPositionRecordsForRemove(i, i2, z);
        requestLayout();
    }

    /* access modifiers changed from: 0000 */
    public void viewRangeUpdate(int i, int i2, Object obj) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        int i3 = i + i2;
        for (int i4 = 0; i4 < unfilteredChildCount; i4++) {
            View unfilteredChildAt = this.mChildHelper.getUnfilteredChildAt(i4);
            ViewHolder childViewHolderInt = getChildViewHolderInt(unfilteredChildAt);
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i && childViewHolderInt.mPosition < i3) {
                childViewHolderInt.addFlags(2);
                childViewHolderInt.addChangePayload(obj);
                ((LayoutParams) unfilteredChildAt.getLayoutParams()).mInsetsDirty = true;
            }
        }
        this.mRecycler.viewRangeUpdate(i, i2);
    }

    /* access modifiers changed from: 0000 */
    public boolean canReuseUpdatedViewHolder(ViewHolder viewHolder) {
        ItemAnimator itemAnimator = this.mItemAnimator;
        return itemAnimator == null || itemAnimator.canReuseUpdatedViewHolder(viewHolder, viewHolder.getUnmodifiedPayloads());
    }

    /* access modifiers changed from: 0000 */
    public void processDataSetCompletelyChanged(boolean z) {
        this.mDispatchItemsChangedEvent = z | this.mDispatchItemsChangedEvent;
        this.mDataSetHasChangedAfterLayout = true;
        markKnownViewsInvalid();
    }

    /* access modifiers changed from: 0000 */
    public void markKnownViewsInvalid() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.addFlags(6);
            }
        }
        markItemDecorInsetsDirty();
        this.mRecycler.markKnownViewsInvalid();
    }

    public void invalidateItemDecorations() {
        if (this.mItemDecorations.size() != 0) {
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager != null) {
                layoutManager.assertNotInLayoutOrScroll("Cannot invalidate item decorations during a scroll or layout");
            }
            markItemDecorInsetsDirty();
            requestLayout();
        }
    }

    public boolean getPreserveFocusAfterLayout() {
        return this.mPreserveFocusAfterLayout;
    }

    public void setPreserveFocusAfterLayout(boolean z) {
        this.mPreserveFocusAfterLayout = z;
    }

    public ViewHolder getChildViewHolder(@NonNull View view) {
        ViewParent parent = view.getParent();
        if (parent == null || parent == this) {
            return getChildViewHolderInt(view);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("View ");
        sb.append(view);
        sb.append(" is not a direct child of ");
        sb.append(this);
        throw new IllegalArgumentException(sb.toString());
    }

    @Nullable
    public View findContainingItemView(@NonNull View view) {
        ViewParent parent = view.getParent();
        while (parent != null && parent != this && (parent instanceof View)) {
            view = (View) parent;
            parent = view.getParent();
        }
        if (parent == this) {
            return view;
        }
        return null;
    }

    @Nullable
    public ViewHolder findContainingViewHolder(@NonNull View view) {
        View findContainingItemView = findContainingItemView(view);
        if (findContainingItemView == null) {
            return null;
        }
        return getChildViewHolder(findContainingItemView);
    }

    static ViewHolder getChildViewHolderInt(View view) {
        if (view == null) {
            return null;
        }
        return ((LayoutParams) view.getLayoutParams()).mViewHolder;
    }

    @Deprecated
    public int getChildPosition(@NonNull View view) {
        return getChildAdapterPosition(view);
    }

    public int getChildAdapterPosition(@NonNull View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            return childViewHolderInt.getAdapterPosition();
        }
        return -1;
    }

    public int getChildLayoutPosition(@NonNull View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            return childViewHolderInt.getLayoutPosition();
        }
        return -1;
    }

    public long getChildItemId(@NonNull View view) {
        Adapter adapter = this.mAdapter;
        if (adapter == null || !adapter.hasStableIds()) {
            return -1;
        }
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            return childViewHolderInt.getItemId();
        }
        return -1;
    }

    @Deprecated
    @Nullable
    public ViewHolder findViewHolderForPosition(int i) {
        return findViewHolderForPosition(i, false);
    }

    @Nullable
    public ViewHolder findViewHolderForLayoutPosition(int i) {
        return findViewHolderForPosition(i, false);
    }

    @Nullable
    public ViewHolder findViewHolderForAdapterPosition(int i) {
        ViewHolder viewHolder = null;
        if (this.mDataSetHasChangedAfterLayout) {
            return null;
        }
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i2 = 0; i2 < unfilteredChildCount; i2++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i2));
            if (childViewHolderInt != null && !childViewHolderInt.isRemoved() && getAdapterPositionFor(childViewHolderInt) == i) {
                if (!this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                    return childViewHolderInt;
                }
                viewHolder = childViewHolderInt;
            }
        }
        return viewHolder;
    }

    /* access modifiers changed from: 0000 */
    @Nullable
    public ViewHolder findViewHolderForPosition(int i, boolean z) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        ViewHolder viewHolder = null;
        for (int i2 = 0; i2 < unfilteredChildCount; i2++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i2));
            if (childViewHolderInt != null && !childViewHolderInt.isRemoved()) {
                if (z) {
                    if (childViewHolderInt.mPosition != i) {
                        continue;
                    }
                } else if (childViewHolderInt.getLayoutPosition() != i) {
                    continue;
                }
                if (!this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                    return childViewHolderInt;
                }
                viewHolder = childViewHolderInt;
            }
        }
        return viewHolder;
    }

    public ViewHolder findViewHolderForItemId(long j) {
        Adapter adapter = this.mAdapter;
        ViewHolder viewHolder = null;
        if (adapter != null && adapter.hasStableIds()) {
            int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
            for (int i = 0; i < unfilteredChildCount; i++) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
                if (childViewHolderInt != null && !childViewHolderInt.isRemoved() && childViewHolderInt.getItemId() == j) {
                    if (!this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                        return childViewHolderInt;
                    }
                    viewHolder = childViewHolderInt;
                }
            }
        }
        return viewHolder;
    }

    @Nullable
    public View findChildViewUnder(float f, float f2) {
        for (int childCount = this.mChildHelper.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mChildHelper.getChildAt(childCount);
            float translationX = childAt.getTranslationX();
            float translationY = childAt.getTranslationY();
            if (f >= ((float) childAt.getLeft()) + translationX && f <= ((float) childAt.getRight()) + translationX && f2 >= ((float) childAt.getTop()) + translationY && f2 <= ((float) childAt.getBottom()) + translationY) {
                return childAt;
            }
        }
        return null;
    }

    public boolean drawChild(Canvas canvas, View view, long j) {
        return super.drawChild(canvas, view, j);
    }

    public void offsetChildrenVertical(@Px int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetTopAndBottom(i);
        }
    }

    public void offsetChildrenHorizontal(@Px int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetLeftAndRight(i);
        }
    }

    public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect rect) {
        getDecoratedBoundsWithMarginsInt(view, rect);
    }

    static void getDecoratedBoundsWithMarginsInt(View view, Rect rect) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        Rect rect2 = layoutParams.mDecorInsets;
        rect.set((view.getLeft() - rect2.left) - layoutParams.leftMargin, (view.getTop() - rect2.top) - layoutParams.topMargin, view.getRight() + rect2.right + layoutParams.rightMargin, view.getBottom() + rect2.bottom + layoutParams.bottomMargin);
    }

    /* access modifiers changed from: 0000 */
    public Rect getItemDecorInsetsForChild(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (!layoutParams.mInsetsDirty) {
            return layoutParams.mDecorInsets;
        }
        if (this.mState.isPreLayout() && (layoutParams.isItemChanged() || layoutParams.isViewInvalid())) {
            return layoutParams.mDecorInsets;
        }
        Rect rect = layoutParams.mDecorInsets;
        rect.set(0, 0, 0, 0);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            this.mTempRect.set(0, 0, 0, 0);
            ((ItemDecoration) this.mItemDecorations.get(i)).getItemOffsets(this.mTempRect, view, this, this.mState);
            rect.left += this.mTempRect.left;
            rect.top += this.mTempRect.top;
            rect.right += this.mTempRect.right;
            rect.bottom += this.mTempRect.bottom;
        }
        layoutParams.mInsetsDirty = false;
        return rect;
    }

    /* access modifiers changed from: 0000 */
    public void dispatchOnScrolled(int i, int i2) {
        this.mDispatchScrollCounter++;
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        onScrollChanged(scrollX, scrollY, scrollX, scrollY);
        onScrolled(i, i2);
        OnScrollListener onScrollListener = this.mScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScrolled(this, i, i2);
        }
        List<OnScrollListener> list = this.mScrollListeners;
        if (list != null) {
            for (int size = list.size() - 1; size >= 0; size--) {
                ((OnScrollListener) this.mScrollListeners.get(size)).onScrolled(this, i, i2);
            }
        }
        this.mDispatchScrollCounter--;
    }

    /* access modifiers changed from: 0000 */
    public void dispatchOnScrollStateChanged(int i) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.onScrollStateChanged(i);
        }
        onScrollStateChanged(i);
        OnScrollListener onScrollListener = this.mScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(this, i);
        }
        List<OnScrollListener> list = this.mScrollListeners;
        if (list != null) {
            for (int size = list.size() - 1; size >= 0; size--) {
                ((OnScrollListener) this.mScrollListeners.get(size)).onScrollStateChanged(this, i);
            }
        }
    }

    public boolean hasPendingAdapterUpdates() {
        return !this.mFirstLayoutComplete || this.mDataSetHasChangedAfterLayout || this.mAdapterHelper.hasPendingUpdates();
    }

    /* access modifiers changed from: 0000 */
    public void repositionShadowingViews() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mChildHelper.getChildAt(i);
            ViewHolder childViewHolder = getChildViewHolder(childAt);
            if (!(childViewHolder == null || childViewHolder.mShadowingHolder == null)) {
                View view = childViewHolder.mShadowingHolder.itemView;
                int left = childAt.getLeft();
                int top = childAt.getTop();
                if (left != view.getLeft() || top != view.getTop()) {
                    view.layout(left, top, view.getWidth() + left, view.getHeight() + top);
                }
            }
        }
    }

    @Nullable
    static RecyclerView findNestedRecyclerView(@NonNull View view) {
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        if (view instanceof RecyclerView) {
            return (RecyclerView) view;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RecyclerView findNestedRecyclerView = findNestedRecyclerView(viewGroup.getChildAt(i));
            if (findNestedRecyclerView != null) {
                return findNestedRecyclerView;
            }
        }
        return null;
    }

    static void clearNestedRecyclerViewIfNotNested(@NonNull ViewHolder viewHolder) {
        if (viewHolder.mNestedRecyclerView != null) {
            View view = (View) viewHolder.mNestedRecyclerView.get();
            while (view != null) {
                if (view != viewHolder.itemView) {
                    ViewParent parent = view.getParent();
                    view = parent instanceof View ? (View) parent : null;
                } else {
                    return;
                }
            }
            viewHolder.mNestedRecyclerView = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public long getNanoTime() {
        if (ALLOW_THREAD_GAP_WORK) {
            return System.nanoTime();
        }
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public void dispatchChildDetached(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        onChildDetachedFromWindow(view);
        Adapter adapter = this.mAdapter;
        if (!(adapter == null || childViewHolderInt == null)) {
            adapter.onViewDetachedFromWindow(childViewHolderInt);
        }
        List<OnChildAttachStateChangeListener> list = this.mOnChildAttachStateListeners;
        if (list != null) {
            for (int size = list.size() - 1; size >= 0; size--) {
                ((OnChildAttachStateChangeListener) this.mOnChildAttachStateListeners.get(size)).onChildViewDetachedFromWindow(view);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void dispatchChildAttached(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        onChildAttachedToWindow(view);
        Adapter adapter = this.mAdapter;
        if (!(adapter == null || childViewHolderInt == null)) {
            adapter.onViewAttachedToWindow(childViewHolderInt);
        }
        List<OnChildAttachStateChangeListener> list = this.mOnChildAttachStateListeners;
        if (list != null) {
            for (int size = list.size() - 1; size >= 0; size--) {
                ((OnChildAttachStateChangeListener) this.mOnChildAttachStateListeners.get(size)).onChildViewAttachedToWindow(view);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    @VisibleForTesting
    public boolean setChildImportantForAccessibilityInternal(ViewHolder viewHolder, int i) {
        if (isComputingLayout()) {
            viewHolder.mPendingAccessibilityState = i;
            this.mPendingAccessibilityImportanceChange.add(viewHolder);
            return false;
        }
        ViewCompat.setImportantForAccessibility(viewHolder.itemView, i);
        return true;
    }

    /* access modifiers changed from: 0000 */
    public void dispatchPendingImportantForAccessibilityChanges() {
        for (int size = this.mPendingAccessibilityImportanceChange.size() - 1; size >= 0; size--) {
            ViewHolder viewHolder = (ViewHolder) this.mPendingAccessibilityImportanceChange.get(size);
            if (viewHolder.itemView.getParent() == this && !viewHolder.shouldIgnore()) {
                int i = viewHolder.mPendingAccessibilityState;
                if (i != -1) {
                    ViewCompat.setImportantForAccessibility(viewHolder.itemView, i);
                    viewHolder.mPendingAccessibilityState = -1;
                }
            }
        }
        this.mPendingAccessibilityImportanceChange.clear();
    }

    /* access modifiers changed from: 0000 */
    public int getAdapterPositionFor(ViewHolder viewHolder) {
        if (viewHolder.hasAnyOfTheFlags(524) || !viewHolder.isBound()) {
            return -1;
        }
        return this.mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
    }

    /* access modifiers changed from: 0000 */
    @VisibleForTesting
    public void initFastScroller(StateListDrawable stateListDrawable, Drawable drawable, StateListDrawable stateListDrawable2, Drawable drawable2) {
        if (stateListDrawable == null || drawable == null || stateListDrawable2 == null || drawable2 == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Trying to set fast scroller without both required drawables.");
            sb.append(exceptionLabel());
            throw new IllegalArgumentException(sb.toString());
        }
        Resources resources = getContext().getResources();
        new FastScroller(this, stateListDrawable, drawable, stateListDrawable2, drawable2, resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness), resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range), resources.getDimensionPixelOffset(R.dimen.fastscroll_margin));
    }

    public void setNestedScrollingEnabled(boolean z) {
        getScrollingChildHelper().setNestedScrollingEnabled(z);
    }

    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int i) {
        return getScrollingChildHelper().startNestedScroll(i);
    }

    public boolean startNestedScroll(int i, int i2) {
        return getScrollingChildHelper().startNestedScroll(i, i2);
    }

    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    public void stopNestedScroll(int i) {
        getScrollingChildHelper().stopNestedScroll(i);
    }

    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    public boolean hasNestedScrollingParent(int i) {
        return getScrollingChildHelper().hasNestedScrollingParent(i);
    }

    public boolean dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr) {
        return getScrollingChildHelper().dispatchNestedScroll(i, i2, i3, i4, iArr);
    }

    public boolean dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr, int i5) {
        return getScrollingChildHelper().dispatchNestedScroll(i, i2, i3, i4, iArr, i5);
    }

    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2) {
        return getScrollingChildHelper().dispatchNestedPreScroll(i, i2, iArr, iArr2);
    }

    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2, int i3) {
        return getScrollingChildHelper().dispatchNestedPreScroll(i, i2, iArr, iArr2, i3);
    }

    public boolean dispatchNestedFling(float f, float f2, boolean z) {
        return getScrollingChildHelper().dispatchNestedFling(f, f2, z);
    }

    public boolean dispatchNestedPreFling(float f, float f2) {
        return getScrollingChildHelper().dispatchNestedPreFling(f, f2);
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int i, int i2) {
        ChildDrawingOrderCallback childDrawingOrderCallback = this.mChildDrawingOrderCallback;
        if (childDrawingOrderCallback == null) {
            return super.getChildDrawingOrder(i, i2);
        }
        return childDrawingOrderCallback.onGetChildDrawingOrder(i, i2);
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (this.mScrollingChildHelper == null) {
            this.mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return this.mScrollingChildHelper;
    }
}
