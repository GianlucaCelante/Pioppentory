package it.pioppi.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD = 10;
    private static final int SWIPE_VELOCITY_THRESHOLD = 40;

    public interface OnSwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }

    private final OnSwipeListener swipeListener;

    public SwipeGestureListener(OnSwipeListener listener) {
        this.swipeListener = listener;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        // Ritorna true per assicurare che gli eventi vengano gestiti
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
                if (swipeListener != null) {
                    swipeListener.onSwipeRight();
                }
            } else {
                if (swipeListener != null) {
                    swipeListener.onSwipeLeft();
                }
            }
            return true;
        }
        return false;
    }
}
