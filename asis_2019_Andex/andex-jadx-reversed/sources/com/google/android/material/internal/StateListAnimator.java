package com.google.android.material.internal;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.StateSet;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.ArrayList;

@RestrictTo({Scope.LIBRARY_GROUP})
public final class StateListAnimator {
    private final AnimatorListener animationListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            if (StateListAnimator.this.runningAnimator == animator) {
                StateListAnimator.this.runningAnimator = null;
            }
        }
    };
    private Tuple lastMatch = null;
    ValueAnimator runningAnimator = null;
    private final ArrayList<Tuple> tuples = new ArrayList<>();

    static class Tuple {
        final ValueAnimator animator;
        final int[] specs;

        Tuple(int[] iArr, ValueAnimator valueAnimator) {
            this.specs = iArr;
            this.animator = valueAnimator;
        }
    }

    public void addState(int[] iArr, ValueAnimator valueAnimator) {
        Tuple tuple = new Tuple(iArr, valueAnimator);
        valueAnimator.addListener(this.animationListener);
        this.tuples.add(tuple);
    }

    public void setState(int[] iArr) {
        Tuple tuple;
        int size = this.tuples.size();
        int i = 0;
        while (true) {
            if (i >= size) {
                tuple = null;
                break;
            }
            tuple = (Tuple) this.tuples.get(i);
            if (StateSet.stateSetMatches(tuple.specs, iArr)) {
                break;
            }
            i++;
        }
        Tuple tuple2 = this.lastMatch;
        if (tuple != tuple2) {
            if (tuple2 != null) {
                cancel();
            }
            this.lastMatch = tuple;
            if (tuple != null) {
                start(tuple);
            }
        }
    }

    private void start(Tuple tuple) {
        this.runningAnimator = tuple.animator;
        this.runningAnimator.start();
    }

    private void cancel() {
        ValueAnimator valueAnimator = this.runningAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.runningAnimator = null;
        }
    }

    public void jumpToCurrentState() {
        ValueAnimator valueAnimator = this.runningAnimator;
        if (valueAnimator != null) {
            valueAnimator.end();
            this.runningAnimator = null;
        }
    }
}
