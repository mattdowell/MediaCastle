package org.dowell.mediacastle.util;

import android.app.Activity;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * @author Matt
 * @from http://android-journey.blogspot.com/2010/01/android-gestures.html
 * <p/>
 * Galaxy Nexus: 4.65″ HD Super AMOLED (1280×720 resolution) – 316ppi
 */
public class SimpleGestureFilter extends SimpleOnGestureListener {

    public final static int SWIPE_UP = 1;
    public final static int SWIPE_DOWN = 2;
    public final static int SWIPE_LEFT = 3;
    public final static int SWIPE_RIGHT = 4;

    public final static int MODE_TRANSPARENT = 0;
    public final static int MODE_SOLID = 1;
    public final static int MODE_DYNAMIC = 2;

    private final static int ACTION_FAKE = -13; // just an unlikely number

    // Min = 10% Max = 40%
    private double swipeDistanceMinPercent = 0.1;
    private double swipeDistanceMaxPercent = 0.8;

    private int swipe_Min_X_Distance;
    private int swipe_Max_X_Distance;

    private int swipe_Min_Y_Distance;
    private int swipe_Max_Y_Distance;

    // pixels per second
    private int swipe_Min_Velocity = 200;

    private int mode = MODE_DYNAMIC;
    private boolean running = true;
    private boolean tapIndicator = false;

    private Activity context;
    private GestureDetector detector;
    private SimpleGestureListener listener;

    public SimpleGestureFilter(Activity context, SimpleGestureListener sgl) {
        this.context = context;
        this.detector = new GestureDetector(context, this);
        this.listener = sgl;
        initScreenDims();
    }

    /**
     * Initialize the pixel lengths for swipes, based upon a percentage
     * of the screen size. Since there is a large difference between a HD
     * phone and an older phone, we have to use percentage.
     */
    private void initScreenDims() {
        Display display = context.getWindowManager().getDefaultDisplay();

        // X = Width. Is this true in landcape mode? hope so
        int width = display.getWidth();
        swipe_Min_X_Distance = (int) (width * swipeDistanceMinPercent);
        swipe_Max_X_Distance = (int) (width * swipeDistanceMaxPercent);

        // Y = Height. Landscape also?
        int height = display.getHeight();
        swipe_Min_Y_Distance = (int) (height * swipeDistanceMinPercent);
        swipe_Max_Y_Distance = (int) (height * swipeDistanceMaxPercent);

    }

    public void onTouchEvent(MotionEvent event) {

        if (!this.running)
            return;

        boolean result = this.detector.onTouchEvent(event);

        if (this.mode == MODE_SOLID)
            event.setAction(MotionEvent.ACTION_CANCEL);
        else if (this.mode == MODE_DYNAMIC) {

            if (event.getAction() == ACTION_FAKE)
                event.setAction(MotionEvent.ACTION_UP);
            else if (result)
                event.setAction(MotionEvent.ACTION_CANCEL);
            else if (this.tapIndicator) {
                event.setAction(MotionEvent.ACTION_DOWN);
                this.tapIndicator = false;
            }
        }
        // else just do nothing, it's Transparent
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int m) {
        this.mode = m;
    }

    public void setEnabled(boolean status) {
        this.running = status;
    }

    public int getSwipeMinVelocity() {
        return this.swipe_Min_Velocity;
    }

    public void setSwipeMinVelocity(int distance) {
        this.swipe_Min_Velocity = distance;
    }

    /**
     * Parameters
     * e1 The first down motion event that started the fling.
     * e2 The move motion event that triggered the current onFling.
     * velocityX The velocity of this fling measured in pixels per second along the x axis.
     * velocityY The velocity of this fling measured in pixels per second along the y axis.
     * <p/>
     * 1) First we need to determine the axis of the swipe X or Y
     * 2) Next, determine whether the swipe meets the minimum distance per the axis
     * <p/>
     * Returns
     * true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());

        if (xDistance > this.swipe_Max_X_Distance || yDistance > this.swipe_Max_Y_Distance) {
            return false;
        }

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
        boolean result = false;

        if (velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_X_Distance) {
            if (e1.getX() > e2.getX()) // right to left
                this.listener.onSwipe(SWIPE_LEFT);
            else
                this.listener.onSwipe(SWIPE_RIGHT);

            result = true;
        } else if (velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Y_Distance) {
            if (e1.getY() > e2.getY()) // bottom to up
                this.listener.onSwipe(SWIPE_UP);
            else
                this.listener.onSwipe(SWIPE_DOWN);

            result = true;
        }

        return result;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        this.tapIndicator = true;
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg0) {
        this.listener.onDoubleTap();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg0) {

        if (this.mode == MODE_DYNAMIC) { // we owe an ACTION_UP, so we fake an
            arg0.setAction(ACTION_FAKE); // action which will be converted to an
            // ACTION_UP later.
            this.context.dispatchTouchEvent(arg0);
        }

        return false;
    }

    public static interface SimpleGestureListener {
        void onSwipe(int direction);

        void onDoubleTap();
    }

}
